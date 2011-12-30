package com.google.android.service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;


import com.google.android.lib.content.CreateRouteTrackPoint;

import com.google.android.lib.content.Route;
import com.google.android.lib.content.RouteTrackPoint;

import com.google.android.lib.content.data.MyRouteProvider;
import com.google.android.lib.logs.MyLogClass;
import com.google.android.lib.services.IRouteRecordingService;
import com.google.android.lib.statistics.RouteStatistics;
import com.google.android.location.content.Constants;
import com.google.android.location.content.R;
import com.google.android.location.preferences.PreferenceManager;
import com.google.android.location.tasks.PeriodicTaskExecutor;
import com.google.android.location.tasks.PeriodicTaskImpl;
import com.google.android.location.tasks.TripStatisticsManager;
import com.google.android.utilities.DefaultRouteNameFactory;
import com.google.android.utilities.LocationFilter;
import com.google.android.utilities.LocationListenerPolicy;
import com.google.android.utilities.MyLocationListenerPolicy;

import static com.google.android.location.content.Constants.*;


public class ServiceManager extends Service 
{
	static final int MAX_AUTO_RESUME_ROUTE_RETRY_ATTEMPTS = 3;
	private ServiceBinder mBinder = new ServiceBinder(this);
	private long recordingRouteId;
	private boolean isAlreadyStarted = false;
	private NotificationManager notificationManager;
	private LocationManager locationManager;
	MyRouteProvider myRouteProvider;
	private PreferenceManager prefManager;
	private PeriodicTaskExecutor routeTrackPointsUpdater;
	private TripStatisticsManager statsBuilder;
	private TripStatisticsManager routeTrackPointStatsBuilder;
	private long currentRouteTrackPointId = -1;
	/**
	 * The interval in milliseconds that we have requested to be notified of gps
	 * readings.
	 */
	private long currentRecordingInterval;
	private LocationListener locationListener = new LocationListener() {
		@Override
		public void onProviderDisabled(String provider) {
			// Do nothing
		}

		@Override
		public void onProviderEnabled(String provider) {
			// Do nothing
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Do nothing
		}

		@Override
		public void onLocationChanged(final Location location) {
			if (executorService.isShutdown() || executorService.isTerminated()) {
				return;
			}
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					onLocationChangedAsynchronous(location);
				}
			});
		}
	};
	/**
	 * A service to run tasks outside of the main thread.
	 */
	private ExecutorService executorService;
	/**
	 * the minimum distance for recording a point
	 */
	private int minRecordingDistance = Constants.DEFAULT_MIN_RECORDING_DISTANCE;
	/**
	 * maximum distance for recording a point
	 */
	private int maxRecordingDistance = Constants.DEFAULT_MAX_RECORDING_DISTANCE;
	/**
	 * minimum accuracy threshold
	 */
	private int minRequiredAccuracy = Constants.DEFAULT_MIN_REQUIRED_ACCURACY;
	/**
	 * auto resume route timeout
	 */
	private int autoResumeRouteimeout = Constants.DEFAULT_AUTO_RESUME_ROUTE_TIMEOUT;
	/**
	 * This timer invokes periodically the checkLocationListener timer task.
	 */
	private final Timer timer = new Timer();

	/**
	 * Is the phone currently moving?
	 */
	private boolean isMoving = true;

	/**
	 * The most recent recording track.
	 */
	private Route recordingRoute;

	/**
	 * Is the service currently recording a track?
	 */
	private boolean isRecording;

	/**
	 * Last good location the service has received from the location listener
	 */
	private Location lastLocation;

	/**
	 * Last valid location (i.e. not a marker) that was recorded.
	 */
	private Location lastValidLocation;
	/**
	 * Current length of the recorded route. This length is calculated from the
	 * recorded points (as compared to each location fix). It's used to overlay
	 * route track points precisely in the elevation profile chart.
	 */
	private double route_length;
	/**
	 * the checkLocationListener timerTask communicates with UI thread through a
	 * handlers that posts messages when its running time has finished.
	 */
	private final Handler handler = new Handler();;
	/**
	 * Task invoked by a timer periodically to make sure the location listener
	 * is still registered.
	 */
	private TimerTask checkLocationListener = new TimerTask() 
	{
		@Override
		public void run() 
		{
			// It's always safe to assume that if isRecording() is true, it
			// implies
			// that onCreate() has finished.
			if (isRecording()) {
				handler.post(new Runnable() 
				{
					public void run() 
					{
						Log.d(Constants.TAG,
								"Re-registering location listener with TrackRecordingService.");
						unregisterLocationListener();
						registerLocationListener();
					}
				});
			}
		}
	};
	LocationListenerPolicy locationListenerPolicy = new MyLocationListenerPolicy(
			0);

	@Override
	public void onCreate() 
	{
		super.onCreate();
		Log.d(MyLogClass.TAG, "Service:Oncreate");
		myRouteProvider = MyRouteProvider.Factory.getRouteProvider(this);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		setupRoutePeriodicUpdater();
		prefManager = new PreferenceManager(this);
		registerLocationListener();
		/*
		 * After 5 min, check every minute that location listener still is
		 * registered and spit out additional debugging info to the logs:
		 */
		timer.schedule(checkLocationListener, 1000 * 60 * 5, 1000 * 60);

		// Try to restore previous recording state in case this service has been
		// restarted by the system, which can sometimes happen.
		executorService = Executors.newSingleThreadExecutor();
		recordingRoute = getRecordingRoute();
		if (recordingRoute != null) 
		{
			restoreRouteFromStats(recordingRoute);
			isRecording = true;
		} 
		else 
		{
			if (recordingRouteId != -1) 
			{
				Log.w(TAG, "ServiceManager:Lost Route");
			}
			prefManager.setRecordingRoute(recordingRouteId = -1);
		}
		showNotification();
	}

	/**
	 * shows notification regarding the current route
	 */
	private void showNotification() 
	{
		Log.d(TAG, "ServiceManager:notifying StartActivity.");
		if (isRecording) 
		{
			Log.d(TAG,
					"ServiceManager:starts notifying and moving the service into foreground");
		}
	}
    /**
     * starts a new route and returns the route id
     * @return route of the current route being recorded
     */
	 private long startNewRoute()
	 {
		 Log.d(TAG, "ServiceManager:starts a new route");
		 if(isRouteInProgress())
		 {
			 return -1L;
		 }
		 long currentLocalStartTime = System.currentTimeMillis();
		 Route route = new Route();
		 RouteStatistics currentRouteStatistics = route.getRouteStatistics();
		 currentRouteStatistics.setStart_time(currentLocalStartTime);
		 route.setStart_id(-1);
		 Uri routeUri = myRouteProvider.insertRoute(route);
		 recordingRouteId = Long.parseLong(routeUri.getLastPathSegment());
		 route.setId(recordingRouteId);
		 route.setName(new DefaultRouteNameFactory(this).newTrackName(recordingRouteId,currentLocalStartTime));
		 route.setCategory(getDefaultActivityType(this));
		 isRecording = true;
		 isMoving = true;
		 myRouteProvider.updateRoute(route);
		 //sets the statistics managers
		 statsBuilder =  new TripStatisticsManager(currentLocalStartTime);
		 statsBuilder.setMinRecordingDistance(minRecordingDistance);
		 routeTrackPointStatsBuilder = new TripStatisticsManager(currentLocalStartTime);
		 routeTrackPointStatsBuilder.setMinRecordingDistance(minRecordingDistance);
		 //inserts the first route track point for knowing where we started our
		 //movement.
		 currentRouteTrackPointId = insertRouteTrackPoint(CreateRouteTrackPoint.DEFAULT_STATISTICS);
		 showNotification();
		 registerLocationListener();
		 setAutoResumeRouteRetries(0);
		 prefManager.setRecordingRoute(recordingRouteId);
		 //sendRouteBroadcat(R.string.route);
		 routeTrackPointsUpdater.restore(isRecording());
		 return recordingRouteId;
	 }
	 /**
	  * inserts a route track point with type deafault_statistics
	  * @param defaultStatistics
	  * @return route track point id
	  */
	 private long insertRouteTrackPoint(CreateRouteTrackPoint defaultStatistics) 
	 {  
		return 0;
	 }

	/**
	  * checks if the current route is in progress
	  */
	 private boolean isRouteInProgress()
	 {
		 return isRecording() || recordingRouteId!=-1;
	 }
	/**
	 * restore route from stats
	 */
	private void restoreRouteFromStats(Route route) 
	{
		Log.d(TAG, "ServiceManager:restore route from stats");
		Log.d(TAG, "Restoring stats of track with ID: " + route.getId());
	    RouteStatistics stats = route.getRouteStatistics();
	    statsBuilder = new TripStatisticsManager(stats.getStart_time());
	    statsBuilder.setMinRecordingDistance(minRecordingDistance);
	    route_length = 0;
	    lastValidLocation = null;

	    RouteTrackPoint routeTrackPoint = myRouteProvider.getRouteTrackPointById(recordingRouteId);
	    if (routeTrackPoint != null && routeTrackPoint.getRouteStatistics() != null) 
	    {
	      currentRouteTrackPointId = routeTrackPoint.getId();
	      routeTrackPointStatsBuilder = new TripStatisticsManager(route.getRouteStatistics());
	    } 
	    else 
	    {
	      // This should never happen, but we got to do something so life goes on:
	      routeTrackPointStatsBuilder = new TripStatisticsManager(stats.getStart_time());
	      currentRouteTrackPointId = -1;
	    }
	    routeTrackPointStatsBuilder.setMinRecordingDistance(minRecordingDistance);

	    Cursor cursor = null;
	    try 
	    {
	      cursor = myRouteProvider.getLocationsCursor(recordingRouteId, -1, Constants.MAX_LOADED_TRACK_POINTS, true);
	      if (cursor != null)
	      {
	        if (cursor.moveToLast()) 
	        {
	          do 
	          {
	            Location location = myRouteProvider.createLocation(cursor);
	            if (LocationFilter.isValidLocation(location))
	            {
	              statsBuilder.addLocation(location, location.getTime());
	              if (lastValidLocation != null)
	              {
	                route_length += location.distanceTo(lastValidLocation);
	              }
	              lastValidLocation = location;
	            }
	          } while (cursor.moveToPrevious());
	        }
	        statsBuilder.getStatistics().setMoving_time(stats.getMoving_time());
	        statsBuilder.pauseAt(stats.getStop_time());
	        statsBuilder.resumeAt(System.currentTimeMillis());
	      } else 
	      {
	        Log.e(TAG, "Could not get track points cursor.");
	      }
	    } catch (RuntimeException e) 
	    {
	      Log.e(TAG, "Error while restoring track.", e);
	    } finally {
	      if (cursor != null) {
	        cursor.close();
	      }
	    }

	}

	/**
	 * 
	 * @return the current recording route or null if no route is selected
	 */
	private Route getRecordingRoute() 
	{
		if (recordingRouteId < 0) {
			return null;
		}
		return myRouteProvider.getRouteById(recordingRouteId);
	}
    private Location getLastReportedLocation()
    {
    	return lastLocation;
    }

	private void unregisterLocationListener() 
	{
		Log.d(TAG, "ServiceManager:unregisters a location listener");
		if (locationManager == null) {
			Log.e(TAG, "ServiceManager: Do not have any location manager.");
			return;
		}
		locationManager.removeUpdates(locationListener);
		Log.d(TAG,
				"Location listener now unregistered w/ TrackRecordingService.");
	}

	private String getDefaultActivityType(Context context) 
	{
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(
				context.getString(R.string.default_activity_key), "");
	}

	/**
	 * first registers a location listener then checks the desired interval for
	 * requesting updates; after sets the currentRecordingInterval with the one
	 * from locationListener manager - received after registering the location
	 * listener
	 */
	private void registerLocationListener() {
		Log.d(TAG, "ServiceManager:registers a call to the location listeners");
		if (locationManager == null) {
			Log.e(TAG,
					"TrackRecordingService: Do not have any location manager.");
			return;
		}
		Log.d(TAG,
				"Preparing to register location listener w/ TrackRecordingService...");
		try {
			// first the desired interval is 0
			boolean checkGPSProvider = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean checkNetworkProvider = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (checkGPSProvider) 
			{
				long desiredInterval = locationListenerPolicy.getDesiredPollingInterval();

				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER,
						// 0 in the first case, when starting//
						desiredInterval,
						locationListenerPolicy.getMinDistance(),
						// , 0 /* minDistance, get all updates to properly time
						// pauses */
						locationListener);
				currentRecordingInterval = desiredInterval;
				Log.d(TAG,"...location listener now registered w/ ServiceManager @ " + currentRecordingInterval);
			} else if (checkNetworkProvider) 
			{
				long desiredInterval = locationListenerPolicy.getDesiredPollingInterval();

				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER,
						// 0 in the first case, when starting//
						desiredInterval,
						locationListenerPolicy.getMinDistance(),
						// , 0 /* minDistance, get all updates to properly time
						// pauses */
						locationListener);
				currentRecordingInterval = desiredInterval;
				Log.d(TAG,
						"...location listener now registered w/ TrackRecordingService @ "
								+ currentRecordingInterval);
			}
		} 
		catch (RuntimeException e) 
		{
			Log.e(TAG,"Could not register location listener: " + e.getMessage(),e);
		}

	}

	/**
	 * set up the periodic updater for inserting markers on the map when a
	 * threshold is met. if the user stays idle for a long time in a place the
	 * place is recorded as an end location, as a route track point.
	 */
	public void setupRoutePeriodicUpdater() {
		if (isRecording) {
			routeTrackPointsUpdater = new PeriodicTaskExecutor(
					new PeriodicTaskImpl.Factory(), isRecording,
					locationListenerPolicy);
		}

	}

	/**
	 * because it's possible that the service is accesed by either the app
	 * itself or 3d party app, thus we allow only start or resume actions.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startCommand(intent, startId);
		return START_STICKY;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		startCommand(intent, startId);
	}
    /**
     * 
     * @param intent that is used for starting
     * @param startId the unique id assigned to the service when it starts.
     * if the intent contains the RESUME_TRACK_EXTRA_NAME then it means that
     * a rebooting has occured somewhere in time. thus we just resume the service and the route
     */
	public void startCommand(Intent intent, int startId) {
		Log.d(TAG, "ServiceManager:onStart");
		if (intent == null) {
			Log.d(TAG, "ServiceManager:intent is null");
		}
		if (intent.getBooleanExtra(RESUME_TRACK_EXTRA_NAME, false)) {
			resumeServiceById(startId);
		}
	}

	/**
	 * changing the location async
	 */
	private void onLocationChangedAsynchronous(Location location) 
	{
      Log.d(TAG, "ServiceManager:OnLocationChangedAsynchronous");
      try 
      {
		if(!isRecording || location==null || (location.getAccuracy() > minRequiredAccuracy))
		{
			Log.d(TAG, "ServiceManager: not recording or location is null or bad accuracy");
			return;
		}
		recordingRoute = getRecordingRoute();
		if(recordingRoute==null)
		{
			Log.d(TAG, "ServiceManager:not recording.no recording route");
		}
		if(currentRecordingInterval!=locationListenerPolicy.getDesiredPollingInterval())
		{
			registerLocationListener();
		}
		addLocationToStats(location);
		Location lastRecordedLocation = myRouteProvider.getLastRecordedLocation();
		double distanceToLastRecorded = Double.POSITIVE_INFINITY;
		if(lastRecordedLocation!=null)
		{
			distanceToLastRecorded = location.distanceTo(lastRecordedLocation);
		}
		double distanceToLastLocation = Double.POSITIVE_INFINITY;
		if(lastLocation!=null)
		{
			distanceToLastLocation = location.distanceTo(lastLocation);
		}
        if(distanceToLastLocation==0)
        {
        	if(isMoving)
        	{
        		Log.d(TAG, "ServiceManager:found two identical locations.");
        	   isMoving =  false;
        	   if(lastLocation!=null && lastRecordedLocation!=null &&!lastRecordedLocation.equals(lastLocation))
        	   {
        		  if(!insertLocation(location, lastRecordedLocation, recordingRouteId))
        		  {
        			  return;
        		  }
        	   }
        	}
        }
        
				
	  } 
      catch (Error e) 
      {
		Log.e(TAG, "ServiceManager:Exception",e);
	    throw e;
      }
      catch(RuntimeException ex)
      {
    	  throw ex;
      }
      lastLocation = location;
	} 
	private boolean insertLocation(Location location, Location lastRecordedLocation, long recordingRouteId)
	{
		Log.d(TAG, "ServiceManager:insertLocation");
		if(LocationFilter.isValidLocation(location))
		{
			if(lastValidLocation!=null)
			{
				route_length +=location.distanceTo(lastRecordedLocation);
			}
			lastValidLocation = location;
		}
		//trying to insert the new location
		try 
		{
		   Location locationToInsert = location;
		   
		}
		catch (SQLException ex) 
		{
			Log.w(TAG, "ServiceManager:insertLocationError");
			return false;
		}
		return true;
	}

	private void addLocationToStats(Location location)
	{
		if(LocationFilter.isValidLocation(location))
		{
			long now = System.currentTimeMillis();
			statsBuilder.addLocation(location, now);
			routeTrackPointStatsBuilder.addLocation(location, now);
		}
	}

	/**
	 * resume a service by its unique id - every service when starting has an
	 * unique id assigned.
	 * 
	 * @param startId
	 *            the id that was assigned to the service
	 */
	private void resumeServiceById(int startId) {
		Log.d(TAG, "ServiceManager:Resumes the service by its unique id");
		if (recordingRoute == null || !shouldResumeRoute(recordingRoute)) {
			Log.i(TAG,
					"ServieManager:couldn't resume route , either is too old or doesn't exist");
			isRecording = false;
			prefManager.setRecordingRoute(recordingRouteId = -1);
			stopSelfResult(startId);
			return;
		}
		Log.i(TAG,
				"ServiceManager:resuming the servicea and the route tracking");
	}

	private boolean shouldResumeRoute(Route route) 
	{
		SharedPreferences sharedPreferences = getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		int retries = sharedPreferences.getInt(
				getString(R.string.auto_resume_route_current_retry_key), 0);
		Log.d(TAG,
				"ServiceManager:attempting resuming the current route, retries:"
						+ MAX_AUTO_RESUME_ROUTE_RETRY_ATTEMPTS + ")");
		if (retries > MAX_AUTO_RESUME_ROUTE_RETRY_ATTEMPTS) {
			Log.d(TAG, "ServiceManager:max limit exceeded");
			return false;
		}
		// sets the number of retry attempts
		setAutoResumeRouteRetries(retries + 1);
		if (autoResumeRouteimeout == 0) 
		{
			Log.d(TAG, "ServiceManange:autoResume is disabled");
			return false;
		} else if (autoResumeRouteimeout == -1) {
			Log.d(TAG, "ServiceManager:Always resume");
			return true;
		}
		// checks the last time the route stopped - the time that the route
		// stopped recording
		// if the stop time is different by null thast was the last time
		// something was recorded
		// otherwise 0
		long lastTime = route.getRouteStatistics() != null ? route.getRouteStatistics().getStop_time() : 0;
		return lastTime > 0 && System.currentTimeMillis() - lastTime <= autoResumeRouteimeout * 60L * 100L;
	}

	/**
	 * updating the current number of autoresume attempts
	 */
	public void setAutoResumeRouteRetries(int retries) {
		Log.d(TAG, "ServiceMananger:Settin up the number of retries - "
				+ retries);
		prefManager.setAutoResumeRouteCurrentRetry(retries);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}



	public boolean isRecording() {
		return isRecording;
	}

	public void setMinRecordingDistance(int minRecordingDistance) 
	{
		this.minRecordingDistance = minRecordingDistance;
		if (statsBuilder != null && routeTrackPointStatsBuilder != null) 
		{
			statsBuilder.setMinRecordingDistance(minRecordingDistance);
			routeTrackPointStatsBuilder.setMinRecordingDistance(minRecordingDistance);
		}
	}

	public void setAutoResumeRouteTimeout(int autorResumeRouteTimeOut) {
		this.autoResumeRouteimeout = autorResumeRouteTimeOut;
	}

	public void setPeriodicTaskKey(int value) {
		Log.d(TAG, "ServiceManager:setting up the Route Insert Points manager");
	}

	public void setMetricUnits(boolean metricUnits) {
		routeTrackPointsUpdater.setMetricUnits(metricUnits);
	}

	public void setRecordingRouteId(long recordingRouteId) {
		this.recordingRouteId = recordingRouteId;
	}

	public void setMinRequiredAccuracy(int minAccuracy) {
		this.minRequiredAccuracy = minAccuracy;
	}

	public void setMaxRecordingDistance(int maxRecordingDistance) 
	{
		this.maxRecordingDistance = maxRecordingDistance;
	}

	public void setLocationListenerPolicy(LocationListenerPolicy myLocationListenerPolicy) 
	{
		this.locationListenerPolicy = myLocationListenerPolicy;
	}

	/**
	 * 
	 * @return a new object of RouteStatistics from an existing one - using a
	 *         copy constructor
	 */
	public RouteStatistics getTripStatistics() {
		return statsBuilder.getStatistics();
	}
	@Override
	public boolean onUnbind(Intent intent) 
	{
	    Log.d(TAG, "TrackRecordingService.onUnbind");
	    return super.onUnbind(intent);
    }
	public void onDestroy() 
	{
		Log.d(TAG, "ServiceManager:Destroy the service");
		isRecording = false;
		showNotification();
		// stops listening to preference updates/savings
		prefManager.shutdown();
		prefManager = null;
		checkLocationListener.cancel();
		checkLocationListener = null;
		timer.cancel();
		timer.purge();
		unregisterLocationListener();
		shutDownRoutePeriodicUpdater();
	    // Make sure we have no indirect references to this service.
	    locationManager = null;
	    notificationManager = null;
	    myRouteProvider = null;
	    mBinder.detachFromService();
	    mBinder = null;
	    // Shutdown the executor service last to avoid sending events to a dead
	    // executor.
	    executorService.shutdown();
	    super.onDestroy();

	}

	public void shutDownRoutePeriodicUpdater() 
	{
		Log.d(TAG, "ServiceManager:");
	}

	/**
	 * 
	 */
	public long insertAndReturnRouteTrackPointId() {
		Log.d(TAG,"ServiceManager:inserts and return the id of the current route track point");
		return 0;
	}
	public void endCurrentRoute() 
	{
       Log.d(TAG, "ServiceManager:endCurrentRoute");
	}
	/**
	 * Service binder which leaks memory. the recommanded solution from google
	 * is to use a static version.
	 * 
	 * @author Andrei
	 * 
	 */
	public static class ServiceBinder extends IRouteRecordingService.Stub {

		DeathRecipient deathRecipient;
		ServiceManager service;

		public ServiceBinder(ServiceManager m_hRouteRecordingService) {
			this.service = m_hRouteRecordingService;
		}

		@Override
		public void linkToDeath(DeathRecipient recipient, int flags) {
			deathRecipient = recipient;
		}

		@Override
		public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
			if (!isBinderAlive()) {
				return false;
			}

			deathRecipient = null;
			return true;
		}

		/**
		 * Returns true if the RPC caller is from the same application or if the
		 * "Allow access" setting indicates that another app can invoke this
		 * service's RPCs. For the moment is set to
		 */
		private boolean canAccessPreferences() 
		{
			checkServiceStatus();
			if (Process.myPid() == Binder.getCallingPid()) 
			{
				return true;
			} 
			else 
			{
				SharedPreferences sharedPreferences = service.getSharedPreferences(Constants.SETTINGS_NAME,Context.MODE_PRIVATE);
				return sharedPreferences.getBoolean(service.getString(R.string.allow_access_key), false);
			}
		}
	    /**
	     * Clears the reference to the outer class to minimize the leak.
	     */
	    private void detachFromService() {
	      this.service = null;
	      attachInterface(null, null);

	      if (deathRecipient != null) {
	        deathRecipient.binderDied();
	      }
	    }
		/**
		 * checks service stats
		 */
		private void checkServiceStatus() {
			if (service == null) 
			{
				throw new IllegalStateException("ServicBinder:Service detached");
			}
		}

		/**
		 * checks if the service is recording or not
		 */
		@Override
		public boolean isRecording() throws RemoteException {
			if (!canAccessPreferences()) {
				return false;
			}
			return service.isRecording();
		}

		@Override
		public boolean isBinderAlive() {
			return service != null;
		}

		@Override
		public long startNewRouteId() throws RemoteException {
			if (!canAccessPreferences()) {
				return -1L;
			}
			return service.startNewRoute();
		}

		@Override
		public long getRecordingRouteId() throws RemoteException {
			return service.recordingRouteId;
		}

		@Override
		public void recordLocation(Location loc) throws RemoteException 
		{
            if(!canAccessPreferences())
            {
            	return;
            }
            service.locationListener.onLocationChanged(loc);
		}
		/**
		 * pings the binder
		 */
        @Override
        public boolean pingBinder() 
        {
        	return isBinderAlive();
        } 
		@Override
		public void calculateStatistics() throws RemoteException 
		{
            Log.d(TAG, "ServiceBinder:calculate the statistics");
		}
        /**
         * checks if a route track point exits
         */
		@Override
		public boolean existEndPointAtId() throws RemoteException 
		{
			if(!canAccessPreferences())
			{
				return false;
			}
			else
			{
				if(service.getRecordingRoute()!=null)
				{
					Log.d(TAG, "ServiceBinder:checking for a route track point with the given id");
					return true;
				}
			}
			return false;
		}

		@Override
		public void endCurrentRoute() throws RemoteException {
			if (!canAccessPreferences()) {
				return;
			}
			service.endCurrentRoute();
		}

		@Override
		public byte[] getSensorData() throws RemoteException {
			return null;
		}

		@Override
		public int getSensorState() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long insertAndReturnRouteTPId(CreateRouteTrackPoint track)
				throws RemoteException {
			if (!canAccessPreferences()) {
				return -1L;
			} else
				return service.insertAndReturnRouteTrackPointId();
		}

	}
}

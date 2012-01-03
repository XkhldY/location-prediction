package com.google.android.service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
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
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;



import com.google.android.lib.content.CreateRouteTrackPoint;

import com.google.android.lib.content.Route;
import com.google.android.lib.content.RouteLocation;
import com.google.android.lib.content.RouteTrackPoint;
import com.google.android.lib.content.RoutesColumns;
import com.google.android.lib.content.RoutesTrackPointsColumns;

import com.google.android.lib.content.data.CreateLocationFactory;
import com.google.android.lib.content.data.DoubleCachedLocationFactory;
import com.google.android.lib.content.data.MyRouteProvider;
import com.google.android.lib.logs.MyLogClass;
import com.google.android.lib.services.IRouteRecordingService;
import com.google.android.lib.statistics.RouteStatistics;
import com.google.android.location.content.Constants;
import com.google.android.location.content.R;
import com.google.android.location.content.StartActivity;
import com.google.android.location.preferences.PreferenceManager;
import com.google.android.location.tasks.PeriodicTaskExecutor;
import com.google.android.location.tasks.PeriodicTaskImpl;
import com.google.android.location.tasks.TripStatisticsManager;
import com.google.android.utilities.DefaultRouteNameFactory;
import com.google.android.utilities.LocationFilter;
import com.google.android.utilities.LocationListenerPolicy;
import com.google.android.utilities.MyLocationListenerPolicy;
import com.google.android.utilities.NotificationManagerAdapter;
import com.google.android.utilities.NotifyAdapterFactory;
import com.google.android.utilities.StringsProfiler;


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
	CreateLocationFactory locationFactory;
	private WakeLock wakeLock;
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
		locationFactory = new DoubleCachedLocationFactory();
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
	 * shows notification regarding the current route and an icon in the notification bar.
	 */
	private void showNotification() 
	{
		Log.d(TAG, "ServiceManager:notifying StartActivity.");
		final NotificationManagerAdapter notifyAdapter = NotifyAdapterFactory.getInstance().getNotifyManagerAdapter();
		if (isRecording) 
		{
			Log.d(TAG,
					"ServiceManager:starts notifying and moving the service into foreground");
			Notification notification = new Notification(R.drawable.arrow_320, null /* tickerText */,System.currentTimeMillis());
		    PendingIntent contentIntent = PendingIntent.getActivity(this, 0 /* requestCode */, 
		                                                  new Intent(this, StartActivity.class), 0 /* flags */);
		    notification.setLatestEventInfo(this, getString(R.string.my_routes_app_name), 
		                                      getString(R.string.route_record_notification), contentIntent);
		    notification.flags += Notification.FLAG_NO_CLEAR;
		    notifyAdapter.startForeground(this, notificationManager, 1, notification);
		}
		else
		{
			notifyAdapter.stopForeground(this, notificationManager, 1);
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
		 acquireWakeLock();
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
		 //routeTrackPointsUpdater.restore(isRecording());
		 return recordingRouteId;
	 }

	/**
	 * updates current route track point
	 */
	private void updateCurrentRouteTrackPoint() {
		if (currentRouteTrackPointId >= 0) {
			ContentValues values = new ContentValues();
			RouteStatistics routeTrackPointsStats = routeTrackPointStatsBuilder
					.getStatistics();
			values.put(RoutesTrackPointsColumns.START_TIME,
					routeTrackPointsStats.getStart_time());
			values.put(RoutesTrackPointsColumns.LEGNTH_OF_TRACK, route_length);
			values.put(RoutesTrackPointsColumns.TRACK_DURATION,
					System.currentTimeMillis()
							- statsBuilder.getStatistics().getStart_time());
			values.put(RoutesTrackPointsColumns.TOTAL_DISTANCE,
					routeTrackPointsStats.getTotalDistance());
			values.put(RoutesTrackPointsColumns.TOTAL_TIME,
					routeTrackPointsStats.getTotal_time());
			values.put(RoutesTrackPointsColumns.MOVING_TIME,
					routeTrackPointsStats.getMoving_time());
			values.put(RoutesTrackPointsColumns.AVG_SPEED,
					routeTrackPointsStats.getAverageSpeed());
			values.put(RoutesTrackPointsColumns.AVG_MOVING_SPEED,
					routeTrackPointsStats.getAverageMovingSpeed());
			values.put(RoutesTrackPointsColumns.MAX_SPEED,
					routeTrackPointsStats.getMax_speed());
			values.put(RoutesTrackPointsColumns.MIN_ELEVATION,
					routeTrackPointsStats.getMinElevation());
			values.put(RoutesTrackPointsColumns.MAX_ELEVATION,
					routeTrackPointsStats.getMaxElevation());
			values.put(RoutesTrackPointsColumns.ELEVATION_GAIN_CURRENT,
					routeTrackPointsStats.getTotal_elevation_gain());
			values.put(RoutesTrackPointsColumns.MIN_GRADE,
					routeTrackPointsStats.getMinGrade());
			values.put(RoutesTrackPointsColumns.MAX_GRADE,
					routeTrackPointsStats.getMaxGrade());
			getContentResolver().update(RoutesTrackPointsColumns.CONTENT_URI,
					values, "_id = " + currentRouteTrackPointId, null);
		}
	}
	 /**
	  * inserts a route track point with type deafault_statistics
	  * @param defaultStatistics
	  * @return route track point id
	  */
	 public long insertRouteTrackPoint(CreateRouteTrackPoint defaultMarker) 
	 {  
		 if(!isRecording())
		 {
			 throw new IllegalStateException("Unable to insert route track points while not recording...");
		 }
		 if(defaultMarker==null)
		 {
			 defaultMarker = CreateRouteTrackPoint.DEFAULT_MARKER;
		 }
		 RouteTrackPoint routeTrackPoint = new RouteTrackPoint();
		 switch(defaultMarker.getType())
		 {
		 case MARKER:
			 buildMarker(routeTrackPoint,defaultMarker);
			 break;
		 case STATISTICS:
			 buildStatisticsMarker(routeTrackPoint);
			 break;
		 }
		 routeTrackPoint.setRouteId(recordingRouteId);
		 routeTrackPoint.setRouteLength(route_length);
		 if(lastLocation ==  null || statsBuilder==null || statsBuilder.getStatistics() == null)
		 {
			 //in the beginning it should be exactly like this
			 Location location = new Location("");//no provider for now
			 location.setLatitude(100);
			 location.setLongitude(180);
			 routeTrackPoint.setLocation(location);
		 }
		 else
		 {
			 routeTrackPoint.setLocation(lastLocation);
			 routeTrackPoint.setDuration(lastLocation.getTime() - statsBuilder.getStatistics().getStart_time());
		 }
		 Uri uri = myRouteProvider.insertRouteTrackPoint(routeTrackPoint);
		 return Long.parseLong(uri.getLastPathSegment());
	 }
	 /**
	   * Build a statistics marker. A statistics marker holds the stats for the*
	   * last segment up to this marker.
	   * 
	   * @param waypoint The route track point which will be populated with stats data.
	   */
	private void buildStatisticsMarker(RouteTrackPoint routeTrackPoint) 
	{
		StringsProfiler stringsProfiler =  new StringsProfiler(ServiceManager.this);
		 // Set stop and total time in the stats data
	    final long time = System.currentTimeMillis();
	    routeTrackPointStatsBuilder.pauseAt(time);

	    // Override the duration - it's not the duration from the last waypoint, but
	    // the duration from the beginning of the whole track
	    routeTrackPoint.setDuration(time - statsBuilder.getStatistics().getStart_time());

	    // Set the rest of the routeTrackPoint data
	    routeTrackPoint.setType(RouteTrackPoint.TYPE_OF_TRACKPOINT);
	    routeTrackPoint.setName(getString(R.string.marker_type_statistics));
	    routeTrackPoint.setRouteStatisticss(routeTrackPointStatsBuilder.getStatistics());
	    routeTrackPoint.setDescription(stringsProfiler.generateRouteTrackPointDescription(routeTrackPoint));
	    routeTrackPoint.setIcon(getString(R.string.marker_statistics_icon_url));

	    routeTrackPoint.setStartPointid(myRouteProvider.getLastLocationId(recordingRouteId));

	    // Create a new stats keeper for the next marker.
	    routeTrackPointStatsBuilder = new TripStatisticsManager(time);
	}

	private void buildMarker(RouteTrackPoint routeTrackPoint, CreateRouteTrackPoint defaultMarker) 
	{
		routeTrackPoint.setType(RouteTrackPoint.TYPE_OF_TRACKPOINT);
		if (defaultMarker.getIconUrl() == null) 
		{
			routeTrackPoint.setIcon(getString(R.string.marker_waypoint_icon_url));
		} 
		else 
		{
			routeTrackPoint.setIcon(defaultMarker.getIconUrl());
		}
		if (defaultMarker.getName() == null) 
		{
			routeTrackPoint.setName(getString(R.string.marker_type_waypoint));
		} 
		else 
		{
			routeTrackPoint.setName(defaultMarker.getName());
		}
		if (defaultMarker.getDescription() != null) 
		{
			routeTrackPoint.setDescription(defaultMarker.getDescription());
		}
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
        routeTrackPointsUpdater.restore(isRecording());
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
			} else 
			if (checkNetworkProvider) 
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
        //routeTrackPointsUpdater.update();
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
					locationListenerPolicy, this);
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
	
		addLocationToStats(location);
		if(currentRecordingInterval!=locationListenerPolicy.getDesiredPollingInterval())
		{
			registerLocationListener();
		}
		
		Location lastRecordedLocation = myRouteProvider.getLastRecordedLocation();
		
		long currentIdleTime = 0;
		
		double distanceToLastRecorded = Double.POSITIVE_INFINITY;
		
		if(lastRecordedLocation!=null)
		{
			currentIdleTime = location.getTime() - lastRecordedLocation.getTime();
		    distanceToLastRecorded = location.distanceTo(lastRecordedLocation);
		}
		locationListenerPolicy.setLastLocation(lastRecordedLocation);
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
        	   // Need to write the last location. This will happen when
               // lastRecordedLocation.distance(lastLocation) <
               // minRecordingDistance
        	   if(lastLocation!=null && lastRecordedLocation!=null &&!lastRecordedLocation.equals(lastLocation))
        	   {
        		  if(!insertLocation(lastLocation, lastRecordedLocation, recordingRouteId))
        		  {
        			  return;
        		  }
        		  currentIdleTime = location.getTime() - lastLocation.getTime();
        		  locationListenerPolicy.setLastLocation(lastLocation);
        		  locationListenerPolicy.setDistance(distanceToLastLocation);
        		  locationListenerPolicy.updateIdleTime(currentIdleTime);
        	   }
        	   else if(lastLocation!=null && lastRecordedLocation!=null && lastRecordedLocation.equals(lastLocation))
        	   {
        		  currentIdleTime = location.getTime() - lastRecordedLocation.getTime();
         		  locationListenerPolicy.setLastLocation(lastLocation);
         		  locationListenerPolicy.setDistance(distanceToLastLocation);
         		  locationListenerPolicy.updateIdleTime(currentIdleTime);
        	   }
        	}
        	else
        	{
        		Log.d(TAG, "ServiceManager:not recording.two identical locations");
        	}
        }
        else if(distanceToLastRecorded > minRecordingDistance)
        {
          if(lastLocation!=null && !isMoving)
          {
        	   // Last location was the last stationary location. Need to go back and
              // add it.
        	   if(!insertLocation(lastLocation, lastRecordedLocation, recordingRouteId))
        	   {
        		   return;
        	   }
        
        	   isMoving = true;
          }
          boolean startNewSegment = lastRecordedLocation != null
                  && lastRecordedLocation.getLatitude() < 90
                  && distanceToLastRecorded > maxRecordingDistance && recordingRoute.getStart_id() >= 0;
              if (startNewSegment) 
              {
                // Insert a separator point to indicate start of new track:
                //we need another technique here because it's very important to know when a track starts and another one ends.
                Log.d(TAG, "Inserting a separator.");
                Location separator = new Location(LocationManager.GPS_PROVIDER);
                separator.setLongitude(0);
                separator.setLatitude(100);
                separator.setTime(lastRecordedLocation.getTime());
                //insert a new location point for the new route
                myRouteProvider.insertRoutePoint(separator, recordingRouteId);
              }
              //being the first location we need to update the locationListenerPolicy
              if(lastLocation == null || lastRecordedLocation==null)
              {
            	  Log.d(TAG, "ServiceManager:first location.");
            	  locationListenerPolicy.setLastLocation(location);
            	  locationListenerPolicy.setDistance(0);
            	  locationListenerPolicy.updateIdleTime(0);
              }
              //we know that this is the first location so we insert it
              if (!insertLocation(location, lastRecordedLocation, recordingRouteId)) 
              {
                return; 
              }
              if(lastLocation!=null )
              { 
            	  currentIdleTime = 0;
            	  locationListenerPolicy.setLastLocation(location);
                  locationListenerPolicy.updateIdleTime(currentIdleTime);
                  locationListenerPolicy.setDistance(0);  
            	  
              }
        }
        else
        {
        	Log.d(TAG,  String.format("Not recording.distance to last point is less than the " +
        			"minimum required distance"));
        	currentIdleTime = location.getTime() -  lastRecordedLocation.getTime();
        	locationListenerPolicy.updateIdleTime(currentIdleTime);
            locationListenerPolicy.setLastLocation(lastRecordedLocation);
        	locationListenerPolicy.setDistance(distanceToLastRecorded);
        	return;
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
      routeTrackPointsUpdater.update();
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
		   //Location locationToInsert = null;
			RouteLocation locationToInsert = null;
			//locationToInsert = new RouteLocation(location);
			if(myRouteProvider.checkLocation(location, recordingRouteId,locationFactory))
			{
				
				//we get a cursor over all the locations from the database given a routeId or 0
			   Cursor cursor = myRouteProvider.getLocationsCursor(recordingRouteId, 0, 
					                                              recordingRoute.getNumberRoutePoints(), true);
			   
			   locationToInsert = new RouteLocation(location);
			   //then we update the location that we found a match for
			   myRouteProvider.updateLocationTimesCount(locationToInsert,recordingRouteId, locationFactory, cursor);
			}
			else
				if(!myRouteProvider.checkLocation(location, recordingRouteId,locationFactory))
				{
					locationToInsert = new RouteLocation(location); 
					locationToInsert.setIdleTime(0);
					locationToInsert.setTimesCount(0);
				}
		      
		  
		   Uri pointToInsertUri = myRouteProvider.insertRoutePoint(locationToInsert, recordingRouteId);
		   int pointToInsertId = Integer.parseInt(pointToInsertUri.getLastPathSegment());
		   //this check is for differentiating start points from other points on the way
		   if(lastRecordedLocation!=null && lastRecordedLocation.getAltitude() < 90)
		   {
			   ContentValues values = new ContentValues();
			   RouteStatistics routeStatistics = statsBuilder.getStatistics();
			   //first time we dont have any points
			   if(recordingRoute.getStart_id() < 0)
			   {
				   values.put(RoutesColumns.START_ID, pointToInsertId);
				   recordingRoute.setStart_id(recordingRouteId);
			   }
			   values.put(RoutesColumns.STOP_ID, pointToInsertId);
		        values.put(RoutesColumns.STOP_TIME, System.currentTimeMillis());
		        values.put(RoutesColumns.NUMBER_POINTS, recordingRoute.getNumberRoutePoints() + 1);
		        values.put(RoutesColumns.MIN_LAT, routeStatistics.getLowestLatitude());
		        values.put(RoutesColumns.MAX_LAT, routeStatistics.getHighestLatitude());
		        values.put(RoutesColumns.MIN_LONG, routeStatistics.getLowestLongitude());
		        values.put(RoutesColumns.MAX_LONG, routeStatistics.getHighestLongitude());
		        values.put(RoutesColumns.TOTAL_DISTANCE, routeStatistics.getTotalDistance());
		        values.put(RoutesColumns.TOTAL_TIME, routeStatistics.getTotal_time());
		        values.put(RoutesColumns.MOVING_TIME, routeStatistics.getMoving_time());
		        values.put(RoutesColumns.AVG_SPEED, routeStatistics.getAverageSpeed());
		        values.put(RoutesColumns.AVG_MOVING_SPEED, routeStatistics.getAverageMovingSpeed());
		        values.put(RoutesColumns.MAX_SPEED, routeStatistics.getMax_speed());
		        values.put(RoutesColumns.MIN_ELEVATION, routeStatistics.getMinElevation());
		        values.put(RoutesColumns.MAX_ELEVATION, routeStatistics.getMaxElevation());
		        values.put(RoutesColumns.ELEVATION_GAIN_CURRENT, routeStatistics.getTotal_elevation_gain());
		        values.put(RoutesColumns.MIN_GRADE, routeStatistics.getMinGrade());
		        values.put(RoutesColumns.MAX_GRADE, routeStatistics.getMaxGrade());
		        getContentResolver().update(RoutesColumns.CONTENT_URI, values, "_id = " + recordingRoute.getId(), null);
		        //updating current route track point
		        updateCurrentRouteTrackPoint();
		   }
		}
		catch (SQLException ex) 
		{
			Log.w(TAG, "ServiceManager:insertLocationError");
			return false;
		}
		routeTrackPointsUpdater.update();
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
		// if the stop time is different by null that was the last time
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
	@Override
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
	    releaseWakeLock();
	    executorService.shutdown();
	    super.onDestroy();

	}
    
	public void shutDownRoutePeriodicUpdater() 
	{
		Log.d(TAG, "ServiceManager:");
	   try 
	   {
		  routeTrackPointsUpdater.shutdown();
	   } 
	   finally  
	   {
		  routeTrackPointsUpdater =null;
	   }	
	}
	/**
	   * Tries to acquire a partial wake lock if not already acquired. Logs errors
	   * and gives up trying in case the wake lock cannot be acquired.
	   */
	  private void acquireWakeLock() {
	    try {
	      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	      if (pm == null) {
	        Log.e(TAG, "TrackRecordingService: Power manager not found!");
	        return;
	      }
	      if (wakeLock == null) {
	        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
	        if (wakeLock == null) {
	          Log.e(TAG, "TrackRecordingService: Could not create wake lock (null).");
	          return;
	        }
	      }
	      if (!wakeLock.isHeld()) {
	        wakeLock.acquire();
	        if (!wakeLock.isHeld()) {
	          Log.e(TAG, "TrackRecordingService: Could not acquire wake lock.");
	        }
	      }
	    } catch (RuntimeException e) {
	      Log.e(TAG, "TrackRecordingService: Caught unexpected exception: " + e.getMessage(), e);
	    }
	  }

	  /**
	   * Releases the wake lock if it's currently held.
	   */
	  private void releaseWakeLock() 
	  {
	    if (wakeLock != null && wakeLock.isHeld()) 
	    {
	      wakeLock.release();
	      wakeLock = null;
	    }
	  }
	/**
	 * 
	 */
	public long insertAndReturnRouteTrackPointId() {
		Log.d(TAG,"ServiceManager:inserts and return the id of the current route track point");
		return 0;
	}
	/**
	 * 
	 */
	public void endCurrentRoute() 
	{
       Log.d(TAG, "ServiceManager:endCurrentRoute");
       if(!isRouteInProgress())
       {
    	   return;
       }
       routeTrackPointsUpdater.shutdown();
       isRecording = true;
       Route mRecordedRoute = myRouteProvider.getRouteById(recordingRouteId);
       if(mRecordedRoute!=null)
       {
    	   RouteStatistics routeStats = mRecordedRoute.getRouteStatistics();
    	   routeStats.setStop_time(routeStats.getStop_time() - routeStats.getStart_time());
    	   long lastRecordedLcationId = myRouteProvider.getLastLocationId(recordingRouteId);
           ContentValues values = new ContentValues();
           if(lastRecordedLcationId >=0 && mRecordedRoute.getStop_id()>=0)
           {
        	   values.put(RoutesColumns.STOP_ID, lastRecordedLcationId);
           }
           values.put(RoutesColumns.STOP_TIME, routeStats.getStop_time());
           values.put(RoutesColumns.TOTAL_TIME, routeStats.getTotal_time());
           getContentResolver().update(RoutesColumns.CONTENT_URI, values, "_id = " + mRecordedRoute.getId(), null);
       }
       showNotification();
       long mLastRecordedRouteId = recordingRouteId;
       prefManager.setRecordingRoute(mLastRecordedRouteId = -1);
       releaseWakeLock();
       stopSelf();
       
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

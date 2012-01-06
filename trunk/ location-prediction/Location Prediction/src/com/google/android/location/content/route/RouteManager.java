package com.google.android.location.route;

import java.util.EnumSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.lib.content.data.DoubleLastLocations;
import com.google.android.lib.content.data.MyRouteProvider;
import com.google.android.location.content.Constants;
import com.google.android.location.content.R;
import com.google.android.location.route.MapManagerDataSourceListener.DataSourceListener;


import static com.google.android.location.content.Constants.*;

public class RouteManager {
	// preference keys
	private final String SELECTED_ROUTE_KEY;
	private final String RECORDING_ROUTE_KEY;
	private final String MIN_REQUIRED_ACCURACY_KEY;
	private final String METRIC_UNITS_KEY;
	private final String SPEED_REPORTING_KEY;
	// threshold for resampling the points
	private final int thresholNumberPoints;

	/**
	 * data that after updating the database needs to update the view(map)
	 */
	public static enum ListenerDataType {
		/** Listen to when the selected track changes. */
		SELECTED_ROUTE_CHANGED,

		/** Listen to when the tracks change. */
		ROUTE_UPDATES,

		/** Listen to when the tracking points change. */
		ROUTE_TRACKPOINT_UPDATES,

		/** Listen to when the current track points change. */
		POINT_UPDATES,

		/**
		 * Listen to sampled-out points. Listening to this without listening to
		 * {@link #POINT_UPDATES} makes no sense and may yield unexpected
		 * results.
		 */
		SAMPLED_OUT_POINT_UPDATES,

		/** Listen to updates to the current location. */
		LOCATION_UPDATES,

		/** Listen to updates to the current heading. */
		COMPASS_UPDATES,

		/** Listens to changes in display preferences. */
		DISPLAY_PREFERENCES;
	}

	/** Listener which receives events from the system. */
	private class ManagerDataSourceListener
			implements
			com.google.android.location.route.MapManagerDataSourceListener.DataSourceListener {
		private boolean hasProviderEnabled;
		private boolean hasFix;
		private float lastSeenMagneticHeading;

		@Override
		public void notifyTrackUpdated() {
			RouteManager.this
					.notifyRouteUpdated(getListenersFor(ListenerDataType.ROUTE_UPDATES));
		}

		@Override
		public void notifyWaypointUpdated() {
			RouteManager.this
					.notifyWaypointUpdated(getListenersFor(ListenerDataType.ROUTE_TRACKPOINT_UPDATES));
		}

		@Override
		public void notifyPointsUpdated() 
		{
			RouteManager.this.notifyPointsUpdated(
							true,
							0,
							0,
							getListenersFor(ListenerDataType.POINT_UPDATES),
							getListenersFor(ListenerDataType.SAMPLED_OUT_POINT_UPDATES));
		}

		@Override
		public void notifyPreferenceChanged(String key) {
			RouteManager.this.notifyPreferenceChanged(key);
		}

		@Override
		public void notifyLocationProviderEnabled(boolean enabled) {
			hasProviderEnabled = enabled;
			RouteManager.this.notifyFixType();
		}

		@Override
		public void notifyLocationProviderAvailable(boolean available) {
			hasFix = available;
			RouteManager.this.notifyFixType();
		}

		@Override
		public void notifyLocationChanged(Location loc) {
			RouteManager.this.notifyLocationChanged(loc,
					getListenersFor(ListenerDataType.LOCATION_UPDATES));
		}

		@Override
		public void notifyHeadingChanged(float heading) {
			lastSeenMagneticHeading = heading;
			maybeUpdateDeclination();
			RouteManager.this
					.notifyHeadingChanged(getListenersFor(ListenerDataType.COMPASS_UPDATES));
		}
	}

	// Application services
	private final Context context;
	private final MyRouteProvider providerUtils;
	private final SharedPreferences preferences;

	// Get content notifications on the main thread, send listener callbacks in
	// another.
	// This ensures listener calls are serialized.
	private HandlerThread listenerHandlerThread;
	private Handler listenerHandler;

	/** Manager for external listeners (those from activities). */
	private final RouteDataListeners routeDataListeners;

	/** Wrapper for interacting with system data managers. */
	private IDbManagerDataSource dataSources;

	/** Manager for system data listener registrations. */
	private MapManagerDataSourceListener dataSourceManager;

	/** Condensed listener for system data listener events. */
	private final DataSourceListener dataManagerSourceListener = new ManagerDataSourceListener();

	// Cached preference values
	private int minRequiredAccuracy;
	private boolean useMetricUnits;
	private boolean reportSpeed;

	// Cached sensor readings
	private float declination;
	private long lastDeclinationUpdate;
	private float lastSeenMagneticHeading;

	// Cached GPS readings
	private Location lastSeenLocation;
	private boolean hasProviderEnabled = true;
	private boolean hasFix;
	private boolean hasGoodFix;

	// Transient state about the selected track
	private long selectedRouteId;
	private long firstSeenLocationId;
	private long lastSeenLocationId;
	private int numLoadedPoints;
	private int lastSamplingFrequency;
	private DoubleLastLocations locationFactory;

	private boolean started = false;

	// constructors
	public RouteManager(Context context, RouteDataListeners routeDataListeners,
			SharedPreferences sharedPreference, MyRouteProvider provider,
			int targetDisplayedTrackPoints) {
		this.context = context;
		this.routeDataListeners = routeDataListeners;
		this.preferences = sharedPreference;
		this.providerUtils = provider;
		this.thresholNumberPoints = targetDisplayedTrackPoints;
		this.locationFactory = new DoubleLastLocations();
		this.SELECTED_ROUTE_KEY = context
				.getString(R.string.selected_route_key);
		this.RECORDING_ROUTE_KEY = context
				.getString(R.string.recording_route_key);
		this.MIN_REQUIRED_ACCURACY_KEY = context
				.getString(R.string.min_required_accuracy_key);
		this.METRIC_UNITS_KEY = context.getString(R.string.metric_units_key);
		this.SPEED_REPORTING_KEY = context.getString(R.string.report_speed_key);
		resetData();
	}

	public synchronized static RouteManager newInstance(Context context) {
		SharedPreferences sharedPreference = context.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		MyRouteProvider provider = MyRouteProvider.Factory
				.getRouteProvider(context);
		return new RouteManager(context, new RouteDataListeners(),
				sharedPreference, provider, TARGET_DISPLAYED_ROUTE_POINTS);
	}

	/**
	 * starts listening to database and other sources changes and when a change
	 * occurs it notifies the listeners - 2 map activities
	 */
	public void startTracking() {
		if (isStarted()) {
			Log.d(Constants.TAG, "RouteManager:Already Started");
		}
		started = true;
		listenerHandlerThread = new HandlerThread("routeDataMangerThread");
		listenerHandlerThread.start();
		listenerHandler = new Handler(listenerHandlerThread.getLooper());
		dataSourceManager.updateAllListeners(getLastUpdatedListenerTypes());
		loadSharedDataPreferences();
	}

	private void loadSharedDataPreferences() {
		selectedRouteId = preferences.getLong(SELECTED_ROUTE_KEY, -1);
		useMetricUnits = preferences.getBoolean(METRIC_UNITS_KEY, true);
		reportSpeed = preferences.getBoolean(SPEED_REPORTING_KEY, true);
		minRequiredAccuracy = preferences.getInt(MIN_REQUIRED_ACCURACY_KEY,
				DEFAULT_MIN_REQUIRED_ACCURACY);

	}

	private EnumSet<ListenerDataType> getLastUpdatedListenerTypes() {

		return null;
	}

	public boolean isStarted() {
		return started;
	}

	public void resetData() {
		this.firstSeenLocationId = -1;
		this.lastSeenLocationId = -1;
		this.numLoadedPoints = 0;
		this.lastSamplingFrequency = -1;
	}

	public Object getListenersFor(ListenerDataType trackUpdates) {
		// TODO Auto-generated method stub
		return null;
	}

	public void notifyRouteUpdated(Object listenersFor) {
		// TODO Auto-generated method stub

	}

	public void notifyHeadingChanged(Object listenersFor) {
		// TODO Auto-generated method stub

	}

	public void notifyLocationChanged(Location loc, Object listenersFor) {
		// TODO Auto-generated method stub

	}

	public void notifyFixType() {
		// TODO Auto-generated method stub

	}

	public void notifyPreferenceChanged(String key) {
		// TODO Auto-generated method stub

	}

	public void notifyPointsUpdated(boolean b, int i, int j,
			Object listenersFor, Object listenersFor2) {
		// TODO Auto-generated method stub

	}

	public void notifyWaypointUpdated(Object listenersFor) {
		// TODO Auto-generated method stub

	}

	/** Updates known magnetic declination if needed. */
	private void maybeUpdateDeclination() {
		if (lastSeenLocation == null) {
			// We still don't know where we are.
			return;
		}

		// Update the declination every hour
		long now = System.currentTimeMillis();
		if (now - lastDeclinationUpdate < 60 * 60 * 1000) {
			return;
		}

		lastDeclinationUpdate = now;
		long timestamp = lastSeenLocation.getTime();
		if (timestamp == 0) {
			// Hack for Samsung phones which don't populate the time field
			timestamp = now;
		}

		declination = getDeclinationFor(lastSeenLocation, timestamp);
		Log.i(TAG, "Updated magnetic declination to " + declination);
	}
    /**
     * 
     * @param location where the declination occurs
     * @param timestamp - Time at which to evaluate the declination, 
     * in milliseconds since January 1, 1970. 
     * (approximate is fine -- the declination changes very slowly). 
     * @return The declination of the horizontal component of the magnetic field from true north, 
     * in degrees 
     * (i.e. positive means the magnetic field is rotated east that much from true north). 
     */
	protected float getDeclinationFor(Location location, long timestamp) {
		GeomagneticField field = new GeomagneticField(
				(float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(), timestamp);
		return field.getDeclination();
	}
    /**
     * garbace collecting...
     */
	@Override
	protected void finalize() throws Throwable {
		if (isStarted()
				|| (listenerHandlerThread != null && listenerHandlerThread
						.isAlive())) {
			Log.e(TAG, "Forgot to stop() TrackDataHub");
		}

		super.finalize();
	}
	/**
	 * stops the listening of the manager
	 */
    public void stopListening()
    {
    	Log.i(TAG, "RouteManger:stop the listening");
    	if(!isStarted())
    	{
    		Log.d(TAG, "RouteManager:already stopped");
    	}
    	started = false;
    	dataSourceManager.unregisterAllListeners();
    	//quites the looper that sends updates to the map listeners
        listenerHandlerThread.getLooper().quit();
        dataSources = null;
        dataSourceManager  = null;
        //dataManagerSourceListener = null;
        listenerHandler = null;
        listenerHandlerThread = null;
    }
	protected void runInListenerThread(Runnable runnable) {
		if (listenerHandler == null) {
			// Use a Throwable to ensure the stack trace is logged.
			Log.e(TAG, "Tried to use listener thread before start()",
					new Throwable());
			return;
		}
		listenerHandler.post(runnable);
	}
}

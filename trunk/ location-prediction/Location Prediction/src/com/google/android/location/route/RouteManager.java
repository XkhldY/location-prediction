package com.google.android.location.route;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.google.android.lib.content.Route;
import com.google.android.lib.content.RouteTrackPoint;
import com.google.android.lib.content.data.DoubleLastLocationsFactory;
import com.google.android.lib.content.data.MyRouteProvider;
import com.google.android.lib.content.data.MyRouteProvider.LocationIterator;
import com.google.android.location.content.Constants;
import com.google.android.location.content.R;
import com.google.android.location.route.MapManagerDataSourceListener.DataSourceListener;
import com.google.android.location.route.RouteDataListeners.ListenerRegistration;
import com.google.android.location.route.RouteManager.ListenerDataType;
import com.google.android.location.route.RouteMapDataListener.MyGpsProviderState;
import com.google.android.maps.MyMapActivity;
import com.google.android.utilities.LocationFilter;

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
	private class ManagerDataSourceListener implements	DataSourceListener {
		private boolean hasProviderEnabled;
		private boolean hasFix;
		private float lastSeenMagneticHeading;

		@Override
		public void notifyRouteUpdated() {
			RouteManager.this
					.notifyRouteUpdated(getListenersFor(ListenerDataType.ROUTE_UPDATES));
		}

		@Override
		public void notifyRouteTrackPointUpdated() {
			RouteManager.this
					.notifyRouteTrackPoints(getListenersFor(ListenerDataType.ROUTE_TRACKPOINT_UPDATES));
		}

		@Override
		public void notifyPointsUpdated() {
			RouteManager.this
					.notifyPointsUpdated(
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
	private DoubleLastLocationsFactory locationFactory;

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
		this.locationFactory = new DoubleLastLocationsFactory();
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
		dataSourceManager = new MapManagerDataSourceListener(dataManagerSourceListener, dataSources);
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

		EnumSet<ListenerDataType> neededTypes = routeDataListeners
				.getAllRegisteredTypes();

		// We always want preference updates.
		neededTypes.add(ListenerDataType.DISPLAY_PREFERENCES);

		return neededTypes;
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

	public Set<RouteMapDataListener> getListenersFor(
			ListenerDataType trackUpdates) {
		synchronized (routeDataListeners) {
			return routeDataListeners.getListenersFor(trackUpdates);
		}
	}

	public void notifyRouteUpdated(Object listenersFor) {
		// TODO Auto-generated method stub

	}

	public void notifyHeadingChanged(float heading) {
		 lastSeenMagneticHeading = heading;
	      maybeUpdateDeclination();
         RouteManager.this.notifyHeadingChanged(getListenersFor(ListenerDataType.COMPASS_UPDATES));
	}
	 /**
	   * Notifies that the current heading has changed.
	   *
	   * @param listeners the listeners to notify
	   */
	  private void notifyHeadingChanged(final Set<RouteMapDataListener> listeners) {
	    if (listeners.isEmpty()) return;

	    runInListenerThread(new Runnable() {
	      @Override
	      public void run() {
	        float heading = lastSeenMagneticHeading + declination;
	        for (RouteMapDataListener listener : listeners) {
	          listener.onCurrentHeadingChanged(heading);
	        }
	      }
	    });
	  }


	 /** Notifies about the current GPS fix state. */
	  private void notifyFixType() {
	    final RouteMapDataListener.MyGpsProviderState state;
	    if (!hasProviderEnabled) {
	      state = MyGpsProviderState.DISABLED;
	    } else if (!hasFix) {
	      state = MyGpsProviderState.NO_FIX;
	    } else if (!hasGoodFix) {
	      state = MyGpsProviderState.BAD_FIX;
	    } else {
	      state = MyGpsProviderState.GOOD_FIX;
	    }

	    runInListenerThread(new Runnable() {
	      @Override
	      public void run() {
	        // Notify to everyone.
	        Log.d(TAG, "Notifying fix type: " + state);
	        for (RouteMapDataListener listener :
	             getListenersFor(ListenerDataType.LOCATION_UPDATES)) {
	          listener.onGPSProviderStateChange(state);
	        }
	      }
	    });
	  }


	public void notifyPreferenceChanged(String key) {
		if (MIN_REQUIRED_ACCURACY_KEY.equals(key)) {
			minRequiredAccuracy = preferences.getInt(MIN_REQUIRED_ACCURACY_KEY,
					DEFAULT_MIN_REQUIRED_ACCURACY);
		} else if (METRIC_UNITS_KEY.equals(key)) {
			useMetricUnits = preferences.getBoolean(METRIC_UNITS_KEY, true);
			notifyUnitsChanged();
		} else if (SPEED_REPORTING_KEY.equals(key)) {
			reportSpeed = preferences.getBoolean(SPEED_REPORTING_KEY, true);
			notifySpeedReportingChanged();
		} else if (SELECTED_ROUTE_KEY.equals(key)) {
			long routeId = preferences.getLong(SELECTED_ROUTE_KEY, -1);
			loadRoute(routeId);
		}

	}

	/** Called when the metric units setting changes. */
	  private void notifyUnitsChanged() {
	    if (!isStarted()) return;

	    runInListenerThread(new Runnable() {
	      @Override
	      public void run() {
	        Set<RouteMapDataListener> displayListeners = getListenersFor(ListenerDataType.DISPLAY_PREFERENCES);

	        for (RouteMapDataListener listener : displayListeners) {
	          if (listener.onUnitsChanged(useMetricUnits)) {
	            synchronized (routeDataListeners) {
	              reloadDataForListener(listener);
	            }
	          }
	        }
	      }
	    });
	  }

	/** Called when the speed/pace reporting preference changes. */
	private void notifySpeedReportingChanged() {
		if (!isStarted())
			return;

		runInListenerThread(new Runnable() {
			@Override
			public void run() {
				Set<RouteMapDataListener> displayListeners = getListenersFor(ListenerDataType.DISPLAY_PREFERENCES);

				for (RouteMapDataListener listener : displayListeners) {
					// TODO: Do the reloading just once for all interested
					// listeners
					if (listener.onReportSpeedChanged(reportSpeed)) {
						synchronized (routeDataListeners) {
							reloadDataForListener(listener);
						}
					}
				}
			}
		});
	}
	/**
	   * Forces the current location to be updated and reported to all listeners.
	   * The reported location may be from the network provider if the GPS provider
	   * is not available or doesn't have a fix.
	   */
	  public void forceUpdateLocation() 
	  {
	    if (!isStarted()) {
	      Log.w(TAG, "Not started, not forcing location update");
	      return;
	    }
	    Log.i(TAG, "Forcing location update");

	    Location loc = dataSources.getLastKnownLocation();
	    if (loc != null)
	    {
	      notifyLocationChanged(loc, getListenersFor(ListenerDataType.LOCATION_UPDATES));
	    }
	  }

	  /**
	   * Notifies the the current location has changed, without any filtering.
	   * If the state of GPS fix has changed, that will also be reported.
	   *
	   * @param location the current location
	   * @param listeners the listeners to notify
	   */
	  private void notifyLocationChanged(Location location, Set<RouteMapDataListener> listeners)
	  {
	    notifyLocationChanged(location, false, listeners);
	  }
	/**
	 * Unloads the currently-selected route.
	 */
	public void unloadCurrentRoute() {
		loadRoute(-1);
	}
	/**
	   * Loads the given route and makes it the currently-selected one.
	   * It is ok to call this method before {@link #start}, and in that case
	   * the data will only be passed to listeners when {@link #start} is called.
	   *
	   * @param routeId the ID of the route to load
	   */
	public void loadRoute(long routeId) 
	{
	    if (routeId == selectedRouteId) 
	    {
	      Log.w(TAG, "Not reloading route, id=" + routeId);
	      return;
	    }

	    // Save the selection to memory and flush.
	    selectedRouteId = routeId;
	    Editor editor = preferences.edit();
	    editor.putLong(SELECTED_ROUTE_KEY, routeId);
	    editor.commit();
	   

	    // Force it to reload data from the beginning.
	    Log.d(TAG, "Loading track");
	    resetData();

	    loadDataForAllListeners();
	}
	/**
	   * Reloads all track data received so far into the specified listeners.
	   */
	  private void loadDataForAllListeners() 
	  {
	    if (!isStarted()) 
	    {
	      Log.w(TAG, "Not started, not reloading");
	      return;
	    }
	    //when starting we have no listeners only listenersDataTypes which dont require or 
	    //dont need a listener like mapactivity 
	    synchronized (routeDataListeners) 
	    {
	      if (!routeDataListeners.hasListeners()) 
	      {
	        Log.d(TAG, "No listeners, not reloading");
	        return;
	      }
	    }

	    runInListenerThread(new Runnable() 
	    {
	      @Override
	      public void run()
	      {
	        // Ignore the return values here, we're already sending the full data set anyway
	        for (RouteMapDataListener listener : getListenersFor(ListenerDataType.DISPLAY_PREFERENCES)) 
	        {
	          listener.onUnitsChanged(useMetricUnits);
	          listener.onReportSpeedChanged(reportSpeed);
	        }

	        notifySelectedRouteChanged(selectedRouteId, getListenersFor(ListenerDataType.SELECTED_ROUTE_CHANGED));

	        notifyRouteUpdated(getListenersFor(ListenerDataType.ROUTE_UPDATES));

	        Set<RouteMapDataListener> pointListeners =
	            getListenersFor(ListenerDataType.POINT_UPDATES);
	        Set<RouteMapDataListener> sampledOutPointListeners = getListenersFor(ListenerDataType.SAMPLED_OUT_POINT_UPDATES);
	        notifyPointsCleared(pointListeners);
	        notifyPointsUpdated(true, 0, 0, pointListeners, sampledOutPointListeners);

	        notifyRouteTrackPoints(getListenersFor(ListenerDataType.ROUTE_TRACKPOINT_UPDATES));

	        if (lastSeenLocation != null) 
	        {
	          notifyLocationChanged(lastSeenLocation, true,
	              getListenersFor(ListenerDataType.LOCATION_UPDATES));
	        } 
	        else 
	        {
	          notifyFixType();
	        }

	        notifyHeadingChanged(getListenersFor(ListenerDataType.COMPASS_UPDATES));
	      }
	    });
	  }
	  /**
	   * Notifies that route track points have been updated.
	   * We assume few route track points, so we reload them all every time.
	   *
	   * @param listeners the listeners to notify
	   */
	  private void notifyRouteTrackPoints(final Set<RouteMapDataListener> listeners)
	  {
	    if (listeners.isEmpty()) return;

	    // Always reload all the route track points.
	    final Cursor cursor = providerUtils.getRouteTrackPointsCursor(selectedRouteId, 0L, MAX_DISPLAYED_ROUTE_TRACK_POINTS);

	    runInListenerThread(new Runnable() {
	      @Override
	      public void run() {
	        Log.d(TAG, "Reloading waypoints");
	        for (RouteMapDataListener listener : listeners) 
	        {
	          listener.clearRouteTrackPoints();
	        }

	        try 
	        {
	          if (cursor != null && cursor.moveToFirst()) 
	          {
	            do 
	            {
	              RouteTrackPoint rtpoint = providerUtils.createRouteTrackPoint(cursor);
	              if (!LocationFilter.isValidLocation(rtpoint.getLocation())) 
	              {
	                continue;
	              }

	              for (RouteMapDataListener listener : listeners) 
	              {
	                listener.onNewRouteTrackPoint(rtpoint);
	              }
	            } while (cursor.moveToNext());
	          }
	        } 
	        finally 
	        {
	          if (cursor != null) 
	          {
	            cursor.close();
	          }
	        }

	        for (RouteMapDataListener listener : listeners) 
	        {
	          listener.onNewRouteTrackPointsDone();
	        }
	      }
	    });
	  }
	  /**
	   * Notifies that the current location has changed, without any filtering.
	   * If the state of GPS fix has changed, that will also be reported.
	   *
	   * @param location the current location
	   * @param forceUpdate whether to force the notifications to happen
	   * @param listeners the listeners to notify
	   */
	  private void notifyLocationChanged(Location location, boolean forceUpdate, final Set<RouteMapDataListener> listeners) 
	  {
	    if (location == null) return;
	    if (listeners.isEmpty()) return;

	    boolean isGpsLocation = location.getProvider().equals(LocationManager.GPS_PROVIDER);

	    boolean oldHasFix = hasFix;
	    boolean oldHasGoodFix = hasGoodFix;

	    long now = System.currentTimeMillis();
	    if (isGpsLocation) {
	      // We consider a good fix to be a recent one with reasonable accuracy.
	      hasFix = !isLocationOld(location, now, MAX_LOCATION_AGE_MS);
	      hasGoodFix = (location.getAccuracy() <= minRequiredAccuracy);
	    } 
	    else 
	    {
	      //GPS location not available
	      if (!isLocationOld(lastSeenLocation, now, MAX_LOCATION_AGE_MS)) 
	      {
	        // This is a network location, but we have a recent/valid GPS location, just ignore this.
	        return;
	      }

	      // We haven't gotten a GPS location in a while (or at all), assume we have no fix anymore.
	      hasFix = false;
	      hasGoodFix = false;

	      // If the network location is recent, we'll use that.
	      if (isLocationOld(location, now, MAX_NETWORK_AGE_MS)) 
	      {
	        // Alas, we have no clue where we are.
	        location = null;
	      }
	    }

	    if (hasFix != oldHasFix || hasGoodFix != oldHasGoodFix || forceUpdate) 
	    {
	      notifyFixType();
	    }

	    lastSeenLocation = location;
	    final Location finalLoc = location;
	    runInListenerThread(new Runnable() 
	    {
	      @Override
	      public void run() {
	        for (RouteMapDataListener listener : listeners) {
	          listener.onGPSCurrentLocationChanged(finalLoc);
	        }
	      }
	    });
	  }
	  /**
	   * Returns true if the given location is either invalid or too old.
	   *
	   * @param location the location to test
	   * @param now the current timestamp in milliseconds
	   * @param maxAge the maximum age in milliseconds
	   * @return true if it's invalid or too old, false otherwise
	   */
	  private static boolean isLocationOld(Location location, long now, long maxAge) {
	    return !LocationFilter.isValidLocation(location) || now - location.getTime() > maxAge;
	  }
	/**
	   * Tells listeners to clear the current list of points.
	   *
	   * @param listeners the listeners to  notify
	   */
	  private void notifyPointsCleared(final Set<RouteMapDataListener> listeners) 
	  {
	    if (listeners.isEmpty()) return;

	    runInListenerThread(new Runnable() 
	    {
	      @Override
	      public void run() 
	      {
	        for (RouteMapDataListener listener : listeners) 
	        {
	          listener.clearRoutePoints();
	        }
	      }
	    });
	  }


	/**
	   * Notifies that a new track has been selected..
	   *
	   * @param trackId the new selected track
	   * @param listeners the listeners to notify
	   */
	  private void notifySelectedRouteChanged(long routeId,
	      final Set<RouteMapDataListener> listeners) {
	    if (listeners.isEmpty()) return;

	    Log.i(TAG, "New route selected, id=" + routeId);
	    final Route route = providerUtils.getRouteById(routeId);

	    runInListenerThread(new Runnable() {
	      @Override
	      public void run() {
	        for (RouteMapDataListener listener : listeners) {
	          listener.onSelectedRouteChanged(route, isRecordingSelected());
	        }
	      }
	    });
	  }

	public boolean isRecordingSelected() 
	{
		if (!isStarted()) 
	    {
	      loadSharedDataPreferences();
	    }
	    long recordingRouteId = preferences.getLong(RECORDING_ROUTE_KEY, -1);
	    return recordingRouteId > 0 && recordingRouteId == selectedRouteId;
	}

	

	public void unregisterTrackDataListener(RouteMapDataListener listener) {
		synchronized (routeDataListeners) {
			routeDataListeners.unregisterTrackDataListener(listener);

			// Don't load any data or start internal listeners if start() hasn't
			// been
			// called. When it is called, we'll do both things.
			if (!isStarted())
				return;

			dataSourceManager.updateAllListeners(getLastUpdatedListenerTypes());
		}
	}

	/**
	 * Reloads all route data received so far into the specified listeners.
	 */
	public void reloadDataForListener(RouteMapDataListener listener) {
		ListenerRegistration registration;
		synchronized (routeDataListeners) {
			registration = routeDataListeners.getRegistration(listener);
			registration.resetState();
			loadNewDataForListener(registration);
		}
	}

	/**
	   * Reloads all track data received so far into the specified listeners.
	   *
	   * Assumes it's called from a block that synchronises on {@link #trackDataListeners}.
	   */
	  private void loadNewDataForListener(final ListenerRegistration registration) 
	  {
	    if (!isStarted()) {
	      Log.w(TAG, "Not started, not reloading");
	      return;
	    }
	    if (registration == null) {
	      Log.w(TAG, "Not reloading for null registration");
	      return;
	    }

	    // If a listener happens to be added after this method but before the Runnable below is
	    // executed, it will have triggered a separate call to load data only up to the point this
	    // listener got to. This is ensured by being synchronized on listeners.
	    final boolean isOnlyListener = (routeDataListeners.getNumListeners() == 1);

	    runInListenerThread(new Runnable() 
	    {
	      @SuppressWarnings("unchecked")
	      @Override
	      public void run() 
	      {
	        // Reload everything if either it's a different track, or the track has been resampled
	        // (this also covers the case of a new registration).
	        boolean reloadAll = registration.lastRouteId != selectedRouteId ||
	                            registration.lastSamplingFrequency != lastSamplingFrequency;
	        Log.d(TAG, "Doing a " + (reloadAll ? "full" : "partial") + " reload for " + registration);

	        RouteMapDataListener listener = registration.listener;
	        Set<RouteMapDataListener> listenerSet = Collections.singleton(listener);

	        if (registration.isInterestedIn(ListenerDataType.DISPLAY_PREFERENCES)) 
	        {
	          reloadAll |= listener.onUnitsChanged(useMetricUnits);
	          reloadAll |= listener.onReportSpeedChanged(reportSpeed);
	        }

	        if (reloadAll && registration.isInterestedIn(ListenerDataType.SELECTED_ROUTE_CHANGED)) 
	        {
	          notifySelectedRouteChanged(selectedRouteId, listenerSet);
	        }

	        if (registration.isInterestedIn(ListenerDataType.ROUTE_UPDATES)) 
	        {
	          notifyRouteUpdated(listenerSet);
	        }

	        boolean interestedInPoints = registration.isInterestedIn(ListenerDataType.POINT_UPDATES);
	        boolean interestedInSampledOutPoints = registration.isInterestedIn(ListenerDataType.SAMPLED_OUT_POINT_UPDATES);
	        if (interestedInPoints || interestedInSampledOutPoints) 
	        {
	          long minPointId = 0;
	          int previousNumPoints = 0;
	          if (reloadAll) 
	          {
	            // Clear existing points and send them all again
	            notifyPointsCleared(listenerSet);
	          } 
	          else 
	          {
	            // Send only new points
	            minPointId = registration.lastPointId + 1;
	            previousNumPoints = registration.numLoadedPoints;
	          }

	          // If this is the only listener we have registered, keep the state that we serve to it as
	          // a reference for other future listeners.
	          if (isOnlyListener && reloadAll) 
	          {
	            resetData();
	          }

	          notifyPointsUpdated(isOnlyListener,
	              minPointId,
	              previousNumPoints,
	              listenerSet,
	              interestedInSampledOutPoints ? listenerSet : Collections.EMPTY_SET);
	        }

	        if (registration.isInterestedIn(ListenerDataType.ROUTE_TRACKPOINT_UPDATES)) 
	        {
	          notifyRouteTrackPoints(listenerSet);
	        }

	        if (registration.isInterestedIn(ListenerDataType.LOCATION_UPDATES)) 
	        {
	          if (lastSeenLocation != null) 
	          {
	            notifyLocationChanged(lastSeenLocation, true, listenerSet);
	          } 
	          else 
	          {
	            notifyFixType();
	          }
	        }

	        if (registration.isInterestedIn(ListenerDataType.COMPASS_UPDATES)) 
	        {
	          notifyHeadingChanged(listenerSet);
	        }
	      }
	    });
	  }
	/**
	   * Notifies the given listeners about track points in the given ID range.
	   *
	   * @param keepState whether to load and save state about the already-notified points.
	   *        If true, only new points are reported.
	   *        If false, then the whole track will be loaded, without affecting the state.
	   * @param minPointId the first point ID to notify, inclusive, or 0 to determine from
	   *        internal state
	   * @param previousNumPoints the number of points to assume were previously loaded for
	   *        these listeners, or 0 to assume it's the kept state
	   */
	  private void notifyPointsUpdated(final boolean keepState,
	      final long minPointId, final int previousNumPoints,
	      final Set<RouteMapDataListener> sampledListeners,
	      final Set<RouteMapDataListener> sampledOutListeners) 
	  {
	    if (sampledListeners.isEmpty() && sampledOutListeners.isEmpty()) 
	      return;
	    runInListenerThread(new Runnable() 
	    {
	      @Override
	      public void run() 
	      {
	        notifyPointsUpdatedSync(keepState, minPointId, previousNumPoints, sampledListeners, sampledOutListeners);
	      }
	    });
	  }

	  /**
	   * Synchronous version of the above method.
	   */
	  private void notifyPointsUpdatedSync(boolean keepState,
	      long minPointId, int previousNumPoints,
	      Set<RouteMapDataListener> sampledListeners,
	      Set<RouteMapDataListener> sampledOutListeners) {
	    // If we're loading state, start from after the last seen point up to the last recorded one
	    // (all new points)
	    // If we're not loading state, then notify about all the previously-seen points.
	    if (minPointId <= 0) 
	    {
	      minPointId = keepState ? lastSeenLocationId + 1 : 0;
	    }
	    long maxPointId = keepState ? -1 : lastSeenLocationId;

	    // TODO: Move (re)sampling to a separate class.
	    if (numLoadedPoints >= TARGET_DISPLAYED_ROUTE_POINTS) 
	    {
	      // We're about to exceed the maximum desired number of points, so reload
	      // the whole track with fewer points (the sampling frequency will be
	      // lower). We do this for every listener even if we were loading just for
	      // a few of them (why miss the opportunity?).

	      Log.i(TAG, "Resampling point set after " + numLoadedPoints + " points.");
	      resetData();
	      synchronized (routeDataListeners) 
	      {
	        sampledListeners = getListenersFor(ListenerDataType.POINT_UPDATES);
	        sampledOutListeners = getListenersFor(ListenerDataType.SAMPLED_OUT_POINT_UPDATES);
	      }
	      maxPointId = -1;
	      minPointId = 0;
	      previousNumPoints = 0;
	      keepState = true;

	      for (RouteMapDataListener listener : sampledListeners)
	      {
	        listener.clearRoutePoints();
	      }
	    }

	    // Keep the originally selected track ID so we can stop if it changes.
	    long currentSelectedRouteId = selectedRouteId;

	    // If we're ignoring state, start from the beginning of the track
	    int localNumLoadedPoints = previousNumPoints;
	    if (previousNumPoints <= 0) 
	    {
	      localNumLoadedPoints = keepState ? numLoadedPoints : 0;
	    }
	    long localFirstSeenLocationId = keepState ? firstSeenLocationId : -1;
	    long localLastSeenLocationId = minPointId;
	    long lastStoredLocationId = providerUtils.getLastLocationId(currentSelectedRouteId);
	    int pointSamplingFrequency = -1;

	    LocationIterator it = providerUtils.getLocationIterator(currentSelectedRouteId, minPointId, false, locationFactory);

	    while (it.hasNext()) 
	    {
	      if (currentSelectedRouteId != selectedRouteId) 
	      {
	        // The selected track changed beneath us, stop.
	        break;
	      }

	      Location location = it.next();
	      long locationId = it.getLocationid();

	      // If past the last wanted point, stop.
	      // This happens when adding a new listener after data has already been loaded,
	      // in which case we only want to bring that listener up to the point where the others
	      // were. In case it does happen, we should be wasting few points (only the ones not
	      // yet notified to other listeners).
	      if (maxPointId > 0 && locationId > maxPointId) 
	      {
	        break;
	      }

	      if (localFirstSeenLocationId == -1) 
	      {
	        // This was our first point, keep its ID
	        localFirstSeenLocationId = locationId;
	      }

	      if (pointSamplingFrequency == -1) 
	      {
	        // Now we already have at least one point, calculate the sampling
	        // frequency.
	        // It should be noted that a non-obvious consequence of this sampling is that
	        // no matter how many points we get in the newest batch, we'll never exceed
	        // MAX_DISPLAYED_ROUTE_POINTS = 2 * TARGET_DISPLAYED_ROUTE_POINTS before resampling.
	        long numTotalPoints = lastStoredLocationId - localFirstSeenLocationId;
	        numTotalPoints = Math.max(0L, numTotalPoints);
	        pointSamplingFrequency = (int) (1 + numTotalPoints / TARGET_DISPLAYED_ROUTE_POINTS);
	      }

	      notifyNewPoint(location, locationId, lastStoredLocationId,
	          localNumLoadedPoints, pointSamplingFrequency, sampledListeners, sampledOutListeners);

	      localNumLoadedPoints++;
	      localLastSeenLocationId = locationId;
	    }
	    it.close();

	    if (keepState) 
	    {
	      numLoadedPoints = localNumLoadedPoints;
	      firstSeenLocationId = localFirstSeenLocationId;
	      lastSeenLocationId = localLastSeenLocationId;
	    }

	    // Always keep the sampling frequency - if it changes we'll do a full reload above anyway.
	    lastSamplingFrequency = pointSamplingFrequency;

	    for (RouteMapDataListener listener : sampledListeners)
	    {
	      listener.onNewRoutePointsDone();

	      // Update the listener state
	      ListenerRegistration registration = routeDataListeners.getRegistration(listener);
	      if (registration != null) {
	        registration.lastRouteId = currentSelectedRouteId;
	        registration.lastPointId = localLastSeenLocationId;
	        registration.lastSamplingFrequency = pointSamplingFrequency;
	        registration.numLoadedPoints = localNumLoadedPoints;
	      }
	    }
	  }

	private void notifyNewPoint(Location location, long locationId,
			long lastStoredLocationId, int loadedPoints,
			int pointSamplingFrequency,
			Set<RouteMapDataListener> sampledListeners,
			Set<RouteMapDataListener> sampledOutListeners) {
		boolean isValid = LocationFilter.isValidLocation(location);
	    if (!isValid) 
	    {
	      // Invalid points are segment splits - report those separately.
	      // TODO: Always send last valid point before and first valid point after a split
	      for (RouteMapDataListener listener : sampledListeners) 
	      {
	        listener.onSegmentSplit();
	      }
	      return;
	    }

	    // Include a point if it fits one of the following criteria:
	    // - Has the mod for the sampling frequency (includes first point).
	    // - Is the last point and we are not recording this track.
	    boolean recordingSelected = isRecordingSelected();
	    boolean includeInSample = 
	                             (loadedPoints % pointSamplingFrequency == 0 ||(!recordingSelected && locationId == lastStoredLocationId));

	    if (!includeInSample) 
	    {
	      for (RouteMapDataListener listener : sampledOutListeners) 
	      {
	        listener.onSampledOutRoutePoint(location);
	      }
	    } else 
	    {
	      // Point is valid and included in sample.
	      for (RouteMapDataListener listener : sampledListeners) 
	      {
	        // No need to allocate a new location (we can safely reuse the existing).
	        listener.onNewRoutePoint(location);
	      }
	    }
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
	 * @param location
	 *            where the declination occurs
	 * @param timestamp
	 *            - Time at which to evaluate the declination, in milliseconds
	 *            since January 1, 1970. (approximate is fine -- the declination
	 *            changes very slowly).
	 * @return The declination of the horizontal component of the magnetic field
	 *         from true north, in degrees (i.e. positive means the magnetic
	 *         field is rotated east that much from true north).
	 */
	protected float getDeclinationFor(Location location, long timestamp) {
		GeomagneticField field = new GeomagneticField(
				(float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(), timestamp);
		return field.getDeclination();
	}

	/**
	 * garbage collecting...
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
	public void stopListening() {
		Log.i(TAG, "RouteManger:stop the listening");
		if (!isStarted()) {
			Log.d(TAG, "RouteManager:already stopped");
		}
		started = false;
		dataSourceManager.unregisterAllListeners();
		// quits the looper that sends updates to the map listeners
		listenerHandlerThread.getLooper().quit();
		dataSources = null;
		dataSourceManager = null;
		// dataManagerSourceListener = null;
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

	public void registerDataListener(RouteMapDataListener routeDataListener,
			EnumSet<ListenerDataType> listeners) {
		// TODO Auto-generated method stub
		
	}

	
}

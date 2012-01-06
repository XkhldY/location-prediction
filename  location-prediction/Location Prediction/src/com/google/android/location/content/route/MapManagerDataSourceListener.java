package com.google.android.location.route;

import java.util.EnumSet;
import java.util.Set;

import com.google.android.lib.content.Route;
import com.google.android.lib.content.RoutesColumns;
import com.google.android.lib.content.RoutesPointsLocations;
import com.google.android.lib.content.RoutesTrackPointsColumns;
import com.google.android.location.content.Constants;
import com.google.android.location.route.RouteManager.ListenerDataType;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;





/**
 * External data source manager, which converts system-level events into My Tracks data events.
 *
 * @author Andrei
 */
class MapManagerDataSourceListener
{

  /** Single interface for receiving system events that were registered for. */
  interface DataSourceListener {
    void notifyTrackUpdated();
    void notifyWaypointUpdated();
    void notifyPointsUpdated();
    void notifyPreferenceChanged(String key);
    void notifyLocationProviderEnabled(boolean enabled);
    void notifyLocationProviderAvailable(boolean available);
    void notifyLocationChanged(Location loc);
    void notifyHeadingChanged(float heading);
  }

  private final DataSourceListener listener;

  /** Observer for when the tracks table is updated. */
  private class RouteObserver extends ContentObserver {
    public RouteObserver() {
      super(contentHandler);
    }

    @Override
    public void onChange(boolean selfChange) {
      listener.notifyTrackUpdated();
    }
  }

  /** Observer for when the waypoints table is updated. */
  private class RouteTrackPointObserver extends ContentObserver {
    public RouteTrackPointObserver() {
      super(contentHandler);
    }

    @Override
    public void onChange(boolean selfChange) {
      listener.notifyWaypointUpdated();
    }
  }

  /** Observer for when the points table is updated. */
  private class PointObserver extends ContentObserver {
    public PointObserver() {
      super(contentHandler);
    }

    @Override
    public void onChange(boolean selfChange) {
      listener.notifyPointsUpdated();
    }
  }

  /** Listener for when preferences change. */
  private class HubSharedPreferenceListener implements OnSharedPreferenceChangeListener 
  {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
    {
      listener.notifyPreferenceChanged(key);
    }
  }

  /** Listener for the current location (independent from track data). */
  private class CurrentLocationListener implements LocationListener {
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      if (!LocationManager.GPS_PROVIDER.equals(provider)) return;

      listener.notifyLocationProviderAvailable(status == LocationProvider.AVAILABLE);
    }

    @Override
    public void onProviderEnabled(String provider) {
      if (!LocationManager.GPS_PROVIDER.equals(provider)) return;

      listener.notifyLocationProviderEnabled(true);
    }

    @Override
    public void onProviderDisabled(String provider) {
      if (!LocationManager.GPS_PROVIDER.equals(provider)) return;

      listener.notifyLocationProviderEnabled(false);
    }

    @Override
    public void onLocationChanged(Location location) {
      listener.notifyLocationChanged(location);
    }
  }

  /** Listener for compass readings. */
  private class CompassListener implements SensorEventListener 
  {
    @Override
    public void onSensorChanged(SensorEvent event) 
    {
      listener.notifyHeadingChanged(event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) 
    {
      // Do nothing
    }
  }

  /** Wrapper for registering internal listeners. */
  private final IDbManagerDataSource dataSources;

  // Internal listeners (to receive data from the system)
  private final Set<ListenerDataType> registeredNoneOfListeners = EnumSet.noneOf(ListenerDataType.class);
  private final Handler contentHandler;
  private final ContentObserver pointObserver;
  private final ContentObserver routeTrackPointObserver;
  private final ContentObserver routeObserver;
  private final LocationListener locationListener;
  private final OnSharedPreferenceChangeListener preferenceListener;
  private final SensorEventListener compassListener;

  MapManagerDataSourceListener(DataSourceListener listener, IDbManagerDataSource dataSources) {
    this.listener = listener;
    this.dataSources = dataSources;

    contentHandler = new Handler();
    pointObserver = new PointObserver();
    routeTrackPointObserver = new RouteTrackPointObserver();
    routeObserver = new RouteObserver();

    compassListener = new CompassListener();
    locationListener = new CurrentLocationListener();
    preferenceListener = new HubSharedPreferenceListener();
  }

  /** Updates the internal (sensor, position, etc) listeners. */
  void updateAllListeners(EnumSet<ListenerDataType> externallyNeededListeners) 
  {
    EnumSet<ListenerDataType> neededListeners = EnumSet.copyOf(externallyNeededListeners);

    // Special case - map sampled-out points type to points type since they
    // correspond to the same internal listener.
    if (neededListeners.contains(ListenerDataType.SAMPLED_OUT_POINT_UPDATES)) 
    {
      neededListeners.remove(ListenerDataType.SAMPLED_OUT_POINT_UPDATES);
      neededListeners.add(ListenerDataType.POINT_UPDATES);
    }

    Log.d(Constants.TAG, "Updating internal listeners to types " + neededListeners);

    // Unnecessary = registered - needed
    Set<ListenerDataType> unnecessaryListeners = EnumSet.copyOf(registeredNoneOfListeners);
    unnecessaryListeners.removeAll(neededListeners);

    // Missing = needed - registered
    Set<ListenerDataType> missingListeners = EnumSet.copyOf(neededListeners);
    missingListeners.removeAll(registeredNoneOfListeners);

    // Remove all unnecessary listeners.
    for (ListenerDataType type : unnecessaryListeners) {
      unregisterListener(type);
    }

    // Add all missing listeners.
    for (ListenerDataType type : missingListeners) {
      registerListener(type);
    }

    // Now all needed types are registered.
    registeredNoneOfListeners.clear();
    registeredNoneOfListeners.addAll(neededListeners);
  }

  private void registerListener(ListenerDataType type) {
    switch (type) {
      case COMPASS_UPDATES: 
      {
        // Listen to compass
        Sensor compass = dataSources.getSensor(Sensor.TYPE_ORIENTATION);
        if (compass != null) 
        {
          Log.d(Constants.TAG, "TrackDataHub: Now registering sensor listener.");
          dataSources.registerSensorListener(compassListener, compass, SensorManager.SENSOR_DELAY_UI);
        }
        break;
      }
      case LOCATION_UPDATES:
        dataSources.requestLocationUpdates(locationListener);
        break;
      case POINT_UPDATES:
        dataSources.registerContentObserver(RoutesPointsLocations.CONTENT_URI, false, pointObserver);
        break;
      case ROUTE_UPDATES:
        dataSources.registerContentObserver(RoutesColumns.CONTENT_URI, false, routeObserver);
        break;
      case ROUTE_TRACKPOINT_UPDATES:
        dataSources.registerContentObserver(RoutesTrackPointsColumns.CONTENT_URI, false, routeTrackPointObserver);
        break;
      case DISPLAY_PREFERENCES:
        dataSources.registerOnSharedPreferenceChangeListener(preferenceListener);
        break;
      case SAMPLED_OUT_POINT_UPDATES:
        throw new IllegalArgumentException("Should have been mapped to point updates");
    }
  }

  private void unregisterListener(ListenerDataType type) {
    switch (type) {
      case COMPASS_UPDATES:
        dataSources.unregisterSensorListener(compassListener);
        break;
      case LOCATION_UPDATES:
        dataSources.removeLocationUpdates(locationListener);
        break;
      case POINT_UPDATES:
        dataSources.unregisterContentObserver(pointObserver);
        break;
      case ROUTE_UPDATES:
        dataSources.unregisterContentObserver(routeObserver);
        break;
      case ROUTE_TRACKPOINT_UPDATES:
        dataSources.unregisterContentObserver(routeTrackPointObserver);
        break;
      case DISPLAY_PREFERENCES:
        dataSources.unregisterOnSharedPreferenceChangeListener(preferenceListener);
        break;
      case SAMPLED_OUT_POINT_UPDATES:
        throw new IllegalArgumentException("Should have been mapped to point updates");
    }
  }

  /** Unregisters all internal (sensor, position, etc.) listeners. */
  void unregisterAllListeners() {
    dataSources.removeLocationUpdates(locationListener);
    dataSources.unregisterSensorListener(compassListener);
    dataSources.unregisterContentObserver(routeObserver);
    dataSources.unregisterContentObserver(routeTrackPointObserver);
    dataSources.unregisterContentObserver(pointObserver);
    dataSources.unregisterOnSharedPreferenceChangeListener(preferenceListener);
  }
}

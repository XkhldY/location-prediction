package com.google.android.location.route;

import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;

/**
* Interface for abstracting registration of external data source listeners.
*
* @author Andrei
*/
interface IDbManagerDataSource {
 // Preferences
 void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener);
 void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener);

 // Content provider
 void registerContentObserver(Uri contentUri, boolean descendents,ContentObserver observer);
 void unregisterContentObserver(ContentObserver observer);

 // Sensors
 Sensor getSensor(int type);
 void registerSensorListener(SensorEventListener listener,Sensor sensor, int sensorDelay);
 void unregisterSensorListener(SensorEventListener listener);

 // Location
 boolean isLocationProviderEnabled(String provider);
 void requestLocationUpdates(LocationListener listener);
 void removeLocationUpdates(LocationListener listener);
 Location getLastKnownLocation();
} 
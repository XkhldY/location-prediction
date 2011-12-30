package com.google.android.location.route;



import com.google.android.location.content.Constants;
import com.google.android.location.content.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import static com.google.android.location.content.Constants.*;
public class DbManagerDataSourceImpl implements IDbManagerDataSource
{

	 // System services
	  private final SensorManager sensorManager;
	  private final LocationManager locationManager;
	  private final ContentResolver contentResolver;
	  private final SharedPreferences sharedPreferences;
	  private final Context context;

	  DbManagerDataSourceImpl(Context context, SharedPreferences sharedPreferences) {
	    this.context = context;
	    this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	    this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	    this.contentResolver = context.getContentResolver();
	    this.sharedPreferences = sharedPreferences;
	  }

	  @Override
	  public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) 
	  {
	    sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
	  }

	  @Override
	  public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) 
	  {
	    sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
	  }
	  /**
	   * register an observer class that gets callbacks when changes to the Uri contentUri take place
	   * Thus when a change in the Uri takes place the class observer receives a callback
	   * contentresolver - the conneciton with the content model
	   */
	  
	  @Override
	  public void registerContentObserver(Uri contentUri, boolean descendents,ContentObserver observer) 
	  {
	    contentResolver.registerContentObserver(contentUri, descendents, observer);
	  }

	  @Override
	  public void unregisterContentObserver(ContentObserver observer) 
	  {
	    contentResolver.unregisterContentObserver(observer);
	  }

	  @Override
	  public Sensor getSensor(int type) {
	    return sensorManager.getDefaultSensor(type);
	  }

	  @Override
	  public void registerSensorListener(SensorEventListener listener, Sensor sensor, int sensorDelay) 
	  {
	    sensorManager.registerListener(listener, sensor, sensorDelay);
	  }

	  @Override
	  public void unregisterSensorListener(SensorEventListener listener) 
	  {
	    sensorManager.unregisterListener(listener);
	  }

	  @Override
	  public boolean isLocationProviderEnabled(String provider) 
	  {
	    return locationManager.isProviderEnabled(provider);
	  }

	  @Override
	  public void requestLocationUpdates(LocationListener listener) 
	  {
	    // Check if the provider exists.
	    LocationProvider gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
	    if (gpsProvider == null) {
	      listener.onProviderDisabled(LocationManager.GPS_PROVIDER);
	      locationManager.removeUpdates(listener);
	      return;
	    }

	    // Listen to GPS location.
	    String providerName = gpsProvider.getName();
	    Log.d(Constants.TAG, "TrackDataHub: Using location provider " + providerName);
	    locationManager.requestLocationUpdates(providerName,
	        0 /*minTime*/, 0 /*minDist*/, listener);

	    // Give an initial update on provider state.
	    if (locationManager.isProviderEnabled(providerName)) {
	      listener.onProviderEnabled(providerName);
	    } else {
	      listener.onProviderDisabled(providerName);
	    }

	    // Listen to network location
	    try {
	      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
	          1000 * 60 * 5 /*minTime*/, 0 /*minDist*/, listener);
	    } catch (RuntimeException e) {
	      // If anything at all goes wrong with getting a cell location do not
	      // abort. Cell location is not essential to this app.
	      Log.w(Constants.TAG, "Could not register network location listener.", e);
	    }
	  }

	  @Override
	  public Location getLastKnownLocation() {
	    // TODO: Let's look at more advanced algorithms to determine the best
	    // current location.

	    Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	    final long now = System.currentTimeMillis();
	    if (loc == null || loc.getTime() < now - MAX_LOCATION_AGE_MS) {
	      // We don't have a recent GPS fix, just use cell towers if available
	      loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

	      int toastResId = R.string.my_location_approximate_location;
	      if (loc == null || loc.getTime() < now - MAX_NETWORK_AGE_MS) {
	        // We don't have a recent cell tower location, let the user know:
	        toastResId = R.string.my_location_no_location;
	      }

	      // Let the user know we have only an approximate location:
	     Toast.makeText(context, context.getString(toastResId), Toast.LENGTH_LONG).show();
	    }
	    return loc;
	  }

	  @Override
	  public void removeLocationUpdates(LocationListener listener) {
	    locationManager.removeUpdates(listener);
	  }
	}
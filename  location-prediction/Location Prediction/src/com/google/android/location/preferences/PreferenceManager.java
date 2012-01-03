package com.google.android.location.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

import com.google.android.location.content.Constants;
import com.google.android.location.content.R;

import com.google.android.service.ServiceManager;
import com.google.android.utilities.MyLocationListenerPolicy;

/**
 * A class that manages reading the shared preferences for the service. *
 * 
 * @author Andrei
 */
public class PreferenceManager implements OnSharedPreferenceChangeListener 
{
	private ServiceManager service;
	private SharedPreferences sharedPreferences;

	private final String autoResumeRouteCurrentRetryKey;
	private final String autoResumeRouteTimeOutKey;
	private final String maxRecordingDistanceKey;
	// until future changes metric will as default(imperial is an option)
	private final String metricUnitsKey;
	/**
	 * min distance for recording
	 */
	private final String minRecordingDistanceKey;
	/**
	 * min recordingIntervcal(time based)
	 */
	private final String minRecordingIntervalKey;
	private final String minRequiredAccuracyKey;
	/**
	 * id of the recording. will be set up in the service before or during the
	 * recording
	 */
	private final String recordingRouteKey;
	/**
	 * the track that will be recorded
	 */
	private final String selectedRouteKey;
	/**
		   * 
		   */
	private final String updateIdleTimeKey;

	public PreferenceManager(ServiceManager service) {
		this.service = service;
		this.sharedPreferences = service.getSharedPreferences(
				Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
		if (sharedPreferences == null) {
			Log.w(Constants.TAG,
					"TrackRecordingService: Couldn't get shared preferences.");
			throw new IllegalStateException("Couldn't get shared preferences");
		}
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		autoResumeRouteCurrentRetryKey = service.getString(R.string.auto_resume_route_current_retry_key);
		autoResumeRouteTimeOutKey = service.getString(R.string.auto_resume_route_timeout_key);
		
		maxRecordingDistanceKey = service.getString(R.string.max_recording_distance_key);
		
		metricUnitsKey = service.getString(R.string.metric_units_key);
		minRecordingDistanceKey = service.getString(R.string.min_recording_distance_key);
		minRecordingIntervalKey = service.getString(R.string.min_recording_interval_key);
		minRequiredAccuracyKey = service.getString(R.string.min_required_accuracy_key);
		recordingRouteKey = service.getString(R.string.recording_route_key);
		selectedRouteKey = service.getString(R.string.selected_route_key);
		updateIdleTimeKey = service.getString(R.string.route_update_idletime_key);

		// Refresh all properties setting the key to null.
		onSharedPreferenceChanged(sharedPreferences, null);
	}

	/**
	 * Notifies that preferences have changed. Call this with key == null to
	 * update all preferences in one call.
	 * 
	 * @param key
	 *            the key that changed (may be null to update all preferences)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) 
	{
		if (service == null) {
			Log.w(Constants.TAG,
			     "onSharedPreferenceChanged: a preference change (key = "+ key + ") " +
			     "after a call to shutdown()");
			return;
		}
		if (key == null || key.equals(minRecordingDistanceKey)) {
			int minRecordingDistance = sharedPreferences.getInt(
					minRecordingDistanceKey,
					Constants.DEFAULT_MIN_RECORDING_DISTANCE);
			service.setMinRecordingDistance(minRecordingDistance);
			Log.d(Constants.TAG,
					"TrackRecordingService: minRecordingDistance = "
							+ minRecordingDistance);
		}
		if (key == null || key.equals(maxRecordingDistanceKey)) {
			service.setMaxRecordingDistance(sharedPreferences.getInt(
					maxRecordingDistanceKey,
					Constants.DEFAULT_MAX_RECORDING_DISTANCE));
		}
		if (key == null || key.equals(minRecordingIntervalKey)) {
			int minRecordingInterval = sharedPreferences.getInt(
					minRecordingIntervalKey,
					Constants.DEFAULT_MIN_RECORDING_INTERVAL);
			switch (minRecordingInterval) {
			case -2:
				// Battery Miser
				// min: 30 seconds
				// max: 3 minutes
				// minDist: 15 meters Choose battery life over moving time
				// accuracy.
				service.setLocationListenerPolicy(new MyLocationListenerPolicy(
						30000, 180000,25));
				break;
			case -1:
				// High Accuracy
				// min: 1 second
				// max: 30 seconds
				// minDist: 0 meters get all updates to properly measure moving
				// time.
				service.setLocationListenerPolicy(new MyLocationListenerPolicy(
						1000, 30000,15));
				break;
			default:
				service.setLocationListenerPolicy(new MyLocationListenerPolicy(
						minRecordingInterval * 1000));
			}
		}
		if (key == null || key.equals(minRequiredAccuracyKey)) 
		{
			service.setMinRequiredAccuracy(sharedPreferences.getInt(
					minRequiredAccuracyKey,
					Constants.DEFAULT_MIN_REQUIRED_ACCURACY));
		}

		if (key == null || key.equals(autoResumeRouteTimeOutKey)) {
			service.setAutoResumeRouteTimeout(sharedPreferences.getInt(
					autoResumeRouteTimeOutKey,
					Constants.DEFAULT_AUTO_RESUME_ROUTE_TIMEOUT));
		}
		if (key == null || key.equals(recordingRouteKey)) 
		{
			long recordingRouteId = sharedPreferences.getLong(
					recordingRouteKey, -1);
			// Only read the id if it is valid.
			// Setting it to -1 should only happen in
			// TrackRecordingService.endCurrentTrack()
			if (recordingRouteId > 0) 
			{
				service.setRecordingRouteId(recordingRouteId);
			}
		}
		if (key == null || key.equals(updateIdleTimeKey)) {
			service.setPeriodicTaskKey(sharedPreferences.getInt( updateIdleTimeKey, 0));
		}
		if (key == null || key.equals(metricUnitsKey)) 
		{
			service.setMetricUnits(sharedPreferences.getBoolean(metricUnitsKey,true));
		}
	}

	public void setAutoResumeRouteCurrentRetry(int retryAttempts) 
	{
		Editor editor = sharedPreferences.edit();
		editor.putInt(autoResumeRouteCurrentRetryKey, retryAttempts);
		editor.commit();
	}

	/**
	 * sets in preferences the current id of route that is currently recording
	 * 
	 * @param id
	 *            of the route that is currently recording
	 */
	public void setRecordingRoute(long id) 
	{
		Editor editor = sharedPreferences.edit();
		editor.putLong(recordingRouteKey, id);
		editor.commit();
	}

	public void setSelectedRoute(long id) 
	{
		Editor editor = sharedPreferences.edit();
		editor.putLong(selectedRouteKey, id);
		editor.commit();
	}

	public void shutdown() {
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		service = null;
	}
}

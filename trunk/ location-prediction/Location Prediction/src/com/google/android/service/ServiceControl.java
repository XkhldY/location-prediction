package com.google.android.service;

import java.util.List;

import com.google.android.lib.services.IRouteRecordingService;
import com.google.android.location.content.Constants;
import com.google.android.location.content.R;

import static com.google.android.lib.logs.MyLogClass.TAG;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;
/**
 * Class for checking the status of the service
 * 
 * @author Andrei
 *
 */
public class ServiceControl {

	/**
	 * Checks whether we're currently recording. The checking is done by calling
	 * the service, if provided, or alternatively by reading recording state
	 * saved to preferences.
	 * 
	 * @param context
	 *            the current context
	 * @param service
	 *            the service, or null if not bound to it
	 * @param preferences
	 *            the preferences, or null if not available
	 * @return true if the service is recording (or supposed to be recording),
	 *         false otherwise
	 */
	public static boolean isRecording(Context context,
			IRouteRecordingService service, SharedPreferences preferences) {
		if (service != null) {
			try {
				return service.isRecording();
			} catch (RemoteException e) {
				Log.e(TAG, "Failed to check if service is recording", e);
			} catch (IllegalStateException e) {
				Log.e(TAG, "Failed to check if service is recording", e);
			}
		}

		if (preferences == null) {
			preferences = context.getSharedPreferences(Constants.SETTINGS_NAME,
					Context.MODE_PRIVATE);
		}
		return preferences.getLong(
				context.getString(R.string.recording_route_key), -1) > 0;
	}

	/**
	 * Checks whether the recording service is currently running.
	 * 
	 * @param ctx
	 *            the current context - advisable to use getApplicationContext
	 * @return true if the service is running, false otherwise
	 */
	public static boolean isServiceRunning(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		for (RunningServiceInfo serviceInfo : services) {
			ComponentName componentName = serviceInfo.service;
			String serviceName = componentName.getClassName();
			if (serviceName.equals(ServiceManager.class.getName())) {
				return true;
			}
		}
		return false;
	}

	private ServiceControl() {
		Log.d(Constants.TAG, "ServiceControl:Constructor");
	}
}

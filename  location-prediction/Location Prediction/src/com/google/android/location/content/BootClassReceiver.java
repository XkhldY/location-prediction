package com.google.android.location.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static com.google.android.location.content.Constants.RESUME_TRACK_EXTRA_NAME;
import com.google.android.service.ServiceManager;
/**
 * class for maintaining the status of booting/rebooting the android phone.
 * Meaning that if the android phone gets rebooted then when the rebooting finishes
 * the route is loaded again and the service starts.
 * @author Andrei
 *
 */
public class BootClassReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		Log.d(Constants.TAG, "BootReceiver.onReceive: " + intent.getAction());
		if (ACTION_BOOT_COMPLETED.equals(intent.getAction())) 
		{
			Intent startIntent = new Intent(context, ServiceManager.class);
			startIntent.putExtra(RESUME_TRACK_EXTRA_NAME, true);
			context.startService(startIntent);
		} 
		else 
		{
			Log.w(Constants.TAG, "BootReceiver: unsupported action");
		}
	}
}
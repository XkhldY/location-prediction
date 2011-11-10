package com.services;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LocalServiceTracking extends Service {

	public static final String COUNT_VALUE = "COUNT";
	private static final String TAG = "ServiceTracking";
	Context mContext;
	private LocationBinder myLocationBinder = new LocationBinder();
	boolean isRecording = false;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		myLocationBinder.onCreate(this);
	    Log.e(TAG, "Creating...");
	}
	@Override
	public IBinder onBind(Intent mIntent) 
	{
		Log.e(TAG, "The message from Activity is :" + mIntent.getExtras().getInt(COUNT_VALUE));
		return myLocationBinder;
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	    stopRecording();
	}
   
	private void stopRecording() 
	{	Log.e(TAG, "Stopping the service..");
		Toast.makeText(getApplicationContext(), "Stopping the service...", Toast.LENGTH_LONG).show();
	}

}

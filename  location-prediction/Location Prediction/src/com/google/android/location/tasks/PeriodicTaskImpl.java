package com.google.android.location.tasks;

import com.google.android.lib.content.CreateRouteTrackPoint;
import com.google.android.service.ServiceControl;
import com.google.android.service.ServiceManager;

import android.content.Context;
import android.util.Log;

import static com.google.android.location.content.Constants.*;

/**
 * class for inserting markers of points on the current relative to the idle time.
 * e.g if an user has spent more than a predefined amount of time in a place
 * that place will be regarded as an end point. if the user will pass another time through the same point
 * new updates will occur and if this time the threshold is not met
 * then the end point will have its status removed, meaning will no longer be regarded as an end point
 * point and new updates will occur this will be regarded as an end point
 * @author Andrei
 *
 */
public class PeriodicTaskImpl implements PeriodicTask {

	ServiceManager service;
	boolean isRecording;
	@Override
	public void insertRouteTrackPoint() 
	{
		Log.d(TAG, "PeriodicTaskImpl:inserting a point on the route");
		if(isRecording)
		{
			Log.d(TAG, "PeriodicTaskImpl:inserting a route track point");
			service.insertRouteTrackPoint(CreateRouteTrackPoint.DEFAULT_STATISTICS);
		}
	}

	@Override
	public void shutdown() 
	{
		Log.d(TAG, "PeriodicTaskImpl:shutdown");
	}

	@Override
	public void start(ServiceManager service, boolean isRecording) 
	{
		Log.d(TAG, "PeriodicTaskImpl:starting");
		this.service = service;
		this.isRecording = isRecording;
	}
    public static class Factory implements PeriodicTaskFactory
    {

		@Override
		public PeriodicTask create() 
		{
		   return new PeriodicTaskImpl();
		}
    	
    }
}

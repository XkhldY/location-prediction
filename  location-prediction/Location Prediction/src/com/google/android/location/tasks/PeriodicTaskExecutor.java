package com.google.android.location.tasks;

import android.util.Log;

import com.google.android.location.content.Constants;
import com.google.android.service.ServiceManager;
import com.google.android.utilities.LocationListenerPolicy;
import com.google.android.utilities.UnitConversions;

import static com.google.android.location.content.Constants.*;

/**
 * Execute a task on a time or distance schedule.
 * 
 * @author Andrei
 */
public class PeriodicTaskExecutor {

	/**
	 * The frequency of the task. A value greater than zero is a frequency in
	 * time. A value less than zero is considered a frequency in distance.
	 */
	private int minRecordingDistance = Constants.DEFAULT_MIN_RECORDING_DISTANCE;

	/**
	 * The next distance when the task should execute.
	 */
	private double distanceToLastRecorded = 0;

	
	/**
	 * for now we use only metric units - imperial units later on
	 */
	private boolean metricUnits;

	LocationListenerPolicy locationListenerPolicy;
	private final PeriodicTaskFactory factory;
	private long idleTime = 0;
	
	/**
	 * task that will update the mapview and the database
	 */
	private PeriodicTask task;
	private boolean isRecording;
	private ServiceManager service;

	public PeriodicTaskExecutor(PeriodicTaskFactory factory,
			boolean isRecording,
			LocationListenerPolicy locationListenerPPolicy,
			ServiceManager service) {
		this.service = service;
		this.factory = factory;
		this.isRecording = isRecording;
		this.locationListenerPolicy = locationListenerPPolicy;
	}

	/**
	 * Restores the manager.
	 */
	public void restore(boolean isRecording) 
	{
		if (!isRecording) {
			return;
		}
		// Try to make the task.
		// PeriodicTask creation
		// implemented by PeriodicTaskImpl
	
	
	}

	/**
	 * Shuts down the manager.
	 */
	public void shutdown() 
	{
		if (task != null) {
			task.shutdown();
			task = null;
		}
	}

	/**
	 * Updates executer with new trip statistics.
	 */
	public void update() 
	{
		idleTime = locationListenerPolicy.getIdleTime();
		distanceToLastRecorded = locationListenerPolicy.getDistance();
		if (idleTime > minRequiredIdleTime && 
			locationListenerPolicy.getDistance() < minRecordingDistance) 
		{
			task = factory.create();
			// Returning null is ok.
			if (task == null) 
			{
				return;
			}
			// does nothing...arranging steps in the PeriodicTaskExecutor
			task.start(service, isRecording);
			task.insertRouteTrackPoint();
		}
	}
    /**
     * setting up the metrics from the preferences.
     * right now there is only one option available - metric not imperial(e.g miles/h not km/h)
     * @param metricUnits
     */
	public void setMetricUnits(boolean metricUnits) {
		this.metricUnits = metricUnits;
	}
}

package com.google.android.location.tasks;

import com.google.android.service.ServiceManager;
/**
 * updates the map view and database after the user is on the same point for an amount of time
 * @author Andrei
 *
 */
public interface PeriodicTask {

	/**
	 * inserts a route track point in the database after some criterias are met
	 */
	void insertRouteTrackPoint();

	/**
	 * shutsdown the task and reloads everything - in case the service stops
	 */
	void shutdown();

	/**
	 * 
	 * @param set up for subsequent calls of inserting a new route track point
	 */
	void start(ServiceManager service, boolean isRecording);

}

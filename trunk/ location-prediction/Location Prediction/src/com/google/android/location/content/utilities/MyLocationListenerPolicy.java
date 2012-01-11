package com.google.android.location.content.utilities;

import com.google.android.location.content.Constants;


import android.location.Location;
import android.util.Log;

public class MyLocationListenerPolicy implements LocationListenerPolicy 
{
    /**
     * distance from the last recorded location
     */
	private double distance;
	/**
	 * min interval for idle time
	 */
    private long minInterval;
    /**
     * upper value for the interval
     */
    private long maxInterval;
    /**
     * min distance between updates
     */
    private int minDistance;
    /**
     * value to check the type of location listener
     */
    private boolean isStaticLocationListener = false;
    private Location lastLocation = null;
    /**
     * idle time in one place
     */
    private long idleTime;
    /**
     * 
     * @param minInterval the minimum by which the idle time is bounded value
     * @param maxInterval the maximum bounded value
     * @param minDistance the minimum distance
     */
	public MyLocationListenerPolicy(long minInterval, long maxInterval, int minDistance) 
	{
	  this.minInterval = minInterval;
	  this.maxInterval = maxInterval;
	  this.minDistance = minDistance;
	}

	public MyLocationListenerPolicy(final long interval) 
	{
      isStaticLocationListener = true;
      this.minInterval = this.maxInterval = interval;
	}
	@Override
    public long getDesiredPollingInterval()
    {
    	if(isStaticLocationListener)
    	{
    		return getDesiredStaticPollingInterval();
    	}
    	return getDesiredDynamicPollingInterval();
    }
	
	private long getDesiredStaticPollingInterval() 
	{
		return (long) minInterval;
	}

	@Override
	public double getDistance() 
	{
		return distance;
	}
    @Override
    public int getMinDistance()
    {
    	return minDistance;
    }
	@Override
	public void updateIdleTime(long idleTime) 
	{
		this.idleTime = idleTime;
	}
    
    @Override
    public void setDistance(double distance)
    {
    	this.distance = distance;
    }
	private long getDesiredDynamicPollingInterval() 
	{
		long desiredInterval = idleTime / 2;
	    // Round to avoid setting the interval too often.
	    desiredInterval = (desiredInterval / 1000) * 1000;
	    return Math.max(Math.min(maxInterval, desiredInterval),
	                    minInterval);
	}

	@Override
	public long getIdleTime() 
	{
       return idleTime;
	}
    @Override
	public Location getLastLocation() {
		return lastLocation;
	}
    @Override
	public void setLastLocation(Location lastLocation) {
		this.lastLocation = lastLocation;
	}

	@Override
	public void resetIdleTime() 
	{
	   this.idleTime = 0;	
	}

}

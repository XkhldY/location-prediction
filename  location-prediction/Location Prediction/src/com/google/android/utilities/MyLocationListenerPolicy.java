package com.google.android.utilities;

import com.google.android.location.content.Constants;


import android.util.Log;

public class MyLocationListenerPolicy implements LocationListenerPolicy 
{

	/**
	 * min interval for idle time
	 */
    private long minInterval;
    /**
     * upper value for the interval
     */
    private long maxInterval;
    /**
     * the min distance until the next update
     */
    private int minDistance;
    /**
     * value to check the type of location listener
     */
    private boolean isStaticLocationListener = false;
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
	public int getMinDistance() 
	{
		if(isStaticLocationListener)
		{
			return 0;
		}
		return minDistance;
	}
   
	@Override
	public void updateIdleTime(long idleTime) 
	{
		if(isStaticLocationListener)
		{
			Log.d(Constants.TAG, "StaticListener");
		}
		else
		{
			this.idleTime = idleTime;
		}
	}


	private long getDesiredDynamicPollingInterval() 
	{
		long desiredInterval = idleTime / 2;
	    // Round to avoid setting the interval too often.
	    desiredInterval = (desiredInterval / 1000) * 1000;
	    return Math.max(Math.min(maxInterval, desiredInterval),
	                    minInterval);
	}

}

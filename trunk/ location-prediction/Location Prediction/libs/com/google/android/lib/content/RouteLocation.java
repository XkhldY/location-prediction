package com.google.android.lib.content;

import android.location.Location;

public final class RouteLocation extends Location 
{
    /**
     * the id of the location from the provider
     */
    private int locationid = -1;
    /**
     * count the number of times the user has been at this location
     */
    private int times_count;
    /**
     * total time that the user has been idle at this location
     */
    private long idle_time;
    
	public RouteLocation(String provider) 
	{
		super(provider);
	}
    public RouteLocation(Location location)
    {
    	super(location);
    }
	public int getLocationid() {
		return locationid;
	}

	public void setLocationid(int locationid) {
		this.locationid = locationid;
	}


    @Override
    public void reset() 
    {
       super.reset();
       locationid = -1;
       times_count = -1;
       idle_time = -1;
    }
	public int getTimesCount() {
		return times_count;
	}
	public void setTimesCount(int times_count) {
		this.times_count = times_count;
	}
	public long getIdleTime() {
		return idle_time;
	}
	public void setIdleTime(long idle_time) {
		this.idle_time = idle_time;
	}
}

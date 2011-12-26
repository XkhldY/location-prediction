package com.google.android.lib.content;

import android.location.Location;

public class RouteLocation extends Location 
{
   
    private int locationid = -1;
    
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
      
    }
}

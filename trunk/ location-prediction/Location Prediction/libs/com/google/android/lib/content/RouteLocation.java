package com.google.android.lib.content;

import android.location.Location;

public class RouteLocation extends Location 
{
    private Sensor.SensorDataSet sensorDataSet = null;
    private int locationid = -1;
    
	public RouteLocation(String provider) 
	{
		super(provider);
	}
    public RouteLocation(Location location, Sensor.SensorDataSet sensorData)
    {
    	super(location);
    	this.sensorDataSet = sensorData;
    }
	public int getLocationid() {
		return locationid;
	}

	public void setLocationid(int locationid) {
		this.locationid = locationid;
	}

	public Sensor.SensorDataSet getSensorDataSet() {
		return sensorDataSet;
	}

	public void setSensorDataSet(Sensor.SensorDataSet sensorDataSet) {
		this.sensorDataSet = sensorDataSet;
	}
    @Override
    public void reset() 
    {
       super.reset();
       locationid = -1;
       sensorDataSet = null; 
    }
}

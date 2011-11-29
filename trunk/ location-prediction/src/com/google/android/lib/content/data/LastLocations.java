package com.google.android.lib.content.data;

import android.location.Location;

import com.google.android.lib.content.RouteLocation;


public class LastLocations implements CreateLocationFactory
{
	private int lastGpsLocation = 0;
	private int lastNetworkLocation = 0;
	Location locsGps[] = new RouteLocation[] 
	{   
		new RouteLocation("gps"),
		new RouteLocation("gps") 
	};
	Location locsNetwork[] = new RouteLocation[] 
	{
		new RouteLocation("Network"), new RouteLocation("Network") 
	};

	public Location createGPSLocation() 
	{
		lastGpsLocation = (lastGpsLocation + 1) % locsGps.length;
		return locsGps[lastGpsLocation];
	}

	public Location createNetworkLocation() 
	{
		lastNetworkLocation = (lastNetworkLocation + 1) % locsNetwork.length;
		return locsNetwork[lastNetworkLocation];
	}
}

package com.google.android.utilities;

import android.location.Location;

public class MyLocationListenerPolicy implements LocationListenerPolicy 
{

	//private double 
	public MyLocationListenerPolicy(int min, int max, int k) 
	{
	
	}

	public MyLocationListenerPolicy(int i) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public long getDesiredPollingInterval() 
	{
		return 0;
	}

	@Override
	public int getMinDistance() 
	{
		return 0;
	}

	@Override
	public void updateIdleTime(long idleTime) 
	{
		
	}

}

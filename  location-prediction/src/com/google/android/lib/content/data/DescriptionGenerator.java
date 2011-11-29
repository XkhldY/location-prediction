package com.google.android.lib.content.data;

import java.util.Vector;

import com.google.android.lib.content.Track;
import com.google.android.lib.content.Route;

public interface DescriptionGenerator 
{
	  public String generateWaypointDescription(Track endPoint);
	  public String generateTrackDescription(Route route, Vector<Double> distances,Vector<Double> elevations);
}

package com.google.android.location.content.libcontent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.android.location.content.libstatistics.RouteStatistics;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class Route implements Parcelable
{
	private long id = -1;
	private String name = "";
	private String description = "";
	private String category  ="";
	private long start_id= -1;
	private long stop_id = -1;
	private String map_id = "";
	private String table_id ="";
	private RouteStatistics routeStatistics =  new RouteStatistics();
	private int times_count = 0;
	private int nrOfRoutePoints = 0;
	private long idleTime = 0L;
	private ArrayList<Location>mLocations = new ArrayList<Location>();
	private Map<Integer, Location>mHashLocations =  new HashMap<Integer, Location>();
	//internal usage for now
	
	private long duration_of_move;
	private double length_of_route;
	public Route()
	{
		
	}
	public int describeContents() 
	{
		return 0;
	}
   
	public void writeToParcel(Parcel dest, int flags) 
	{
	    dest.writeLong(id);
	    dest.writeString(name);
	    dest.writeString(description);
	    dest.writeString(description);
	    dest.writeLong(start_id);
	    dest.writeLong(stop_id);
	    dest.writeString(map_id);
	    
	    dest.writeParcelable(routeStatistics, 0);
	    dest.writeInt(nrOfRoutePoints);
	    for(int k = 0;k<nrOfRoutePoints;k++)
	    {
	    	dest.writeParcelable(mLocations.get(k), 0);
	    	dest.writeParcelable(mHashLocations.get(k), 0);
	    }
	    dest.writeString(table_id);
	    dest.writeInt(times_count);
	    dest.writeLong(idleTime);
	}
	public Route(Parcel source)
	{
	   readFromParcel(source);	
	}
	public void readFromParcel(Parcel source)
	{
		ClassLoader mClassLoader  = getClass().getClassLoader();
		
		this.id = source.readLong();
		this.name = source.readString();
		this.description = source.readString();
		this.category = source.readString();
		this.start_id = source.readLong();
		this.stop_id = source.readLong();
		this.map_id = source.readString();
	   
		routeStatistics = source.readParcelable(mClassLoader);
		
		nrOfRoutePoints = source.readInt();
		for(int  k =0;k<nrOfRoutePoints;k++)
		{
			Location mLocation = source.readParcelable(mClassLoader);
			mLocations.add(mLocation);
			mHashLocations.put(k, mLocation);
		}
		table_id = source.readString();
		times_count = source.readInt();
		idleTime = source.readLong();
	}
	Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() 
    {

		public Route createFromParcel(Parcel source) 
		{
			return new Route(source);
		}

		public Route[] newArray(int size) 
		{
			return new Route[size];
		}
    };
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public long getStart_id() {
		return start_id;
	}

	public void setStart_id(long start_id) {
		this.start_id = start_id;
	}

	public long getStop_id() {
		return stop_id;
	}

	public void setStop_id(long stop_id) {
		this.stop_id = stop_id;
	}

	public String getMap_id() {
		return map_id;
	}

	public void setMap_id(String map_id) {
		this.map_id = map_id;
	}

	public String getTable_id() {
		return table_id;
	}

	public void setTable_id(String table_id) {
		this.table_id = table_id;
	}

	public RouteStatistics getRouteStatistics() {
		return routeStatistics;
	}

	public void setRouteStatistics(RouteStatistics routeStatistics) {
		this.routeStatistics = routeStatistics;
	}

	public long getDuration_of_move() {
		return duration_of_move;
	}

	public void setDuration_of_move(long duration_of_move) {
		this.duration_of_move = duration_of_move;
	}

	public double getLength_of_route() {
		return length_of_route;
	}

	public void setLength_of_route(double length_of_route) {
		this.length_of_route = length_of_route;
	}

	public ArrayList<Location> getLocations() {
		return mLocations;
	}
    public void addLocation(Location loc)
    {
    	mLocations.add(loc);
    }
	public void setLocations(ArrayList<Location> mLocations) {
		this.mLocations = mLocations;
	}

	public Map<Integer, Location> getHashLocations() {
		return mHashLocations;
	}

	public void setHashLocations(Map<Integer, Location> mHashLocations) {
		this.mHashLocations = mHashLocations;
	}

	public int getNumberRoutePoints() {
		return nrOfRoutePoints;
	}

	public void setNumberRoutePoints(int nrOfRoutePoints) {
		this.nrOfRoutePoints = nrOfRoutePoints;
	}
	public int getTimesCount() {
		return times_count;
	}
	public void setTimesCount(int times_count) {
		this.times_count = times_count;
	}
	public long getIdleTime()
	{
		return idleTime;
	}
	public void setIdleTime(long idleTime)
	{
		this.idleTime = idleTime;
	}
	
}

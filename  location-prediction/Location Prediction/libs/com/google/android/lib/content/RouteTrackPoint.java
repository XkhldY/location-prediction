package com.google.android.lib.content;


import com.google.android.lib.statistics.RouteStatistics;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public final class RouteTrackPoint implements Parcelable 
{
	public static final int TYPE_OF_TRACKPOINT= 0;
	public static final int TRACKPOINT_STATISTICS = 1;
	private String name = "";
	private String category = "";
	private String description = "";
    private String icon="";
	private Location location;
    private long id = -1;
    private long route_id =-1;
    private long start_id = -1;
    private long stop_id = -1;
    private RouteStatistics tripStats;
    
    //length of the route
    private double route_length;
    //duration pf the whole route
    private double track_duration;
    //last route duration - from the last RouteTrackPoint
    private double last_trackpoint_duration;
    private String track_Icon = "";
	private int type = 0;
	private int nrOfTimes = 0;
	public RouteTrackPoint() 
	{
		
	}
	public void writeToParcel(Parcel dest, int flags) 
	{
         dest.writeLong(id);
         dest.writeLong(route_id);
         dest.writeLong(start_id);
         dest.writeLong(stop_id);
         dest.writeString(name);
         dest.writeString(description);
         dest.writeString(category);
         dest.writeString(track_Icon);
         dest.writeInt(type); 
         //and now parcelable objects
         dest.writeByte(tripStats == null?(byte)0:(byte)1);
         if(tripStats!=null)
         {
        	 dest.writeParcelable(tripStats, 0);
         }
         dest.writeByte(location==null?(byte)0:(byte)1);
         if(location!=null)
         {
        	 dest.writeParcelable(location, 0);
         }
         dest.writeInt(nrOfTimes);
	}
	public RouteTrackPoint(Parcel source)
	{	
       readFromParcel(source);
	}
	public void readFromParcel(Parcel source)
	{
		id = source.readLong();
		start_id = source.readLong();
		stop_id = source.readLong();
		name = source.readString();
		description = source.readString();
		category = source.readString();
		track_Icon = source.readString();
		route_id = source.readLong();
		type = source.readInt();
		//and now parcelable objects
		ClassLoader mClassLoader = getClass().getClassLoader();
		byte getTripStats = source.readByte();
		if(getTripStats > 0)
		{
			tripStats = source.readParcelable(mClassLoader);
		}
		byte  hasLocation = source.readByte();
		if(hasLocation > 0)
		{
			location = source.readParcelable(mClassLoader);
		}
		nrOfTimes = source.readInt();
		
	}
	public static final Parcelable.Creator<RouteTrackPoint> CREATOR = new Parcelable.Creator<RouteTrackPoint>() 
	{

		public RouteTrackPoint createFromParcel(Parcel source) 
		{
			return new RouteTrackPoint(source);
		}

		public RouteTrackPoint[] newArray(int size) 
		{
			return new RouteTrackPoint[size];
		}
	
	};
    public double getRouteLength()
    {
    	return route_length;
    }
    public void setRouteLength(double track_length)
    {
    	this.route_length = track_length;
    }
    public int getType()
    {
      return type;
    }
    public void setType(int type)
    {
    	this.type = type;
    }
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	public String getTrackicon() {
		return track_Icon;
	}
	public void setTrackicon(String track_icon) {
		this.track_Icon = track_icon;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getRouteId() {
		return route_id;
	}
	public void setRouteId(long routeid) {
		this.route_id = routeid;
	}
	public long getStartPointid() {
		return start_id;
	}
	public void setStartPointid(long startid) {
		start_id = startid;
	}
	public long getStopPointId() {
		return stop_id;
	}
	public void setStopPointId(long stopPointId) {
		stop_id = stopPointId;
	}
	public RouteStatistics getRouteStatistics() {
		return tripStats;
	}
	public void setRouteStatisticss(RouteStatistics tripStats) {
		this.tripStats = tripStats;
	}
	
	public double getDuration() {
		return track_duration;
	}
	public void setDuration(double track_duration) {
		this.track_duration = track_duration;
	}
	public double getLastTrackDuration() {
		return last_trackpoint_duration;
	}
	public void setLastTrackDuration(double lastTrackDuration) {
		this.last_trackpoint_duration = lastTrackDuration;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public int getNrOfTimes() {
		return nrOfTimes;
	}
	public void setNrOfTimes(int nrOfTimes) {
		this.nrOfTimes = nrOfTimes;
	}

	
  
}

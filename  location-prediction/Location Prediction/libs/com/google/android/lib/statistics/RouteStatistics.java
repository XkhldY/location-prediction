package com.google.android.lib.statistics;

import android.os.Parcel;
import android.os.Parcelable;

public class RouteStatistics implements Parcelable{

	//start of the trip/route
	private long start_time = -1;
	private long stop_time = -1;
	private long moving_time = -1;
	private long total_time = -1;
	private double total_distance;
	private double total_elevation_gain;
	private double max_speed;
	
	private final ExtremLocationManager latitudeExtremities = new ExtremLocationManager();
	private final ExtremLocationManager longitudeExtremities  = new ExtremLocationManager();
	private final ExtremLocationManager elevationExtremities = new ExtremLocationManager();
	private final ExtremLocationManager gradeExtremities = new ExtremLocationManager();
	private final ExtremTimerManager start_interval_time = new ExtremTimerManager();
	private final ExtremTimerManager stop_interval_time =  new ExtremTimerManager();
	
	public RouteStatistics()
	{
		
	}
	public RouteStatistics(RouteStatistics otherRoute)
	{
		this.start_time = otherRoute.start_time;
		this.stop_time = otherRoute.stop_time;
		this.moving_time = otherRoute.moving_time;
		this.total_distance = otherRoute.total_distance;
		this.total_elevation_gain = otherRoute.total_elevation_gain;
		this.total_time = otherRoute.total_time;
		this.latitudeExtremities.setMinMax(otherRoute.latitudeExtremities.getMin(),otherRoute.latitudeExtremities.getMax());
		this.longitudeExtremities.setMinMax(otherRoute.longitudeExtremities.getMin(), otherRoute.longitudeExtremities.getMax());
		this.elevationExtremities.setMinMax(otherRoute.elevationExtremities.getMin(), otherRoute.elevationExtremities.getMax());
		this.gradeExtremities.setMinMax(otherRoute.gradeExtremities.getMin(), otherRoute.gradeExtremities.getMax());
		this.start_interval_time.setMinMax(otherRoute.start_interval_time.getMin(), otherRoute.start_interval_time.getMin());
		this.stop_interval_time.setMinMax(otherRoute.stop_interval_time.getMin(), otherRoute.stop_interval_time.getMax());
	}
	public void merge(RouteStatistics other)
	{
		this.start_time = Math.min(this.start_time, other.start_time);
		this.stop_time = Math.min(this.stop_time, other.stop_time);
		this.total_time += other.total_time;
		this.total_distance += other.total_distance;
		this.total_elevation_gain +=other.total_elevation_gain;
		this.max_speed = Math.max(this.max_speed,other.max_speed);
		this.latitudeExtremities.updateValue(other.latitudeExtremities.getMax());
		this.latitudeExtremities.updateValue(other.latitudeExtremities.getMin());
		this.longitudeExtremities.updateValue(other.longitudeExtremities.getMax());
		this.longitudeExtremities.updateValue(other.longitudeExtremities.getMin());
		this.gradeExtremities.updateValue(other.gradeExtremities.getMax());
		this.gradeExtremities.updateValue(other.gradeExtremities.getMin());
	}
	public RouteStatistics(Parcel source) 
	{
	   readFromParcel(source);
	}
	@Override
	public String toString() 
	{
	    return "RouteStatistics " 
	        +  "{ Start Time: " + getStart_time()
	        + "; Total Time: " + getTotal_time()
	        + "; Moving Time: " + getMoving_time()
	        + "; Total Distance: " + getTotalDistance()
	        + "; Elevation Gain: " + getTotal_elevation_gain()
	        + "; Min Elevation: " + getMinElevation()
	        + "; Max Elevation: " + getMaxElevation()
	        + "; Average Speed: " + getAverageMovingSpeed()
	        + "; Min Grade: " + getMinGrade()
	        + "; Max Grade: " + getMaxGrade()
	        + "}";
	}
	
   
	/**get the average moving speed
	 * @return average moving speed =  total distance / moving time
	 * */
	public double getAverageMovingSpeed() 
	{
	   return  getTotalDistance()/((double) getMoving_time()/1000);	
	}
	/**get the average speed during a trip
	 * @return average speed = total distance/total time
	 * */
	public double getAverageSpeed()
	{
		return getTotalDistance()/((double)getTotal_time()/1000);
	}
	
	public void readFromParcel(Parcel source)
    {
    	//this.max_speed = source.readLong();
    	
    	this.start_time = source.readLong();
    	this.moving_time = source.readLong();
    	this.total_time = source.readLong();
    	this.total_distance = source.readDouble();
    	this.total_elevation_gain = source.readDouble();
    	this.max_speed = source.readDouble();
    	double minLong = source.readDouble();
    	double maxLong = source.readDouble(); 
    	this.latitudeExtremities.setMinMax(minLong, maxLong);
    	double minLat = source.readDouble();
    	double maxLat = source.readDouble();
    	this.longitudeExtremities.setMinMax(minLat,maxLat);
    	double minElevation = source.readDouble();
    	double maxElevation = source.readDouble();
    	this.elevationExtremities.setMinMax(minElevation, maxElevation);
    	double minGrade = source.readDouble();
    	double maxGrade = source.readDouble();
    	this.gradeExtremities.setMinMax(minGrade, maxGrade);
    	double minStartInterval = source.readDouble();
    	double maxStartInterval = source.readDouble();
    	this.start_interval_time.setMinMax(minStartInterval, maxStartInterval);
    	double minStopInterval = source.readDouble();
    	double maxStopInterval = source.readDouble();
    	this.stop_interval_time.setMinMax(minStopInterval, maxStopInterval);
    	
    }
	public int describeContents() 
	{
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags)
	{
	    dest.writeLong(start_time);
	    dest.writeLong(moving_time);
	    dest.writeLong(total_time);
	    dest.writeDouble(total_distance);
	    dest.writeDouble(total_elevation_gain);
	    dest.writeDouble(max_speed);
	    
	    dest.writeDouble(latitudeExtremities.getMin());
	    dest.writeDouble(latitudeExtremities.getMax());
        dest.writeDouble(longitudeExtremities.getMin());
        dest.writeDouble(longitudeExtremities.getMax());
        dest.writeDouble(elevationExtremities.getMin());
        dest.writeDouble(elevationExtremities.getMax());
        dest.writeDouble(gradeExtremities.getMin());
        dest.writeDouble(gradeExtremities.getMax());
        dest.writeDouble(start_interval_time.getMin());
        dest.writeDouble(start_interval_time.getMax());
        dest.writeDouble(stop_interval_time.getMin());
        dest.writeDouble(stop_interval_time.getMax());
	}
	
	Parcelable.Creator<RouteStatistics> CREATOR = new Creator<RouteStatistics>() 
    {

		public RouteStatistics createFromParcel(Parcel source) 
		{
			return new RouteStatistics(source);
		}

		public RouteStatistics[] newArray(int size) 
		{
			return new RouteStatistics[size];
		}
	  
    };
	public long getStart_time() {
		return start_time;
	}

	public void setStart_time(long start_time) {
		this.start_time = start_time;
	}

	public long getStop_time() {
		return stop_time;
	}

	public void setStop_time(long stop_time) {
		this.stop_time = stop_time;
	}

	public long getMoving_time() {
		return moving_time;
	}

	public void setMoving_time(long moving_time) {
		this.moving_time = moving_time;
	}

	public long getTotal_time() {
		return total_time;
	}

	public void setTotal_time(long total_time) {
		this.total_time = total_time;
	}

	public double getTotalDistance() {
		return total_distance;
	}

	public void setTotalDistance(double total_distance) {
		this.total_distance = total_distance;
	}

	public double getTotal_elevation_gain() {
		return total_elevation_gain;
	}

	public void setTotal_elevation_gain(double total_elevation_gain) {
		this.total_elevation_gain = total_elevation_gain;
	}

	public double getMax_speed() {
		return max_speed;
	}

	public void setMax_speed(double max_speed) {
		this.max_speed = max_speed;
	}
	  /**
	   * Returns the leftmost position (lowest longitude) of the track, in signed
	   * decimal degrees.
	   */
	  public int getLowestLongitude() {
	    return (int) (longitudeExtremities.getMin() * 1E6);
	  }

	  /**
	   * Returns the rightmost position (highest longitude) of the track, in signed
	   * decimal degrees.
	   */
	  public int getHighestLongitude() {
	    return (int) (longitudeExtremities.getMax() * 1E6);
	  }

	  /**
	   * Returns the bottommost position (lowest latitude) of the track, in meters.
	   */
	  public int getLowestAltitude() {
	    return (int) (latitudeExtremities.getMin() * 1E6);
	  }

	  /**
	   * Returns the topmost position (highest latitude) of the track, in meters.
	   */
	  public int getHighestAltitude() {
	    return (int) (latitudeExtremities.getMax() * 1E6);
	  }
	  /**
	   * get min elevation
	   * @return min elevation
	   * */
	  public double getMinElevation()
	  {
		  return elevationExtremities.getMin();
	  }
	  /**
	   * gets the max elevation
	   * @return max elevation 
	   * */
	  public double getMaxElevation()
	  {
		  return elevationExtremities.getMax();
	  }
	  /**
	   * set the minimum grade
	   * @param value
	   * */
	  public void setMinElevation(double value)
	  {
		  this.elevationExtremities.setMin(value);
	  }
	  /**
	   * sets the max elevation
	   * @param value
	   */
	  public void setMaxElevation(double value)
	  {
		  this.elevationExtremities.setMax(value);
	  }
	  /**
	   * get min grade
	   * @return min grade
	   */
	  public double getMinGrade()
	  {
		  return gradeExtremities.getMin();
	  }
	  /**
	   * get maximum grade
	   * @return max grade
	   * */
	  public double getMaxGrade()
	  {
		  return gradeExtremities.getMax();
	  }
	  /**
	   * set the min grade
	   * 
	   * @param grade
	   */
	  public void setMinGrade(double grade)
	  {
		  this.gradeExtremities.setMin(grade);
	  }
	  /**
	   set the max grade
	   * @param grade
	   */
	 
	  public void setMaxGrade(double grade)
	  {
		  this.gradeExtremities.setMax(grade);
	  }
	  /**
	   * sets the bounderies with 
	   * @param leftE6 the minim latitude
	   * @param topE6  the maximum latitude
	   * @param rightE6 the minimum longitude
	   * @param bottomE6 the maximum longitude
	   */
	  public void setLatitudeLongitudeBounds(int leftE6, int topE6, int rightE6, int bottomE6) {
		    latitudeExtremities.setMinMax(bottomE6 / 1E6, topE6 / 1E6);
		    longitudeExtremities.setMinMax(leftE6 / 1E6, rightE6 / 1E6);
		  }

		  // Data manipulation methods

		  /**
		   * Adds to the current total distance.
		   *
		   * @param distance the distance to add in meters
		   */
		  public void addTotalDistance(double distance) {
		     total_distance += distance;
		  }

		  /**
		   * Adds to the total elevation variation.
		   *
		   * @param gain the elevation variation in meters
		   */
		  public void addTotalElevationGain(double gain) {
		    total_elevation_gain += gain;
		  }

		  /**
		   * Adds to the total moving time of the trip.
		   *
		   * @param time the time in milliseconds
		   */
		  public void addMovingTime(long time) {
		    moving_time += time;
		  }
		  
		  /**
		   * Accounts for a new latitude value for the bounding box.
		   *
		   * @param latitude the latitude value in signed decimal degrees
		   */
		  public void updateLatitudeExtremities(double latitude) {
		    latitudeExtremities.updateValue(latitude);
		  }

		  /**
		   * Accounts for a new longitude value for the bounding box.
		   *
		   * @param longitude the longitude value in signed decimal degrees
		   */
		  public void updateLongitudeExtremities(double longitude) {
		    longitudeExtremities.updateValue(longitude);
		  }

		  /**
		   * Accounts for a new elevation value for the bounding box.
		   *
		   * @param elevation the elevation value in meters
		   */
		  public void updateElevationExtremities(double elevation) {
		    elevationExtremities.updateValue(elevation);
		  }

		  /**
		   * Accounts for a new grade value.
		   *
		   * @param grade the grade value as a fraction
		   */
		  public void updateGradeExtremities(double grade) {
		    gradeExtremities.updateValue(grade);
		  }

	
	
    
}

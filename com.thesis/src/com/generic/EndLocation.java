package com.generic;

import android.location.Address;

public class EndLocation 
{
	private String name;
	private boolean complete;
	private long id;
	private String description;
	private String address;
	private double latitude;
	private double longitude;
	public EndLocation(String name)
	{
		this.name = name;
		//this.description = mDescription;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getName()
	{
		return name;
	}
	public String toString()
	{
	  return name;	
	}
	public boolean isComplete() 
	{
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	public void setToggleComplete() 
	{	
	   complete = !complete;	
	}
	public long getId()
	{
		return id;
	}
	public void setId(long id) 
	{
	   this.id = id;	
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() 
	{
		return description;
	}
	public boolean hasLocation()
	{
		return(latitude!=0 && longitude!=0);
	}
	public boolean hasAddress()
	{
		return null!=address;
	}
	public void setAddress(String mAddress) {
		this.address = mAddress;
	}
	public void setAddress(Address mAddress)
	{
		if(mAddress==null)
		{
			latitude = longitude = 0;
			address = null;
		}
		else
		{
			int maxAddressLine = mAddress.getMaxAddressLineIndex();
			StringBuffer sb = new StringBuffer();
			for(int i = 0;i < maxAddressLine;i++)
			{
				sb.append(mAddress.getAddressLine(i) + " ");
			}
			address = sb.toString();
			latitude = mAddress.getLatitude();
			longitude =  mAddress.getLongitude();
		}
	}
	public String getAddress() {
		return address;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLongitude() {
		return longitude;
	}

}

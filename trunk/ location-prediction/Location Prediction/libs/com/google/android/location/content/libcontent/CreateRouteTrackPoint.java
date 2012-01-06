package com.google.android.location.content.libcontent;

import android.os.Parcel;
import android.os.Parcelable;

public class CreateRouteTrackPoint implements Parcelable 
{
	public static enum POINT_TYPE
	{
		MARKER,
		STATISTICS;
	}
	private POINT_TYPE type;
	private String name;
	private String description;
	private String iconUrl;
	public final static CreateRouteTrackPoint DEFAULT_MARKER = new CreateRouteTrackPoint(POINT_TYPE.MARKER);
	public final static CreateRouteTrackPoint DEFAULT_STATISTICS = new CreateRouteTrackPoint(POINT_TYPE.STATISTICS);
	public CreateRouteTrackPoint(POINT_TYPE marker) 
	{
	   this.type = marker;
	}
    public CreateRouteTrackPoint(String mName, String mDescription, String mIconUrl)
    {
    	this.name = mName;
    	this.description = mDescription;
    	this.iconUrl = mIconUrl;
    }
    public CreateRouteTrackPoint(Parcel source)
    {
    	readFromParcel(source);
    }
    public void readFromParcel(Parcel source)
    {
    	int position = source.readInt();
    	if(position!=POINT_TYPE.values().length)
    	{
    		throw new IllegalArgumentException("Could not find the type at position" + position);
    	}
    	this.type = POINT_TYPE.values()[position];
        this.description = source.readString();
        this.name = source.readString();
        this.iconUrl = source.readString();
    }
    public void writeToParcel(Parcel dest, int flags) 
	{
	   dest.writeInt(type.ordinal());
	   dest.writeString(description);
	   dest.writeString(name);
	   dest.writeString(iconUrl);
	}
    public POINT_TYPE getType() {
		return type;
	}
	public void setType(POINT_TYPE type) {
		this.type = type;
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
	public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	public static Parcelable.Creator<CreateRouteTrackPoint> CREATOR = new Parcelable.Creator<CreateRouteTrackPoint>() 
    {

		public CreateRouteTrackPoint createFromParcel(Parcel source) 
		{
          return new CreateRouteTrackPoint(source);
		}

		public CreateRouteTrackPoint[] newArray(int size)
		{
			return new CreateRouteTrackPoint[size];
		}
    };
	public int describeContents() 
	{
		return 0;
	}

	

}

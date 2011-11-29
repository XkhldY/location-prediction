package com.google.android.lib.content;

import android.os.Parcel;
import android.os.Parcelable;

public class CreateEndPoint implements Parcelable 
{
	public static enum EndPointType
	{
		MARKER,
		STATISTICS;
	}
	private EndPointType type;
	private String name;
	private String description;
	private String iconUrl;
	public final static CreateEndPoint DEFAULT_MARKER = new CreateEndPoint(EndPointType.MARKER);
	public final static CreateEndPoint DEFAULT_STATISTICS = new CreateEndPoint(EndPointType.STATISTICS);
	public CreateEndPoint(EndPointType marker) 
	{
	   this.type = marker;
	}
    public CreateEndPoint(String mName, String mDescription, String mIconUrl)
    {
    	this.name = mName;
    	this.description = mDescription;
    	this.iconUrl = mIconUrl;
    }
    public CreateEndPoint(Parcel source)
    {
    	readFromParcel(source);
    }
    public void readFromParcel(Parcel source)
    {
    	int position = source.readInt();
    	if(position!=EndPointType.values().length)
    	{
    		throw new IllegalArgumentException("Could not find the type at position" + position);
    	}
    	this.type = EndPointType.values()[position];
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
    public EndPointType getType() {
		return type;
	}
	public void setType(EndPointType type) {
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
	public static Parcelable.Creator<CreateEndPoint> CREATOR = new Parcelable.Creator<CreateEndPoint>() 
    {

		public CreateEndPoint createFromParcel(Parcel source) 
		{
          return new CreateEndPoint(source);
		}

		public CreateEndPoint[] newArray(int size)
		{
			return new CreateEndPoint[size];
		}
    };
	public int describeContents() 
	{
		return 0;
	}

	

}

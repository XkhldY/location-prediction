package com.google.android.lib.content;

import android.os.Parcel;
import android.os.Parcelable;

public class CreateTrack implements Parcelable 
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
	public final static CreateTrack DEFAULT_MARKER = new CreateTrack(EndPointType.MARKER);
	public final static CreateTrack DEFAULT_STATISTICS = new CreateTrack(EndPointType.STATISTICS);
	public CreateTrack(EndPointType marker) 
	{
	   this.type = marker;
	}
    public CreateTrack(String mName, String mDescription, String mIconUrl)
    {
    	this.name = mName;
    	this.description = mDescription;
    	this.iconUrl = mIconUrl;
    }
    public CreateTrack(Parcel source)
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
	public static Parcelable.Creator<CreateTrack> CREATOR = new Parcelable.Creator<CreateTrack>() 
    {

		public CreateTrack createFromParcel(Parcel source) 
		{
          return new CreateTrack(source);
		}

		public CreateTrack[] newArray(int size)
		{
			return new CreateTrack[size];
		}
    };
	public int describeContents() 
	{
		return 0;
	}

	

}

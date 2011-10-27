package com.data.structure;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
@DatabaseTable
public class TimeSchedule implements Serializable
{

	private static final long serialVersionUID = -7133534388607251844L;
   
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(canBeNull = true, foreign=true, columnName="address_id")
	private Address_Location mAddress_Location;
	
	@DatabaseField
	private Date time_start;
	
	@DatabaseField
	private Date time_end;
	
	public TimeSchedule()
	{
		
	}
	public TimeSchedule(Address_Location m_hAddress_Location)
	{
		this.setmAddress_Location(m_hAddress_Location);
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Address_Location getmAddress_Location() {
		return mAddress_Location;
	}
	public void setmAddress_Location(Address_Location mAddress_Location) {
		this.mAddress_Location = mAddress_Location;
	}
	public Date getTime_start() {
		return time_start;
	}
	public void setTime_start(Date time_start) {
		this.time_start = time_start;
	}
	public Date getTime_end() {
		return time_end;
	}
	public void setTime_end(Date time_end) {
		this.time_end = time_end;
	}
}

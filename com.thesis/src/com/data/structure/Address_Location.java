package com.data.structure;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
@DatabaseTable
public class Address_Location implements Serializable
{

	private static final long serialVersionUID = -2639367174349138318L;
    
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(canBeNull = true, foreign=true, columnName = "cell_id")
	private CellPoints mCellLocation;
	
	@DatabaseField(canBeNull = true, foreign=true, columnName = "gps_id")
	private GPSPoints mGpsLocation;

	public Address_Location()
	{
		
	}
	
	public Address_Location(CellPoints  m_hCellLocation, GPSPoints m_hGpsLocation)
	{
		this.mCellLocation = m_hCellLocation;
		this.mGpsLocation = m_hGpsLocation;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public CellPoints getmCellLocation() {
		return mCellLocation;
	}

	public void setmCellLocation(CellPoints mCellLocation) {
		this.mCellLocation = mCellLocation;
	}

	public GPSPoints getmGpsLocation() {
		return mGpsLocation;
	}

	public void setmGpsLocation(GPSPoints mGpsLocation) {
		this.mGpsLocation = mGpsLocation;
	}
	
}

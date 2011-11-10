package com.data.ormstructure;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class CellPoints implements Serializable 
{
	private static final long serialVersionUID = 6026616400367117013L;
    
	@DatabaseField(generatedId = true)
	private Integer id;
	
	@DatabaseField
	private double c_latitude;
	
	@DatabaseField
	private double c_longitude;

	@DatabaseField(canBeNull = true, foreign=true, columnName = "sequence_id")
	private CellSequences cellSequence;
	
	public CellPoints()
	{
		
	}
	public CellPoints(double c_latitude, double c_longitude)
	{
		this.c_latitude = c_latitude;
		this.c_longitude = c_longitude;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public double getLatitude() {
		return c_latitude;
	}

	public void setLtitude(double c_latitude) {
		this.c_latitude = c_latitude;
	}

	public double getLongitude() {
		return c_longitude;
	}

	public void setLongitude(double c_longitude) {
		this.c_longitude = c_longitude;
	}
	public CellSequences getSequence() {
		return cellSequence;
	}
	public void setSequence(CellSequences cellSequence) {
		this.cellSequence = cellSequence;
	}
    

	
}

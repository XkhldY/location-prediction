package com.data.ormstructure;

import java.io.Serializable;

import com.interfaces.ISequence;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public abstract class GpsSequences implements Serializable, ISequence 
{

	private static final long serialVersionUID = 6741096629549249457L;
    
	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField
	private String startPointAddress;
	
	@DatabaseField 
	private String endPointAddress;
	@DatabaseField
	private int nrOfTimesUsed;
	public GpsSequences()
	{
		
	}
	
	public GpsSequences(String startPoint)
	{
		this.startPointAddress = startPoint;
		this.endPointAddress = null;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStartPoint() {
		return startPointAddress;
	}

	public void setStartPoint(String startPointAddress) {
		this.startPointAddress = startPointAddress;
	}

	public String getEndPoint() {
		return endPointAddress;
	}

	public void setEndPoint(String endPointAddress) {
		this.endPointAddress = endPointAddress;
	}

	public int getNrOfTimesUsed() {
		return nrOfTimesUsed;
	}

	public void setNrOfTimesUsed(int nrOfTimesUsed) {
		this.nrOfTimesUsed = nrOfTimesUsed;
	}
	
}
package com.data.structure;

import java.io.Serializable;

import com.interfaces.Points;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable
public abstract class GPSPoints implements Serializable, Points
{
   
	private static final long serialVersionUID = 1551422019312099487L;

	@DatabaseField(generatedId = true)
    private Integer id;
    
    @DatabaseField
    private double g_latitutde;
    
    @DatabaseField
    private double g_longitude;
    
   @DatabaseField(canBeNull = true, foreign = true,columnName = "sequence_id")
   private GpsSequences mGPSSequence;
   
   public GPSPoints()
   {
   	
   }
   
   public GPSPoints(double mLatitude, double mLongitude)
    {
    	this.g_latitutde = mLatitude;
    	this.g_longitude = mLongitude;
    
    }

    
    public Integer getId() {
		return id;
	}

	public double getLatitutde() {
		return g_latitutde;
	}

	public void setLatitutde(double latitutde) {
		this.g_latitutde = latitutde;
	}

	public double getLongitude() {
		return g_longitude;
	}

	public void setLongitude(double longitude) {
		this.g_longitude = longitude;
	}
	public GpsSequences getSequence() {
		return mGPSSequence;
	}
	public void setSequence(GpsSequences mGPSSequence) {
		this.mGPSSequence = mGPSSequence;
	}

	

	

	
   
}

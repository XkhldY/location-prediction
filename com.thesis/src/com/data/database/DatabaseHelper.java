package com.data.database;

import java.sql.SQLException;

import com.data.structure.Address_Location;
import com.data.structure.CellPoints;
import com.data.structure.CellSequences;
import com.data.structure.GPSPoints;
import com.data.structure.GpsSequences;
import com.data.structure.TimeSchedule;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;



public class DatabaseHelper extends OrmLiteSqliteOpenHelper 
{

	private static final String TAG = DatabaseHelper.class.getName();
	private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "datapoints.sql";
	public DatabaseHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource mConnection) 
	{
		try 
		{
			TableUtils.createTable(mConnection, CellPoints.class);
			TableUtils.createTable(mConnection, GPSPoints.class);
			TableUtils.createTable(mConnection, GpsSequences.class);
			TableUtils.createTable(mConnection, TimeSchedule.class);
			TableUtils.createTable(mConnection, Address_Location.class);
			TableUtils.createTable(mConnection, CellSequences.class);
		} 
		catch (SQLException e) 
		{		
			Log.e(TAG,"Unable to create database", null);
			e.printStackTrace();
		}
		
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource mConnection, int oldVersion,
			int newVersion) 
	{
	   	try 
	   	{
			TableUtils.dropTable(mConnection, CellPoints.class,  false);
			TableUtils.dropTable(mConnection, GPSPoints.class, false);
		   	TableUtils.dropTable(mConnection, Address_Location.class, false);
		   	TableUtils.dropTable(mConnection, CellSequences.class, false);
		   	TableUtils.dropTable(mConnection, TimeSchedule.class, false);
		   	TableUtils.dropTable(mConnection, GpsSequences.class, false);
		   	onCreate(db, mConnection);
	   	} 
	   	catch (SQLException e) 
		{
	   		Log.e(TAG, "Unable to upgrade the database fro version " + oldVersion + " to " + newVersion);
			e.printStackTrace();
		}
	   	
	}

	

}

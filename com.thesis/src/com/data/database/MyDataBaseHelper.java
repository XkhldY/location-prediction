package com.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.DropBoxManager;

public class MyDataBaseHelper extends SQLiteOpenHelper 
{

	public static final String TAG = "Database";
	public static int version  = 1;
	public static final String DB_NAME = "points_db.sqlite";
	public static final String GPS_TABLE = "gps_table";
	//creating the columns in the table gps
	public static final String ID_GPS = "id_gps";
	public static final String G_LAT = "g_lat";
	public static final String G_LONG ="g_long";
	public static final String G_ID_SEQUENCE = "g_id_seq";
	//creating cell_table
	public static final String CELL_TABLE = "cell_table";
	//creating the columngs inside the cell_table
	public static final String ID_CELL = "id_cell";
	public static final String C_LAT = "c_lat";
	public static final String C_LONG = "c_long";
	public static final String C_ID_SEQUENCE = "c_id_sequence";
	//creating the address table
    public static final String ADDRESS_TABLE= "address_table";
    public static final String ADDRESS_ID = "id_address";
    public static final String ADDRESS_ID_GPS = "id_gps";
    public static final String ADDRESS_ID_CELL = "id_cell";
    public static final String ADDRESS_NAME = "address_name";
    //creating Time Table
    public static final String TIME_TABLE = "time_table";
    //creating the columngs
    public static final String TIME_ID = "time_id";
    public static final String TIME_ID_ADDRESS = "time_id_address";
    public static final String TIME_START = "time_start";
    public static final String TIME_END = "time_end";
    //Creating the gps sequences
    public static final String GPS_SEQUENCE = "gps_sequences";
    //creating the colunmns
    public static final String GPS_ID_SEQUENCE = "gps_id_sequence";
    public static final String GPS_START_ADDRESS = "start_address";
    public static final String GPS_END_ADDRESS="end_address";
    //creatint the cell sequence table
    public static final String CELL_SEQUENCE = "cell_sequences";
    //creating the columns
    public static final String CELL_ID_SEQUENCE="cell_id_sequence";
    public static final String CELL_START_ADDRESS = "cell_start_address";
    public static final String CELL_END_ADDRESS = "cell_end_address";
    //creating the querries
    //query 1 gps_table query
    public static final String gps_query = "create table " + GPS_TABLE + " (" + ID_GPS + " integer primary key autoincrement not null,"
    														 + G_LAT + " text not null," 
    														 + G_LONG +  " text not null,"
    														 + G_ID_SEQUENCE + " text not null, FOREIGN KEY("+ G_ID_SEQUENCE + ")REFERENCES " 
    														 + GPS_SEQUENCE + " (" + GPS_ID_SEQUENCE + "));";
    //query 2
    //cell_id table
    public static final String cell_query = "create table " + CELL_TABLE + " (" + ID_CELL + " integer primary key autoincrement not null,"
			  												+ C_LAT + " text not null," 
			  												+ C_LONG +  " text not null,"
			  												+ TIME_START +  " text,"
			  												+ TIME_END + " text," 
			  												+ C_ID_SEQUENCE + " text not null , FOREIGN KEY(" + C_ID_SEQUENCE +") REFERENECES " 
			  												+ CELL_SEQUENCE + " (" + CELL_ID_SEQUENCE + "));";
    //query 3
    //address_table query
    public static final String address_table_query = "create table " + ADDRESS_TABLE + " (" + ADDRESS_ID + " integer primary key autoincrement not null,"
			  													+ ADDRESS_ID_GPS + " integer not null, FOREIGN KEY(" + ADDRESS_ID_GPS +")REFERENCES " 
			  													+ GPS_TABLE + " ("+ ID_GPS +"),"  
			  													+ ADDRESS_ID_CELL +  " integer not null, FOREIGN KEY(" + ADDRESS_ID_CELL + ")REFERENCES "
			  													+ CELL_TABLE + "(" + ID_CELL + "),"
			  													+ ADDRESS_NAME + " text" + ")";
    //query 5
    //query for time table
    public static final String time_table_query = "create table " + TIME_TABLE + " (" + TIME_ID + " integer primary key autoincrement not null,"
																+ TIME_ID_ADDRESS + " text not null, FOREIGN KEY(" + TIME_ID_ADDRESS + ")REFERENCES "
																+ ADDRESS_TABLE + " (" + ADDRESS_ID + "),"
																+ TIME_START +  " text,"
																+ TIME_END + " text" + ")";
    //query 6
    //query for gps sequences
    public static final String gps_sequences_query = "create table " + GPS_SEQUENCE + " (" + GPS_ID_SEQUENCE + " integer primary key autoincrement not null,"
																	 + GPS_START_ADDRESS + " text," 
																	 + GPS_END_ADDRESS +  " text" + ")";
    //query 7
    //query for cell sequences
    public static final String cell_sequences_query = "create table " + CELL_SEQUENCE + " (" + CELL_ID_SEQUENCE + " integer primary key autoincrement not null,"
			 														  + CELL_START_ADDRESS + " text," 
			 														  + CELL_END_ADDRESS +  " text" + ")";
    public MyDataBaseHelper(Context context, String name, CursorFactory factory, int version) 
	{
		super(context, DB_NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
	  db.execSQL(gps_query);
      db.execSQL(cell_query);
      db.execSQL(address_table_query);
      db.execSQL(time_table_query);
      db.execSQL(gps_sequences_query);
      db.execSQL(cell_sequences_query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
	    
	}

}

package com.google.android.lib.content;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;

public interface RoutesColumns extends BaseColumns 
{
   public static final Uri  CONTENT_URI  = Uri.parse("content://com.google.android.maps.myroutes/routes");
   public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.route";
   public static final String CONTENT_ITEMTYPE = "vnd.android.cursor.item/vnd.google.route";
   public static final String DEFAULT_SORT_OREDER_ID = "_id";
   public static final String TIME_SORT_ORDER = "start_time";
   
   //columns
   public static final String NAME = "name";
   public static final String DESCRIPTION = "description";
   public static final String CATEGORY = "cateogy";
   public static final String START_ID ="start_id";
   public static final String STOP_ID = "stop_id";
   public static final String START_TIME = "start_time";
   public static final String STOP_TIME = "stop_time";
   /* to be implemented
   //public static final String NUMBER_OF_STOPS = "nr_of_stops";
   //public static final String NUMBER_OF_SUB_ROUTES = "nr_of_subroutes";
   */
   public static final String NUMBER_POINTS = "number_points";
   public static final String NUMBER_TIMES= "number_times";  
   public static final String TOTAL_DISTANCE= "total_distance";
   public static final String TOTAL_TIME = "total_time";
   public static final String MOVING_TIME = "moving_time";
   public static final String AVG_SPEED = "avg_speed";
   public static final String AVG_MOVING_SPEED = "avg_moving_speed";
   public static final String MAX_SPEED = "max_speed";
   public static final String MAX_ELEVATION = "max_elevation";
   public static final String MIN_ELEVATION = "min_elevation";
   public static final String ELEVATION_GAIN_CURRENT = "elevation_gain_current";
   public static final String MIN_GRADE = "min_grade";
   public static final String MAX_GRADE = "min_grade";
   public static final String MIN_LAT = "min_lat";
   public static final String MAX_LAT = "max_lat";
   public static final String MAX_LONG ="max_long";
   public static final String MIN_LONG = "min_long";
   public static final String MAP_ID = "map_id";
   public static final String TABLE_ID = "table_id";   
}
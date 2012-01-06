package com.google.android.location.content.libcontent;

import com.google.android.location.content.libdata.MyRouteProvider;

import android.net.Uri;
import android.provider.BaseColumns;

public interface RoutesPointsLocations extends BaseColumns 
{
    public static final Uri CONTENT_URI = Uri.parse("content://com.google.android.maps.myroutes/routepoints");
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.routepoint";
    public static final String CONTENT_ITEMTYPE = "vnd.android.cursor.item/vnd.google.routepoint";
    //columns
    public static final String DEFAULT_SORT_ORDER = "_id";

    public static final String ROUTE_ID = "route_id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ALTITUDE = "altitude";
    public static final String BEARING  = "bearing";
    public static final String TIME = "time";
    public static final String ACCURACY = "accuracy";
    public static final String SPEED = "speed";
    public static final String TIMES_COUNT = "times_count";
    public static final String IDLE_TIME = "idle_time";
   /* public static final String SENSOR_TYPE_GPS = "sensor_gps";
    //public static final String SENSOR_TYPE_NETWORK = "sensor_network";
    //to be implemented
    */
    
    
}

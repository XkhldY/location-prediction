package com.google.android.location.content.libcontent;

import com.google.android.location.content.libdata.MyRouteProvider;

import android.net.Uri;
import android.provider.BaseColumns;

public interface RoutesTrackPointsColumns extends BaseColumns {
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.google.android.maps.myroutes/routetrackpoints");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.routetrackpoint";
	public static final String CONTENT_ITEMTYPE = "vnd.android.cursor.item/vnd.google.routetrackpoint";
	public static final String DEFAULT_SORT_ORDER = "_id";
	public static final String TIME_SORT_ORDER = "START_TIME";
	
	//columnws
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String CATEGORY = "cateogy";
	public static final String ICON_TRACK = "icon_track";
	public static final String TYPE = "type";
	public static final String ROUTE_ID = "route_id";
	public static final String START_ID = "start_id";
	public static final String STOP_ID = "stop_id";
	public static final String START_TIME = "start_time";
	public static final String STOP_TIME = "stop_time";
	public static final String ACCURACY = "accuracy";
	public static final String BEARING = "bearing";
	public static final String SPEED = "speed";
	public static final String TRACK_DURATION="track_duration";
	public static final String NUMBER_TIMES = "number_times";
	/*
	 * to be implemented //public static final String NUMBER_OF_STOPS =
	 * "nr_of_stops"; //public static final String NUMBER_OF_SUB_ROUTES =
	 * "nr_of_subroutes"; //public static final String NUMBER_POINTS =
	 * "numpoints";
	 */
	public static final String TIMES_COUNT = "times_count";
	public static final String IDLE_TIME = "idle_time";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String ALTITUDE = "altitude";
	public static final String TOTAL_DISTANCE = "total_distance";
	public static final String TIME = "time";
	public static final String LEGNTH_OF_TRACK = "length_of_track";
	public static final String TOTAL_TIME = "total_time";
	public static final String MOVING_TIME = "movingtime";
	public static final String AVG_SPEED = "avgspeed";
	public static final String AVG_MOVING_SPEED = "avg_moving_speed";
	public static final String MAX_SPEED = "max_speed";
	public static final String MAX_ELEVATION = "max_elevation";
	public static final String MIN_ELEVATION = "min_elevation";
	public static final String ELEVATION_GAIN_CURRENT = "elvation_gain_current";
	public static final String MIN_GRADE = "min_grade";
	public static final String MAX_GRADE = "max_grade";

	public static final String MAP_ID = "mapid";
}

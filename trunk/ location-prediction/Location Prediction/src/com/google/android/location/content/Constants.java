package com.google.android.location.content;


public class Constants 
{
public static final String TAG = "MyTracks";

/**
 * Name of the top-level directory inside the SD card where our files will
 * be read from/written to.
 */
public static final String SDCARD_TOP_DIR = "MyTracks";

/*
 * onActivityResult request codes:
 */

public static final int GET_LOGIN = 0;
public static final int GET_MAP = 1;
public static final int SHOW_TRACK = 2;
public static final int AUTHENTICATE_TO_MY_MAPS = 3;
public static final int AUTHENTICATE_TO_FUSION_TABLES = 4;
public static final int AUTHENTICATE_TO_DOCLIST = 5;
public static final int AUTHENTICATE_TO_TRIX = 6;
public static final int SHARE_GPX_FILE = 7;

public static final int SAVE_GPX_FILE = 11;
public static final int SHOW_WAYPOINT = 15;
public static final int WELCOME = 16;

/*
 * Menu ids:
 */

public static final int MENU_MY_LOCATION = 1;
public static final int MENU_TOGGLE_LAYERS = 2;
public static final int MENU_CHART_SETTINGS = 3;

/*
 * Context menu ids:
 */

public static final int MENU_EDIT = 100;
public static final int MENU_DELETE = 101;
public static final int MENU_SEND_TO_GOOGLE = 102;
public static final int MENU_SHARE = 103;
public static final int MENU_SHOW = 104;
public static final int MENU_SHARE_LINK = 200;
public static final int MENU_SHARE_GPX_FILE = 201;
public static final int MENU_WRITE_TO_SD_CARD = 205;
public static final int MENU_SAVE_GPX_FILE = 206;
public static final int MENU_CLEAR_MAP = 210;

/**
 * the minimum required idle time from where we can conclude this can be an end
 * location
 */
public static final int minRequiredIdleTime = 5000;
/**
 * The number of distance readings to smooth to get a stable signal.
 */
public static final int DISTANCE_SMOOTHING_FACTOR = 25;

/**
 * The number of elevation readings to smooth to get a somewhat accurate
 * signal.
 */
public static final int ELEVATION_SMOOTHING_FACTOR = 25;

/**
 * The number of grade readings to smooth to get a somewhat accurate signal.
 */
public static final int GRADE_SMOOTHING_FACTOR = 5;

/**
 * The number of speed reading to smooth to get a somewhat accurate signal.
 */
public static final int SPEED_SMOOTHING_FACTOR = 25;

/**
 * Maximum number of track points displayed by the map overlay.
 */
public static final int MAX_DISPLAYED_ROUTE_POINTS = 10000;

/**
 * Target number of track points displayed by the map overlay.
 * We may display more than this number of points.
 */
public static final int TARGET_DISPLAYED_ROUTE_POINTS = 5000;

/**
 * Maximum number of route points ever loaded at once from the provider into
 * memory.
 * With a recording frequency of 2 seconds, 15000 corresponds to 8.3 hours.
 */
public static final int MAX_LOADED_ROUTE_POINTS = 20000;

/**
 * Maximum number of track points ever loaded at once from the provider into
 * memory in a single call to read points.
 */
public static final int MAX_LOADED_TRACK_POINTS_PER_BATCH = 1000;

/**
 * Maximum number of way points displayed by the map overlay.
 */
public static final int MAX_DISPLAYED_ROUTE_TRACK_POINTS = 128;

/**
 * Maximum number of way points that will be loaded at one time.
 */
public static final int MAX_LOADED_TRACK_POINTS = 10000;

/**
 * Any time segment where the distance traveled is less than this value will
 * not be considered moving.
 */
public static final double MAX_NO_MOVEMENT_DISTANCE = 2;

/**
 * Anything faster than that (in meters per second) will be considered moving.
 */
public static final double MAX_NO_MOVEMENT_SPEED = 0.224;

/**
 * Ignore any acceleration faster than this.
 * Will ignore any speeds that imply accelaration greater than 2g's
 * 2g = 19.6 m/s^2 = 0.0002 m/ms^2 = 0.02 m/(m*ms)
 */
public static final double MAX_ACCELERATION = 0.02;

/** Maximum age of a GPS location to be considered current. */
public static final long MAX_LOCATION_AGE_MS = 60 * 1000;  // 1 minute

/** Maximum age of a network location to be considered current. */
public static final long MAX_NETWORK_AGE_MS = 1000 * 60 * 10;  // 10 minutes

/**
 * The type of account that we can use for gdata uploads.
 */
public static final String ACCOUNT_TYPE = "com.google";

/**
 * The name of extra intent property to indicate whether we want to resume
 * a previously recorded track.
 */
public static final String RESUME_ROUTE_EXTRA_NAME =
    "com.google.android.location.content.RESUME_ROUTE";


/*
 * Default values - keep in sync with those in preferences.xml.
 */

public static final int DEFAULT_ANNOUNCEMENT_FREQUENCY = -1;
public static final int DEFAULT_AUTO_RESUME_ROUTE_TIMEOUT = 10;  // In min.
public static final int DEFAULT_MAX_RECORDING_DISTANCE = 200;
public static final int DEFAULT_MIN_RECORDING_DISTANCE = 5;
public static final int DEFAULT_MIN_RECORDING_INTERVAL = 0;
public static final int DEFAULT_MIN_REQUIRED_ACCURACY = 200;


public static final String SETTINGS_NAME = "SettingsStartActivity";

/**
 * This is an abstract utility class.
 */
protected Constants() 
{
	}
}

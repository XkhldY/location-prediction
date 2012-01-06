package com.google.android.location.content.route;

import com.google.android.locatiom.maps.myroutes.R;
import com.google.android.location.content.Constants;
import com.google.android.location.content.libcontent.RoutesColumns;
import com.google.android.location.content.libcontent.RoutesPointsLocations;
import com.google.android.location.content.libcontent.RoutesTrackPointsColumns;
import com.google.android.location.content.libdata.MyRouteProvider;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;



/**
 * A provider that handles recorded (GPS) tracks and their track points.
 *
 * @author Andrei
 */
public class MyRouteTrackDataProvider extends ContentProvider {

  private static final String DATABASE_NAME = "myroutes.db";
  private static final int DATABASE_VERSION = 19;
  private static final int ROUTEPOINTS = 1;
  private static final int ROUTEPOINTS_ID = 2;
  private static final int ROUTES = 3;
  private static final int ROUTES_ID = 4;
  private static final int ROUTESTRACKPOINTS = 5;
  private static final int ROUTESTRACKPOINTS_ID = 6;
  private static final String ROUTEPOINTS_TABLE = "routepoints";
  private static final String ROUTES_TABLE = "routes";
  private static final String ROUTESTRACKPOINTS_TABLE = "routetrackpoints";


  /**
   * Helper which creates or upgrades the database if necessary.
   */
  private static class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + ROUTEPOINTS_TABLE + " ("
          + RoutesPointsLocations._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
          + RoutesPointsLocations.ROUTE_ID + " INTEGER, "
          + RoutesPointsLocations.LONGITUDE + " INTEGER, "
          + RoutesPointsLocations.LATITUDE + " INTEGER, "
          + RoutesPointsLocations.TIME + " INTEGER, "
          + RoutesPointsLocations.ALTITUDE + " FLOAT, "
          + RoutesPointsLocations.ACCURACY + " FLOAT, "
          + RoutesPointsLocations.SPEED + " FLOAT, "
          + RoutesPointsLocations.BEARING + " FLOAT, "  
          + RoutesPointsLocations.TIMES_COUNT + " INTEGER, "
          + RoutesPointsLocations.IDLE_TIME + " FLOAT);");
      db.execSQL("CREATE TABLE " + ROUTES_TABLE + " ("
          + RoutesColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
          + RoutesColumns.NAME + " STRING, "
          + RoutesColumns.DESCRIPTION + " STRING, "
          + RoutesColumns.CATEGORY + " STRING, "
          + RoutesColumns.START_ID + " INTEGER, "
          + RoutesColumns.STOP_ID + " INTEGER, "
          + RoutesColumns.START_TIME + " INTEGER, "
          + RoutesColumns.STOP_TIME + " INTEGER, "
          + RoutesColumns.NUMBER_POINTS + " INTEGER, "
          + RoutesColumns.TOTAL_DISTANCE + " FLOAT, "
          + RoutesColumns.TOTAL_TIME + " INTEGER, "
          + RoutesColumns.MOVING_TIME + " INTEGER, "
          + RoutesColumns.MIN_LAT + " INTEGER, "
          + RoutesColumns.MAX_LAT + " INTEGER, "
          + RoutesColumns.MIN_LONG + " INTEGER, "
          + RoutesColumns.MAX_LONG + " INTEGER, "
          + RoutesColumns.AVG_SPEED + " FLOAT, "
          + RoutesColumns.AVG_MOVING_SPEED + " FLOAT, "
          + RoutesColumns.MAX_SPEED + " FLOAT, "
          + RoutesColumns.MIN_ELEVATION + " FLOAT, "
          + RoutesColumns.MAX_ELEVATION + " FLOAT, "
          + RoutesColumns.ELEVATION_GAIN_CURRENT + " FLOAT, "
          + RoutesColumns.MIN_GRADE + " FLOAT, "
          + RoutesColumns.MAX_GRADE + " FLOAT, "
          + RoutesColumns.TIMES_COUNT + " INTEGER, " 
          + RoutesColumns.IDLE_TIME + " FLOAT, "
          + RoutesColumns.MAP_ID + " STRING, "
          + RoutesColumns.TABLE_ID + " STRING);");
      db.execSQL("CREATE TABLE " + ROUTESTRACKPOINTS_TABLE + " ("
          + RoutesTrackPointsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
          + RoutesTrackPointsColumns.NAME + " STRING, "
          + RoutesTrackPointsColumns.DESCRIPTION + " STRING, "
          + RoutesTrackPointsColumns.CATEGORY + " STRING, "
          + RoutesTrackPointsColumns.ICON_TRACK + " STRING, "
          + RoutesTrackPointsColumns.ROUTE_ID + " INTEGER, "
          + RoutesTrackPointsColumns.TYPE + " INTEGER, "
          + RoutesTrackPointsColumns.LEGNTH_OF_TRACK + " FLOAT, "
          + RoutesTrackPointsColumns.TRACK_DURATION + " INTEGER, "
          + RoutesTrackPointsColumns.START_TIME + " INTEGER, "
          + RoutesTrackPointsColumns.START_ID + " INTEGER, "
          + RoutesTrackPointsColumns.STOP_ID + " INTEGER, "
          + RoutesTrackPointsColumns.LONGITUDE + " INTEGER, "
          + RoutesTrackPointsColumns.LATITUDE + " INTEGER, "
          + RoutesTrackPointsColumns.TIME + " INTEGER, "
          + RoutesTrackPointsColumns.ALTITUDE + " FLOAT, "
          + RoutesTrackPointsColumns.ACCURACY + " FLOAT, "
          + RoutesTrackPointsColumns.SPEED + " FLOAT, "
          + RoutesTrackPointsColumns.BEARING + " FLOAT, "
          + RoutesTrackPointsColumns.TOTAL_DISTANCE + " FLOAT, "
          + RoutesTrackPointsColumns.TOTAL_TIME + " INTEGER, "
          + RoutesTrackPointsColumns.MOVING_TIME + " INTEGER, "
          + RoutesTrackPointsColumns.AVG_SPEED + " FLOAT, "
          + RoutesTrackPointsColumns.AVG_MOVING_SPEED + " FLOAT, "
          + RoutesTrackPointsColumns.MAX_SPEED + " FLOAT, "
          + RoutesTrackPointsColumns.MIN_ELEVATION + " FLOAT, "
          + RoutesTrackPointsColumns.MAX_ELEVATION + " FLOAT, "
          + RoutesTrackPointsColumns.MAX_GRADE + " FLOAT," 
          + RoutesTrackPointsColumns.MIN_GRADE + " FLOAT, "
          + RoutesTrackPointsColumns.ELEVATION_GAIN_CURRENT + " FLOAT, "
          + RoutesTrackPointsColumns.TIMES_COUNT + " INTEGER, "
          + RoutesTrackPointsColumns.IDLE_TIME + " FLOAT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (oldVersion < 17) {
        // Wipe the old data.
        Log.w(Constants.TAG, "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + ROUTEPOINTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ROUTES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ROUTESTRACKPOINTS_TABLE);
        onCreate(db);
      } else {
        // Incremental updates go here.
        // Each time you increase the DB version, add a corresponding if clause.
        Log.w(Constants.TAG, "Upgrading database from version " + oldVersion + " to "
            + newVersion);

        // Sensor data.
       
        
        if (oldVersion <= 17) {
          Log.w(Constants.TAG, "Upgrade DB: Adding tableid column.");
          db.execSQL("ALTER TABLE " + ROUTES_TABLE
              + " ADD " + RoutesColumns.TABLE_ID + " STRING");
        }
      }
    }
  }

  private final UriMatcher urlMatcher;

  private SQLiteDatabase db;

  public MyRouteTrackDataProvider() {
    urlMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    urlMatcher.addURI(com.google.android.location.content.libdata.MyRouteProvider.AUTHORITY,
        "routepoints", ROUTEPOINTS);
    urlMatcher.addURI(MyRouteProvider.AUTHORITY,
        "trackpoints/#", ROUTEPOINTS_ID);
    urlMatcher.addURI(MyRouteProvider.AUTHORITY, "routes", ROUTES);
    urlMatcher.addURI(MyRouteProvider.AUTHORITY, "routes/#", ROUTES_ID);
    urlMatcher.addURI(MyRouteProvider.AUTHORITY, "routetrackpoints", ROUTESTRACKPOINTS);
    urlMatcher.addURI(MyRouteProvider.AUTHORITY,
        "routetrackpoints/#", ROUTESTRACKPOINTS_ID);
  }

  private boolean canAccess() {
    if (Binder.getCallingPid() == Process.myPid()) {
      return true;
    } else {
      Context context = getContext();
      SharedPreferences sharedPreferences = context.getSharedPreferences(
          Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
      return sharedPreferences.getBoolean(context.getString(R.string.allow_access_key), false);
    }
  }
  
  @Override
  public boolean onCreate() {
    if (!canAccess()) {
      return false;
    }
    DatabaseHelper dbHelper = new DatabaseHelper(getContext());
    try {
      db = dbHelper.getWritableDatabase();
    } catch (SQLiteException e) {
      Log.e(Constants.TAG, "Unable to open database for writing", e);
    }
    return db != null;
  }

  @Override
  public int delete(Uri url, String where, String[] selectionArgs) {
    if (!canAccess()) {
      return 0;
    }
    String table;
    boolean shouldVacuum = false;
    switch (urlMatcher.match(url)) {
      case ROUTEPOINTS:
        table = ROUTEPOINTS_TABLE;
        break;
      case ROUTES:
        table = ROUTES_TABLE;
        shouldVacuum = true;
        break;
      case ROUTESTRACKPOINTS:
        table = ROUTESTRACKPOINTS_TABLE;
        break;
      default:
        throw new IllegalArgumentException("Unknown URL " + url);
    }

    Log.w(Constants.TAG, "provider delete in " + table + "!");
    int count = db.delete(table, where, selectionArgs);
    getContext().getContentResolver().notifyChange(url, null, true);

    if (shouldVacuum) {
      // If a potentially large amount of data was deleted, we want to reclaim its space.
      Log.i(Constants.TAG, "Vacuuming the database");
      db.execSQL("VACUUM");
    }

    return count;
  }

  @Override
  public String getType(Uri url) {
    if (!canAccess()) {
      return null;
    }
    switch (urlMatcher.match(url)) {
      case ROUTEPOINTS:
        return RoutesPointsLocations.CONTENT_TYPE;
      case ROUTEPOINTS_ID:
        return RoutesPointsLocations.CONTENT_ITEMTYPE;
      case ROUTES:
        return RoutesColumns.CONTENT_TYPE;
      case ROUTES_ID:
        return RoutesColumns.CONTENT_ITEMTYPE;
      case ROUTESTRACKPOINTS:
        return RoutesTrackPointsColumns.CONTENT_TYPE;
      case ROUTESTRACKPOINTS_ID:
        return RoutesTrackPointsColumns.CONTENT_ITEMTYPE;
      default:
        throw new IllegalArgumentException("Unknown URL " + url);
    }
  }

  @Override
  public Uri insert(Uri url, ContentValues initialValues) {
    if (!canAccess()) {
      return null;
    }
    Log.d(Constants.TAG, "MyRouteProvider.insert");
    ContentValues values;
    if (initialValues != null) {
      values = initialValues;
    } else {
      values = new ContentValues();
    }

    int urlMatchType = urlMatcher.match(url);
    return insertType(url, urlMatchType, values);
  }

  private Uri insertType(Uri url, int urlMatchType, ContentValues values) {
    switch (urlMatchType) {
      case ROUTEPOINTS:
        return insertRoutePoint(url, values);
      case ROUTES:
        return insertRoute(url, values);
      case ROUTESTRACKPOINTS:
        return insertRouteTrackPoint(url, values);
      default:
        throw new IllegalArgumentException("Unknown URL " + url);
    }
  }


  @Override
  public int bulkInsert(Uri url, ContentValues[] valuesBulk) {
    if (!canAccess()) {
      return 0;
    }
    Log.d(Constants.TAG, "MyRoutesProvider.insertMany");
    int numInserted = 0;
    try {
      // Use a transaction in order to make the insertions run as a single batch
      db.beginTransaction();

      int urlMatch = urlMatcher.match(url);
      for (numInserted = 0; numInserted < valuesBulk.length; numInserted++) {
        ContentValues values = valuesBulk[numInserted];
        if (values == null) { values = new ContentValues(); }

        insertType(url, urlMatch, values);
      }

      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }

    return numInserted;
  }

  private Uri insertRoutePoint(Uri url, ContentValues values) {
    boolean hasLat = values.containsKey(RoutesPointsLocations.LATITUDE);
    boolean hasLong = values.containsKey(RoutesPointsLocations.LONGITUDE);
    boolean hasTime = values.containsKey(RoutesPointsLocations.TIME);
    if (!hasLat || !hasLong || !hasTime) {
      throw new IllegalArgumentException(
          "Latitude, longitude, and time values are required.");
    }
    long rowId = db.insert(ROUTEPOINTS_TABLE, RoutesPointsLocations._ID, values);
    if (rowId >= 0) {
      Uri uri = ContentUris.appendId(
          RoutesPointsLocations.CONTENT_URI.buildUpon(), rowId).build();
      getContext().getContentResolver().notifyChange(url, null, true);
      return uri;
    }
    throw new SQLiteException("Failed to insert row into " + url);
  }

  private Uri insertRoute(Uri url, ContentValues values) {
    boolean hasStartTime = values.containsKey(RoutesColumns.START_TIME);
    boolean hasStartId = values.containsKey(RoutesColumns.START_ID);
    if (!hasStartTime || !hasStartId) {
      throw new IllegalArgumentException(
          "Both start time and start id values are required.");
    }
    long rowId = db.insert(ROUTES_TABLE, RoutesColumns._ID, values);
    if (rowId > 0) {
      Uri uri = ContentUris.appendId(
          RoutesColumns.CONTENT_URI.buildUpon(), rowId).build();
      getContext().getContentResolver().notifyChange(url, null, true);
      return uri;
    }
    throw new SQLException("Failed to insert row into " + url);
  }

  private Uri insertRouteTrackPoint(Uri url, ContentValues values) {
    long rowId = db.insert(ROUTESTRACKPOINTS_TABLE, RoutesTrackPointsColumns._ID, values);
    if (rowId > 0) {
      Uri uri = ContentUris.appendId(
          RoutesTrackPointsColumns.CONTENT_URI.buildUpon(), rowId).build();
      getContext().getContentResolver().notifyChange(url, null, true);
      return uri;
    }
    throw new SQLException("Failed to insert row into " + url);
  }

  @Override
  public Cursor query(
      Uri url, String[] projection, String selection, String[] selectionArgs,
      String sort) {
    if (!canAccess()) {
      return null;
    }
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    int match = urlMatcher.match(url);
    String sortOrder = null;
    if (match == ROUTEPOINTS) {
      qb.setTables(ROUTEPOINTS_TABLE);
      if (sort != null) {
        sortOrder = sort;
      } else {
        sortOrder = RoutesPointsLocations.DEFAULT_SORT_ORDER;
      }
    } else if (match == ROUTEPOINTS_ID) {
      qb.setTables(ROUTEPOINTS_TABLE);
      qb.appendWhere("_id=" + url.getPathSegments().get(1));
    } else if (match == ROUTES) {
      qb.setTables(ROUTES_TABLE);
      if (sort != null) {
        sortOrder = sort;
      } else {
        sortOrder = RoutesColumns.DEFAULT_SORT_OREDER_ID;
      }
    } else if (match == ROUTES_ID) {
      qb.setTables(ROUTES_TABLE);
      qb.appendWhere("_id=" + url.getPathSegments().get(1));
    } else if (match == ROUTESTRACKPOINTS) {
      qb.setTables(ROUTESTRACKPOINTS_TABLE);
      if (sort != null) {
        sortOrder = sort;
      } else {
        sortOrder = RoutesTrackPointsColumns.DEFAULT_SORT_ORDER;
      }
    } else if (match == ROUTESTRACKPOINTS_ID) {
      qb.setTables(ROUTESTRACKPOINTS_TABLE);
      qb.appendWhere("_id=" + url.getPathSegments().get(1));
    } else {
      throw new IllegalArgumentException("Unknown URL " + url);
    }

    
    Log.i(Constants.TAG,
          "Build query: " + qb.buildQuery(projection, selection, selectionArgs,
          null, null, sortOrder, null));
    Cursor c = qb.query(db, projection, selection, selectionArgs, null, null,
        sortOrder);
    c.setNotificationUri(getContext().getContentResolver(), url);
    return c;
  }

  @Override
  public int update(Uri url, ContentValues values, String where,
      String[] selectionArgs) {
    if (!canAccess()) {
      return 0;
    }
    int count;
    int match = urlMatcher.match(url);
    if (match == ROUTEPOINTS) {
      count = db.update(ROUTEPOINTS_TABLE, values, where, selectionArgs);
    } else if (match == ROUTEPOINTS_ID) {
      String segment = url.getPathSegments().get(1);
      count = db.update(ROUTEPOINTS_TABLE, values, "_id=" + segment
          + (!TextUtils.isEmpty(where)
              ? " AND (" + where + ')'
              : ""),
          selectionArgs);
    } else if (match == ROUTES) {
      count = db.update(ROUTES_TABLE, values, where, selectionArgs);
    } else if (match == ROUTES_ID) {
      String segment = url.getPathSegments().get(1);
      count = db.update(ROUTES_TABLE, values, "_id=" + segment
          + (!TextUtils.isEmpty(where)
              ? " AND (" + where + ')'
              : ""),
          selectionArgs);
    } else if (match == ROUTESTRACKPOINTS) {
      count = db.update(ROUTESTRACKPOINTS_TABLE, values, where, selectionArgs);
    } else if (match == ROUTESTRACKPOINTS_ID) {
      String segment = url.getPathSegments().get(1);
      count = db.update(ROUTESTRACKPOINTS_TABLE, values, "_id=" + segment
          + (!TextUtils.isEmpty(where)
              ? " AND (" + where + ')'
              : ""),
          selectionArgs);
    } else {
      throw new IllegalArgumentException("Unknown URL " + url);
    }
    getContext().getContentResolver().notifyChange(url, null, true);
    return count;
  }

}

package com.google.android.lib.content.data;

import java.io.ObjectOutputStream.PutField;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.android.lib.logs.MyLogClass.TAG;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.android.lib.content.Track;
import com.google.android.lib.content.TrackColumns;
import com.google.android.lib.content.Route;
import com.google.android.lib.content.RouteLocation;
import com.google.android.lib.content.RouteCoordinates;
import com.google.android.lib.content.RoutesColumns;

import com.google.android.lib.statistics.RouteStatistics;


public class MyRouteProviderImpl implements MyRouteProvider {
	private final ContentResolver mContentResolver;
	private int defaultCursorBatchSize = 2000;

	public MyRouteProviderImpl(ContentResolver m_hContentResolver) {
		this.mContentResolver = m_hContentResolver;
	}

	private static ContentValues createContentValues(Location location,
			long routeId) {
		ContentValues myValues = new ContentValues();
		myValues.put(RouteCoordinates.ROUTE_ID, routeId);
		myValues.put(RouteCoordinates.LATITUDE,
				(int) location.getLatitude() * 1E6);
		myValues.put(RouteCoordinates.LONGITUDE,
				(int) location.getLongitude() * 1E6);
		myValues.put(
				RouteCoordinates.TIME,
				location.getTime() == 0 ? System.currentTimeMillis() : location
						.getTime());
		if (location.hasAltitude()) {
			myValues.put(RouteCoordinates.ALTITUDE, location.getAltitude());
		}
		if (location.hasAccuracy()) {
			myValues.put(RouteCoordinates.ACCURACY, location.getAccuracy());
		}
		if (location.hasBearing()) {
			myValues.put(RouteCoordinates.BEARING, location.getBearing());
		}
		if (location.hasSpeed()) {
			myValues.put(RouteCoordinates.SPEED, location.getSpeed());
		}
		
		return myValues;
	}

	public ContentValues createContentValues(Route route) {
		ContentValues content = new ContentValues();
		RouteStatistics routeStats = new RouteStatistics();
		if (route.getId() > 0) {
			content.put(RoutesColumns._ID, route.getId());
		}
		content.put(RoutesColumns.NAME, route.getName());
		content.put(RoutesColumns.DESCRIPTION, route.getDescription());
		content.put(RoutesColumns.MAP_ID, route.getMap_id());
		content.put(RoutesColumns.TABLE_ID, route.getTable_id());
		content.put(RoutesColumns.CATEGORY, route.getCategory());
		content.put(RoutesColumns.NUMBER_POINTS, route.getNumberRoutePoints());
		content.put(RoutesColumns.START_ID, route.getStart_id());
		content.put(RoutesColumns.STOP_ID, route.getStop_id());
		content.put(RoutesColumns.START_TIME, routeStats.getStart_time());
		content.put(RoutesColumns.STOP_TIME, routeStats.getStop_time());
		content.put(RoutesColumns.MIN_LAT, routeStats.getLowestAltitude());
		content.put(RoutesColumns.MAX_LAT, routeStats.getHighestAltitude());
		content.put(RoutesColumns.MIN_LONG, routeStats.getLowestLongitude());
		content.put(RoutesColumns.MAX_LONG, routeStats.getHighestAltitude());
		content.put(RoutesColumns.MIN_ELEVATION, routeStats.getMinElevation());
		content.put(RoutesColumns.MAX_ELEVATION, routeStats.getMaxElevation());
		content.put(RoutesColumns.AVG_MOVING_SPEED,
				routeStats.getAverageMovingSpeed());
		content.put(RoutesColumns.AVG_SPEED, routeStats.getAverageSpeed());
		content.put(RoutesColumns.TOTAL_TIME, routeStats.getTotal_time());
		content.put(RoutesColumns.TOTAL_DISTANCE,
				routeStats.getTotal_distance());
		content.put(RoutesColumns.MIN_GRADE, routeStats.getMinGrade());
		content.put(RoutesColumns.MAX_GRADE, routeStats.getMaxGrade());
		return content;
	}

	public ContentValues createContentValues(Track point) {
		ContentValues content = new ContentValues();
		RouteStatistics routeStats = point.getRouteStatistics();
		if (point.getId() > 0) {
			content.put(TrackColumns._ID, point.getId());
		}
		content.put(TrackColumns.NAME, point.getName());
		content.put(TrackColumns.DESCRIPTION, point.getDescription());
		content.put(TrackColumns.CATEGORY, point.getCategory());
		content.put(TrackColumns.START_ID, point.getStartPointid());
		content.put(TrackColumns.STOP_ID, point.getStopPointId());
		content.put(TrackColumns.ICON_TRACK, point.getIcon());
		content.put(TrackColumns.TRACK_DURATION, point.getDuration());
		content.put(TrackColumns.LEGNTH_OF_TRACK, point.getLastTrackDuration());
		if (routeStats != null) {
			content.put(TrackColumns.START_TIME, routeStats.getStart_time());
			content.put(TrackColumns.AVG_MOVING_SPEED,
					routeStats.getAverageMovingSpeed());
			content.put(TrackColumns.AVG_SPEED, routeStats.getAverageSpeed());
			content.put(TrackColumns.TOTAL_DISTANCE,
					routeStats.getTotal_distance());
			content.put(TrackColumns.TOTAL_TIME, routeStats.getTotal_time());
			content.put(TrackColumns.MIN_ELEVATION,
					routeStats.getMinElevation());
			content.put(TrackColumns.MAX_ELEVATION,
					routeStats.getMaxElevation());
			content.put(TrackColumns.MIN_GRADE, routeStats.getMinGrade());
			content.put(TrackColumns.MAX_GRADE, routeStats.getMaxGrade());
			content.put(TrackColumns.MAX_SPEED, routeStats.getMax_speed());
		}
		Location location = point.getLocation();
		if (location != null) {

			content.put(TrackColumns.LATITUDE,
					(int) location.getLatitude() * 1E6);
			content.put(TrackColumns.LONGITUDE,
					(int) location.getLongitude() * 1E6);
			content.put(TrackColumns.TIME, location.getTime());
			if (location.hasAccuracy()) {
				content.put(TrackColumns.ACCURACY, location.getAccuracy());
			}
			if (location.hasBearing()) {
				content.put(TrackColumns.BEARING, location.getBearing());
			}
			if (location.hasAltitude()) {
				content.put(TrackColumns.ALTITUDE, location.getAltitude());
			}
			if (location.hasSpeed()) {
				content.put(TrackColumns.SPEED, location.getSpeed());
			}
		}

		return content;
	}

	public Location createLocation(Cursor cursor) {
		Location location = new RouteLocation("");
		updateLocation(cursor, location);
		return location;
	}

	public void updateLocation(Cursor cursor, Location location) {
		CachedRouteCoordinatesColumnIndex mColmunIndex = new CachedRouteCoordinatesColumnIndex(
				cursor);
		updateLocation(cursor, mColmunIndex, location);
	}

	public void updateLocation(Cursor cursor,
			CachedRouteCoordinatesColumnIndex columnIndex, Location location) {
		if (!cursor.isNull(columnIndex.idxLatitude)) {
			location.setLatitude(1. * cursor.getInt(columnIndex.idxLatitude) / 1E6);
		}
		if (!cursor.isNull(columnIndex.idxLongitude)) {
			location.setLongitude(1. * cursor.getInt(columnIndex.idxLongitude) / 1E6);
		}
		if (!cursor.isNull(columnIndex.idxAltitude)) {
			location.setAltitude(cursor.getFloat(columnIndex.idxAltitude));
		}
		if (!cursor.isNull(columnIndex.idxTime)) {
			location.setTime(cursor.getLong(columnIndex.idxTime));
		}
		if (!cursor.isNull(columnIndex.idxBearing)) {
			location.setBearing(cursor.getFloat(columnIndex.idxBearing));
		}
		if (!cursor.isNull(columnIndex.idxSpeed)) {
			location.setSpeed(cursor.getFloat(columnIndex.idxSpeed));
		}
		if (!cursor.isNull(columnIndex.idxAccuracy)) {
			location.setAccuracy(cursor.getFloat(columnIndex.idxAccuracy));
		}
		
	}

	private static class CachedRouteCoordinatesColumnIndex {
		public final int idxId;
		public final int idxLatitude;
		public final int idxLongitude;
		public final int idxAltitude;
		public final int idxTime;
		public final int idxBearing;
		public final int idxAccuracy;
		public final int idxSpeed;
		

		public CachedRouteCoordinatesColumnIndex(Cursor cursor) {
			idxId = cursor.getColumnIndex(RouteCoordinates._ID);
			idxLatitude = cursor
					.getColumnIndexOrThrow(RouteCoordinates.LATITUDE);
			idxLongitude = cursor
					.getColumnIndexOrThrow(RouteCoordinates.LONGITUDE);
			idxAltitude = cursor
					.getColumnIndexOrThrow(RouteCoordinates.ALTITUDE);
			idxTime = cursor.getColumnIndexOrThrow(RouteCoordinates.TIME);
			idxBearing = cursor.getColumnIndexOrThrow(RouteCoordinates.BEARING);
			idxAccuracy = cursor
					.getColumnIndexOrThrow(RouteCoordinates.ACCURACY);
			idxSpeed = cursor.getColumnIndexOrThrow(RouteCoordinates.SPEED);
			
		}
	}

	public Route createRoute(Cursor cursor) {
		int idxId = cursor.getColumnIndexOrThrow(RoutesColumns._ID);
		int idxName = cursor.getColumnIndexOrThrow(RoutesColumns.NAME);
		int idxDescription = cursor
				.getColumnIndexOrThrow(RoutesColumns.DESCRIPTION);
		int idxMapId = cursor.getColumnIndexOrThrow(RoutesColumns.MAP_ID);
		int idxTableId = cursor.getColumnIndexOrThrow(RoutesColumns.TABLE_ID);
		int idxCategory = cursor.getColumnIndexOrThrow(RoutesColumns.CATEGORY);
		int idxStartId = cursor.getColumnIndexOrThrow(RoutesColumns.START_ID);
		int idxStartTime = cursor
				.getColumnIndexOrThrow(RoutesColumns.START_TIME);
		int idxStopTime = cursor.getColumnIndexOrThrow(RoutesColumns.STOP_TIME);
		int idxStopId = cursor.getColumnIndexOrThrow(RoutesColumns.STOP_ID);
		int idxNumPoints = cursor
				.getColumnIndexOrThrow(RoutesColumns.NUMBER_POINTS);
		int idxMaxlat = cursor.getColumnIndexOrThrow(RoutesColumns.MAX_LAT);
		int idxMinlat = cursor.getColumnIndexOrThrow(RoutesColumns.MIN_LAT);
		int idxMaxlon = cursor.getColumnIndexOrThrow(RoutesColumns.MAX_LONG);
		int idxMinlon = cursor.getColumnIndexOrThrow(RoutesColumns.MIN_LONG);

		int idxTotalDistance = cursor
				.getColumnIndexOrThrow(RoutesColumns.TOTAL_DISTANCE);
		int idxTotalTime = cursor
				.getColumnIndexOrThrow(RoutesColumns.TOTAL_TIME);
		int idxMovingTime = cursor
				.getColumnIndexOrThrow(RoutesColumns.MOVING_TIME);
		int idxMaxSpeed = cursor.getColumnIndexOrThrow(RoutesColumns.MAX_SPEED);
		int idxMinElevation = cursor
				.getColumnIndexOrThrow(RoutesColumns.MIN_ELEVATION);
		int idxMaxElevation = cursor
				.getColumnIndexOrThrow(RoutesColumns.MAX_ELEVATION);
		int idxElevationGain = cursor
				.getColumnIndexOrThrow(RoutesColumns.ELEVATION_GAIN_CURRENT);
		int idxMinGrade = cursor.getColumnIndexOrThrow(RoutesColumns.MIN_GRADE);
		int idxMaxGrade = cursor.getColumnIndexOrThrow(RoutesColumns.MAX_GRADE);

		Route route = new Route();
		RouteStatistics stats = route.getRouteStatistics();
		if (!cursor.isNull(idxId)) {
			route.setId(cursor.getLong(idxId));
		}
		if (!cursor.isNull(idxName)) {
			route.setName(cursor.getString(idxName));
		}
		if (!cursor.isNull(idxDescription)) {
			route.setDescription(cursor.getString(idxDescription));
		}
		if (!cursor.isNull(idxMapId)) {
			route.setMap_id(cursor.getString(idxMapId));
		}
		if (!cursor.isNull(idxTableId)) {
			route.setTable_id(cursor.getString(idxTableId));
		}
		if (!cursor.isNull(idxCategory)) {
			route.setCategory(cursor.getString(idxCategory));
		}
		if (!cursor.isNull(idxStartId)) {
			route.setStart_id(cursor.getInt(idxStartId));
		}
		if (!cursor.isNull(idxStartTime)) {
			stats.setStart_time(cursor.getLong(idxStartTime));
		}
		if (!cursor.isNull(idxStopTime)) {
			stats.setStop_time(cursor.getLong(idxStopTime));
		}
		if (!cursor.isNull(idxStopId)) {
			route.setStop_id(cursor.getInt(idxStopId));
		}
		if (!cursor.isNull(idxNumPoints)) {
			route.setNumberRoutePoints(cursor.getInt(idxNumPoints));
		}
		if (!cursor.isNull(idxTotalDistance)) {
			stats.setTotal_distance(cursor.getFloat(idxTotalDistance));
		}
		if (!cursor.isNull(idxTotalTime)) {
			stats.setTotal_time(cursor.getLong(idxTotalTime));
		}
		if (!cursor.isNull(idxMovingTime)) {
			stats.setMoving_time(cursor.getLong(idxMovingTime));
		}
		if (!cursor.isNull(idxMaxlat) && !cursor.isNull(idxMinlat)
				&& !cursor.isNull(idxMaxlon) && !cursor.isNull(idxMinlon)) {
			int top = cursor.getInt(idxMaxlat);
			int bottom = cursor.getInt(idxMinlat);
			int right = cursor.getInt(idxMaxlon);
			int left = cursor.getInt(idxMinlon);
			stats.setLatitudeLongitudeBounds(left, top, right, bottom);
		}
		if (!cursor.isNull(idxMaxSpeed)) {
			stats.setMax_speed(cursor.getFloat(idxMaxSpeed));
		}
		if (!cursor.isNull(idxMinElevation)) {
			stats.setMinElevation(cursor.getFloat(idxMinElevation));
		}
		if (!cursor.isNull(idxMaxElevation)) {
			stats.setMaxElevation(cursor.getFloat(idxMaxElevation));
		}
		if (!cursor.isNull(idxElevationGain)) {
			stats.setTotal_elevation_gain(cursor.getFloat(idxElevationGain));
		}
		if (!cursor.isNull(idxMinGrade)) {
			stats.setMinGrade(cursor.getFloat(idxMinGrade));
		}
		if (!cursor.isNull(idxMaxGrade)) {
			stats.setMaxGrade(cursor.getFloat(idxMaxGrade));
		}
		return route;
	}

	public Track createTrack(Cursor cursor) {
		int idxId = cursor.getColumnIndexOrThrow(TrackColumns._ID);
		int idxName = cursor.getColumnIndexOrThrow(TrackColumns.NAME);
		int idxDescription = cursor
				.getColumnIndexOrThrow(TrackColumns.DESCRIPTION);
		int idxCategory = cursor.getColumnIndexOrThrow(TrackColumns.CATEGORY);
		int idxIcon = cursor.getColumnIndexOrThrow(TrackColumns.ICON_TRACK);
		int idxRouteId = cursor.getColumnIndexOrThrow(TrackColumns.ROUTE_ID);
		int idxType = cursor.getColumnIndexOrThrow(TrackColumns.TYPE);
		int idxLength = cursor
				.getColumnIndexOrThrow(TrackColumns.LEGNTH_OF_TRACK);
		int idxDuration = cursor
				.getColumnIndexOrThrow(TrackColumns.TRACK_DURATION);
		int idxStartTime = cursor
				.getColumnIndexOrThrow(TrackColumns.START_TIME);
		int idxStartId = cursor.getColumnIndexOrThrow(TrackColumns.START_ID);
		int idxStopId = cursor.getColumnIndexOrThrow(TrackColumns.STOP_ID);

		int idxNrOfTimes = cursor
				.getColumnIndexOrThrow(TrackColumns.NUMBER_TIMES);
		int idxTotalDistance = cursor
				.getColumnIndexOrThrow(TrackColumns.TOTAL_DISTANCE);
		int idxTotalTime = cursor
				.getColumnIndexOrThrow(TrackColumns.TOTAL_TIME);
		int idxMovingTime = cursor
				.getColumnIndexOrThrow(TrackColumns.MOVING_TIME);
		int idxMaxSpeed = cursor.getColumnIndexOrThrow(TrackColumns.MAX_SPEED);
		int idxMinElevation = cursor
				.getColumnIndexOrThrow(TrackColumns.MIN_ELEVATION);
		int idxMaxElevation = cursor
				.getColumnIndexOrThrow(TrackColumns.MAX_ELEVATION);
		int idxElevationGain = cursor
				.getColumnIndexOrThrow(TrackColumns.ELEVATION_GAIN_CURRENT);
		int idxMinGrade = cursor.getColumnIndexOrThrow(TrackColumns.MIN_GRADE);
		int idxMaxGrade = cursor.getColumnIndexOrThrow(TrackColumns.MAX_GRADE);

		int idxLatitude = cursor.getColumnIndexOrThrow(TrackColumns.LATITUDE);
		int idxLongitude = cursor.getColumnIndexOrThrow(TrackColumns.LONGITUDE);
		int idxAltitude = cursor.getColumnIndexOrThrow(TrackColumns.ALTITUDE);
		int idxTime = cursor.getColumnIndexOrThrow(TrackColumns.TIME);
		int idxBearing = cursor.getColumnIndexOrThrow(TrackColumns.BEARING);
		int idxAccuracy = cursor.getColumnIndexOrThrow(TrackColumns.ACCURACY);
		int idxSpeed = cursor.getColumnIndexOrThrow(TrackColumns.SPEED);

		Track track = new Track();

		if (!cursor.isNull(idxId)) {
			track.setId(cursor.getLong(idxId));
		}
		if (!cursor.isNull(idxName)) {
			track.setName(cursor.getString(idxName));
		}
		if (!cursor.isNull(idxDescription)) {
			track.setDescription(cursor.getString(idxDescription));
		}
		if (!cursor.isNull(idxNrOfTimes)) {
			track.setNrOfTimes(cursor.getInt(idxNrOfTimes));
		}
		if (!cursor.isNull(idxCategory)) {
			track.setCategory(cursor.getString(idxCategory));
		}
		if (!cursor.isNull(idxIcon)) {
			track.setIcon(cursor.getString(idxIcon));
		}
		if (!cursor.isNull(idxRouteId)) {
			track.setRouteId(cursor.getLong(idxRouteId));
		}
		if (!cursor.isNull(idxType)) {
			track.setType(cursor.getInt(idxType));
		}
		if (!cursor.isNull(idxLength)) {
			track.setTrackLength(cursor.getDouble(idxLength));
		}
		if (!cursor.isNull(idxDuration)) {
			track.setDuration(cursor.getLong(idxDuration));
		}
		if (!cursor.isNull(idxStartId)) {
			track.setStartPointid(cursor.getLong(idxStartId));
		}
		if (!cursor.isNull(idxStopId)) {
			track.setStopPointId(cursor.getLong(idxStopId));
		}

		RouteStatistics stats = new RouteStatistics();
		boolean hasStats = false;
		if (!cursor.isNull(idxStartTime)) {
			stats.setStart_time(cursor.getLong(idxStartTime));
			hasStats = true;
		}
		if (!cursor.isNull(idxTotalDistance)) {
			stats.setTotal_distance(cursor.getFloat(idxTotalDistance));
			hasStats = true;
		}
		if (!cursor.isNull(idxTotalTime)) {
			stats.setTotal_time(cursor.getLong(idxTotalTime));
			hasStats = true;
		}
		if (!cursor.isNull(idxMovingTime)) {
			stats.setMoving_time(cursor.getLong(idxMovingTime));
			hasStats = true;
		}
		if (!cursor.isNull(idxMaxSpeed)) {
			stats.setMax_speed(cursor.getFloat(idxMaxSpeed));
			hasStats = true;
		}
		if (!cursor.isNull(idxMinElevation)) {
			stats.setMinElevation(cursor.getFloat(idxMinElevation));
			hasStats = true;
		}
		if (!cursor.isNull(idxMaxElevation)) {
			stats.setMaxElevation(cursor.getFloat(idxMaxElevation));
			hasStats = true;
		}
		if (!cursor.isNull(idxElevationGain)) {
			stats.setTotal_elevation_gain(cursor.getFloat(idxElevationGain));
			hasStats = true;
		}
		if (!cursor.isNull(idxMinGrade)) {
			stats.setMinGrade(cursor.getFloat(idxMinGrade));
			hasStats = true;
		}
		if (!cursor.isNull(idxMaxGrade)) {
			stats.setMaxGrade(cursor.getFloat(idxMaxGrade));
			hasStats = true;
		}

		if (hasStats) {
			track.setRouteStatisticss(stats);
		}

		Location location = new Location("");
		if (!cursor.isNull(idxLatitude) && !cursor.isNull(idxLongitude)) {
			location.setLatitude(1. * cursor.getInt(idxLatitude) / 1E6);
			location.setLongitude(1. * cursor.getInt(idxLongitude) / 1E6);
		}
		if (!cursor.isNull(idxAltitude)) {
			location.setAltitude(cursor.getFloat(idxAltitude));
		}
		if (!cursor.isNull(idxTime)) {
			location.setTime(cursor.getLong(idxTime));
		}
		if (!cursor.isNull(idxBearing)) {
			location.setBearing(cursor.getFloat(idxBearing));
		}
		if (!cursor.isNull(idxSpeed)) {
			location.setSpeed(cursor.getFloat(idxSpeed));
		}
		if (!cursor.isNull(idxAccuracy)) {
			location.setAccuracy(cursor.getFloat(idxAccuracy));
		}
		track.setLocation(location);
		return track;
	}

	public void deleteAllRoutes() {
		mContentResolver.delete(RoutesColumns.CONTENT_URI, null, null);
		mContentResolver.delete(RouteCoordinates.CONTENT_URI, null, null);
		mContentResolver.delete(TrackColumns.CONTENT_URI, null, null);
	}

	public void deleteRouteById(long routeId) {
		String where = "";
		String[] selectionArgs = null;
		Route route = getRouteById(routeId);
		if (route != null) {
			where += "_id>=" + route.getStart_id() + " AND _id<="
					+ route.getStop_id();
			mContentResolver.delete(RouteCoordinates.CONTENT_URI, where,
					selectionArgs);
		}
		mContentResolver.delete(TrackColumns.CONTENT_URI, "_ID = " + routeId,
				null);
		mContentResolver.delete(RoutesColumns.CONTENT_URI, "_ID = " + routeId,
				null);
	}

	public Route getRouteById(long routeId) {
		if (routeId < 0) {
			Log.i(TAG, "Should not be null");
			return null;
		}
		String selectQuery = RoutesColumns._ID + " = " + routeId;
		return findRouteByQuery(selectQuery);
	}

	private Route findRouteByQuery(String selectQuery) {
		Cursor cursor = null;
		try {
			cursor = mContentResolver.query(RoutesColumns.CONTENT_URI, null,
					selectQuery, null, null);
			if (cursor != null && cursor.moveToNext()) {
				return createRoute(cursor);
			}
		} catch (RuntimeException e) {
			Log.e(TAG, "Exception in findRouteByQuery");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;

	}

	public void deleteTrack(long trackId,
			DescriptionGenerator descriptionGenerator) {
		String where = "";
		final Track deleteTrack = getTrackById(trackId);
		if (deleteTrack != null) {
			final Track nextTrack = getNextStatisticTrackAfter(deleteTrack);
			if (nextTrack != null
					&& deleteTrack.getType() == Track.TRACK_STATISTICS) {
				Log.d(TAG, "new Marker..." + nextTrack.getId() + " old marker "
						+ deleteTrack.getId());
				nextTrack.getRouteStatistics().merge(
						deleteTrack.getRouteStatistics());
				nextTrack.setDescription(descriptionGenerator
						.generateWaypointDescription(nextTrack));
				if (!updateTrack(nextTrack)) {
					Log.w(TAG, "Update impossible");
				} else {
					Log.d(TAG, "No statistics");
				}
			}
		}
		where += " _id = " + trackId;
		mContentResolver.delete(TrackColumns.CONTENT_URI, where, null);
	}

	public Track getTrackById(long trackId) {
		Cursor cursor = null;
		String selection = "";
		if (trackId < 0) {
			return null;
		}
		selection += "_ID" + " = " + trackId;
		cursor = mContentResolver.query(TrackColumns.CONTENT_URI, null,
				selection, null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					return createTrack(cursor);
				}
			} catch (RuntimeException e) {
				Log.w(TAG, "Exception in getTrackById");
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	public Track getNextStatisticTrackAfter(Track track) {
		String selection = TrackColumns._ID + " > " + track.getId()
				+ TrackColumns.ROUTE_ID + " = " + track.getRouteId() + " AND "
				+ TrackColumns.TYPE + " = " + Track.TRACK_STATISTICS;
		final String sortOrder = TrackColumns._ID + " LIMIR 1";
		Cursor cursor = null;
		try {
			cursor = mContentResolver.query(TrackColumns.CONTENT_URI, null,
					selection, null, sortOrder);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					return createTrack(cursor);
				}
			}
		} catch (RuntimeException exception) {
			Log.w(TAG, "Exception in getNextStatisticTrackAfter");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	public boolean updateTrack(Track track) {
		String where = "";
		try {
			where += "_ID" + " = " + track.getId();
			final int rows = mContentResolver.update(TrackColumns.CONTENT_URI,
					createContentValues(track), where, null);
			if (rows > 0) {
				return (rows == 1);
			}
		} catch (Exception e) {
			Log.w(TAG, "Exception in updateTrack");
		}
		return false;
	}

	public Location getLastRecordedLocation() {
		String selection = "_id = (select max(_id) from routepoints " + " )";
		return findLocationById(selection);
	}

	public Track getFirstTrackFromRoute(long routeId) {
		if (routeId < 0) {
			return null;
		}
		return null;
	}

	public long getLastLocationId(long routeId) {
		if (routeId < 0) {
			return -1;
		}
		Cursor cursor = null;
		String selection = "";
		try {
			final String[] projection = { "_id" };
			selection += "_id = (select max(_id) from routepoints where route_id "
					+ " = " + routeId + " )";
			cursor = mContentResolver.query(RouteCoordinates.CONTENT_URI,
					projection, selection, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					return cursor.getLong(cursor
							.getColumnIndexOrThrow(RouteCoordinates._ID));
				}
			}
		} catch (Exception e) {
			Log.w(TAG, "Exception in getLastLoctionId");
		} finally {
			cursor.close();
		}

		return -1;
	}

	public long getFirstTrackId(long routeId) {
		if (routeId < 0) {
			return -1;
		}
		Cursor cursor = null;
		String selection = "";
		String sortOrder = "_id ASC LIMIT 1";
		String[] projection = { "_id" };
		try {
			selection += TrackColumns.ROUTE_ID + " = " + routeId;
			cursor = mContentResolver.query(TrackColumns.CONTENT_URI,
					projection, selection, null, sortOrder);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					return cursor.getLong(cursor
							.getColumnIndexOrThrow(TrackColumns._ID));
				}

			}
		} catch (RuntimeException e) {
			Log.w(TAG, "Exception in getFirstTrackId");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return -1;
	}

	public long getLastTrackId(long routeId) {
		if (routeId < 0) {
			return -1;
		}
		String selection = "";
		String[] projection = { "_id" };
		Cursor cursor = null;
		String sortOrder = "_id DESC LIMIT 1";
		try {
			selection += TrackColumns.ROUTE_ID + " = " + routeId;
			cursor = mContentResolver.query(TrackColumns.CONTENT_URI,
					projection, selection, null, sortOrder);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					return cursor.getLong(cursor
							.getColumnIndexOrThrow(TrackColumns._ID));
				}
			}
		} catch (Exception e) {
			Log.w(TAG, "Exception in getLasttrackid");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return 0;
	}

	public long getLastRouteId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Location getLocationById(long id) {
		if (id < 0) {
			return null;
		}
		String selection = RouteCoordinates._ID + " = " + id;
		return findLocationById(selection);
	}

	private Location findLocationById(String selection) {
		Cursor cursor = null;
		try {
			cursor = mContentResolver.query(RouteCoordinates.CONTENT_URI, null,
					selection, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					return createLocation(cursor);
				}
			}
		} catch (Exception e) {
			Log.w(TAG, "Exception");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	public Route getLastRoute() {
		Cursor cursor = null;
		String selection = "";
		try {
			selection += "+id = (select max _id) from routes";
			cursor = mContentResolver.query(RoutesColumns.CONTENT_URI, null,
					selection, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				return createRoute(cursor);
			}
		} catch (Exception e) {
			Log.w(TAG, "Exception in GetLastRoute");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	public List<Route> getAllRoutes() {
		Cursor cursor = null;
		String selection = "";
		ArrayList<Route> mRoutes = new ArrayList<Route>();
		try {
			cursor = mContentResolver.query(RoutesColumns.CONTENT_URI, null,
					null, null, null);
			if (cursor != null) {
				mRoutes.ensureCapacity(cursor.getCount());
				if (cursor.moveToFirst()) {
					do {
						mRoutes.add(createRoute(cursor));
					} while (cursor.moveToNext());
				}
			}
		} catch (RuntimeException e) {
			Log.w(TAG, "Exeption in getAllRoutes");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}

	public Cursor getLocationsCursor(long routeId, long minRoutePointId,
			int locationsNumber, boolean sort_type) {
		if (routeId < 0) {
			return null;
		}

		String selection;
		if (minRoutePointId >= 0) {
			selection = String.format("%s=%d AND %s%s%d",
					RouteCoordinates.ROUTE_ID, routeId, RouteCoordinates._ID,
					sort_type ? "<=" : ">=", minRoutePointId);
		} else {
			selection = String.format("%s=%d", RouteCoordinates.ROUTE_ID,
					routeId);
		}

		String sortOrder = "_id " + (sort_type ? "DESC" : "ASC");
		if (locationsNumber > 0) {
			sortOrder += " LIMIT " + locationsNumber;
		}

		return mContentResolver.query(RouteCoordinates.CONTENT_URI, null,
				selection, null, sortOrder);
	}

	public Cursor getTracksCursor(long routeId, long minTrackId, int maxTraks) {
		if (routeId < 0) {
			return null;
		}
		String selection = "";
		String sortOrder = " _id ASC";
		Cursor cursor = null;
		try {
			if (minTrackId > 0) {
				selection += TrackColumns.ROUTE_ID + " = " + routeId + " AND "
						+ TrackColumns._ID + " > " + minTrackId;
			} else {
				selection += TrackColumns.ROUTE_ID + " = " + routeId;
			}
			if (maxTraks > 0) {
				sortOrder += " LIMIT" + maxTraks;
			}

			cursor = mContentResolver.query(TrackColumns.CONTENT_URI, null,
					selection, null, sortOrder);
			return cursor;
		} catch (RuntimeException e) {
			Log.w(TAG, "Exception in GetTrackCursor");
		}
		return null;
	}

	public Cursor getSelectedRoutesCursor(String selection) {
		// TODO Auto-generated method stub
		return null;
	}

	public Uri insertRoute(Route newRoute) {
		Log.d(TAG, "MyRouteProviderImpl.insertTrack");
		return mContentResolver.insert(RoutesColumns.CONTENT_URI,
				createContentValues(newRoute));
	}

	public Uri insertRouteTrack(Location location, long routeId) {
		Log.d(TAG, "MyRouteProviderImpl.insertTrackPoint");
		return mContentResolver.insert(RoutesColumns.CONTENT_URI,
				createContentValues(location, routeId));
	}

	public int insertManyRouteTracks(Location[] locations, int length,
			long routeId) {
		if (length == -1) {
			length = locations.length;
		}
		ContentValues[] values = new ContentValues[length];
		for (int i = 0; i < length; i++) {
			values[i] = createContentValues(locations[i], routeId);
		}
		return mContentResolver
				.bulkInsert(RouteCoordinates.CONTENT_URI, values);
	}

	public Uri insertTrack(Track track) {
		Log.d(TAG, "MyRouteProviderImpl.insertWaypoint");
		track.setId(-1);
		return mContentResolver.insert(TrackColumns.CONTENT_URI,
				createContentValues(track));
	}

	public boolean checkRoute(long routeId) {
		if (routeId < 0) {
			return false;
		}
		String selection;
		String[] projection = { "_id" };
		Cursor cursor = null;
		try {
			selection = RoutesColumns._ID + " = " + routeId;
			cursor = mContentResolver.query(RoutesColumns.CONTENT_URI,
					projection, selection, null, null);
			if (cursor != null) {
				if (cursor.moveToNext()) {
					return true;
				}
			}
		} catch (Exception e) {
			Log.w(TAG, "Exception in checkRoute");
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	public void updateRoute(Route route) {
		Log.d(TAG, "MyRouteProviderImpl.updateTrack");
		mContentResolver.update(RoutesColumns.CONTENT_URI,
				createContentValues(route), "_id=" + route.getId(), null);

	}

	public void updateRoute(Track endpoint) {
		// TODO Auto-generated method stub

	}

	public LocationIterator getLocationIterator(final long routeId, final long startRoutePointId, final boolean sort_type,final CreateLocationFactory locationFactory) 
	{
		if (locationFactory == null) 
		{
			throw new IllegalArgumentException("Expected to be non-empty");
		}
		return new LocationIterator() 
		{
			private long lastPointId = startRoutePointId;
			private Cursor cursor = geCurrentCursor(startRoutePointId);

			private final CachedRouteCoordinatesColumnIndex columnIndices = cursor != null ? new CachedRouteCoordinatesColumnIndex(cursor) : null;

			private Cursor geCurrentCursor(long startRoutePointId) {
				return getLocationsCursor(routeId, startRoutePointId,
						defaultCursorBatchSize, sort_type);
			}

			private boolean advanceCursorToNextBatch() {
				long pointId = lastPointId + (sort_type ? -1 : 1);
				Log.d(TAG, "Advancing cursor point ID: " + pointId);
				cursor.close();
				cursor = getCursor(pointId);
				return cursor != null;
			}

			private Cursor getCursor(long trackPointId) {
				return getLocationsCursor(routeId, trackPointId, defaultCursorBatchSize, sort_type);
			}

			public long getLocationid() {
				return lastPointId;
			}

			public boolean hasNext() {
				if (cursor == null) {
					return false;
				}
				if (cursor.isAfterLast()) {
					return false;
				}
				if (cursor.isLast()) {
					// If the current batch size was less that max, we can
					// safely return, otherwise
					// we need to advance to the next batch.
					return cursor.getCount() == defaultCursorBatchSize
							&& advanceCursorToNextBatch()
							&& !cursor.isAfterLast();
				}

				return true;
			}

			public Location next() {
				if (cursor == null
						|| !(cursor.moveToNext() || advanceCursorToNextBatch() || cursor
								.moveToNext())) {
					throw new NoSuchElementException();
				}

				lastPointId = cursor.getLong(columnIndices.idxId);
				Location location = locationFactory.createGPSLocation();
				updateLocation(cursor, columnIndices, location);

				return location;
			}

			public void close() {
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
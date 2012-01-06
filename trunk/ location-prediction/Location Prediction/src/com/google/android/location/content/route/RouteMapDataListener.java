package com.google.android.location.route;

import com.google.android.lib.content.Route;
import com.google.android.lib.content.RouteTrackPoint;

import android.location.Location;



/**
 * Listener for route data, for both initial and incremental loading.
 *
 * @author Andrei
 */
public interface RouteMapDataListener {

  /** States for the GPS location provider. */
  public enum MyGpsProviderState
  {
    DISABLED,
    NO_FIX,
    BAD_FIX,
    GOOD_FIX;
  }
 public enum MyNetworkProviderState
 {
	 DISABLED,
	 NO_FIX,
	 BAD_FIX,
	 GOOD_FIX;
 }
  /**
   * Called when the location provider changes state.
   */
  void onGPSProviderStateChange(MyGpsProviderState state);
  /**
   * called when network location provider changes its state
   */
  void onNetworkProviderStateChange(MyNetworkProviderState state);
  /**
   * Called when the current gps location changes.
   * This is meant for immediate location display only - route point data is
   * delivered by other methods below, such as {@link #onNewRouteTrackPoint}.
   *
   * @param loc the last known location
   */
  void onGPSCurrentLocationChanged(Location loc);
  /**
   *called when current network location changes
   *this is meant for immediate location display only - route point data is
   *delivered by other methods below, such as {@link onNewRoutePoint}
   */
  
  /**
   * Called when the current heading changes.
   *
   * @param heading the current heading, already accounting magnetic declination
   */
  void onCurrentHeadingChanged(double heading);

  /**
   * Called when the currently-selected route changes.
   * This will be followed by calls to data methods such as
   * {@link #onRouteUpdated}, {@link #clearRoutePoints},
   * {@link #onNewRoutePoint(Location)}, etc., even if no route is currently
   * selected (in which case you'll only get calls to clear the current data).
   * 
   * @param route the selected route, or null if no route is selected
   * @param isRecording whether we're currently recording the selected route
   */
  void onSelectedRouteChanged(Route route, boolean isRecording);

  /**
   * Called when the route and/or its statistics have been updated.
   *
   * @param route the updated version of the route
   */
  void onRouteUpdated(Route route);

  /**
   * Called to clear any previously-sent route points.
   * This can be called at any time that we decide the data needs to be
   * reloaded, such as when it needs to be resampled.
   */
  void clearRoutePoints();

  /**
   * Called when a new interesting track point is read.
   * In this case, interesting means that the point has already undergone
   * sampling and invalid point filtering.
   *
   * @param loc the new track point
   */
  void onNewRoutePoint(Location loc);

  /**
   * Called when a uninteresting track point is read.
   * Uninteresting points are all points that get sampled out of the track.
   *
   * @param loc the new track point
   */
  void onSampledOutRoutePoint(Location loc);

  /**
   * Called when an invalid point (representing a segment split) is read.
   */
  void onSegmentSplit();

  /**
   * Called when we're done (for the time being) sending new points.
   * This gets called after every batch of calls to {@link #onNewRoutePoint},
   * {@link #onSampledOutTrackPoint} and {@link #onSegmentSplit}.
   */
  void onNewRoutePointsDone();

  /**
   * Called to clear any previously-sent route track points.
   * This can be called at any time that we decide the data needs to be
   * reloaded.
   */
  void clearRouteTrackPoints();

  /**
   * Called when a new RouteTrackPoint is read.
   *
   * @param wpt the new RouteTrackPoint
   */
  void onNewRouteTrackPoint(RouteTrackPoint route_track_point);

  /**
   * Called when we're done (for the time being) sending new route track points.
   * This gets called after every batch of calls to {@link #clearRouteTrackPoints} and
   * {@link #onNewRouteTrackPoint}.
   */
  void onNewRouteTrackPointsDone();

  /**
   * Called when the display units are changed by the user.
   *
   * @param metric true if the units are metric, false if imperial
   * @return true to reload all the data, false otherwise
   */
  boolean onUnitsChanged(boolean metric);

  /**
   * Called when the speed/pace display unit is changed by the user.
   *
   * @param reportSpeed true to report speed, false for pace
   * @return true to reload all the data, false otherwise
   */
  boolean onReportSpeedChanged(boolean reportSpeed);
}

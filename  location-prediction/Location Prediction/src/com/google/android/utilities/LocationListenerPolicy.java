package com.google.android.utilities;

import android.location.Location;

/**
 * This is an interface for classes that will manage the location listener policy.
 * Different policy options are:
 * MyLocationListenerPolicy
 *
 * @author Andrei
 */
public interface LocationListenerPolicy {

  /**
   * Returns the polling time this policy would like at this time.
   *
   * @return The polling that this policy dictates -  relative to the min and max values
   */
  public long getDesiredPollingInterval();
  /**
   * distance between updates;
   * @return the distance after a new request for location should be made
   */
  public int getMinDistance();
  /**
   * Returns the didstance between last location and the current one.
   * @return the distance between the current location and the last recorded.
   */
  public double getDistance();
  /**
   * sets the current distance from the last recorded locaiton
   * @param distance the distance
   */
  public void setDistance(double distance);
  /**
   * Notifies the amount of time the user has been idle at their current
   * location.
   *
   * @param idleTime The time that the user has been idle at this point
   */
  public void updateIdleTime(long idleTime);
  /**
   * returns the last location from stats
   * @return the last location from stats.
   */
  public Location getLastLocation();
  /**
   * sets the last location
   * @param location the location to be modified
   */
  public void setLastLocation(Location location);
  /**
   * gets the updated idle time 
   */
  public long getIdleTime();
  /**
   * resets the idle time
   */
  public void resetIdleTime();
}
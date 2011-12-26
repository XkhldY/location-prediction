package com.google.android.utilities;

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
   * @return The polling that this policy dictates
   */
  public long getDesiredPollingInterval();

  /**
   * Returns the minimum distance between updates.
   */
  public int getMinDistance();

  /**
   * Notifies the amount of time the user has been idle at their current
   * location.
   *
   * @param idleTime The time that the user has been idle at this spot
   */
  public void updateIdleTime(long idleTime);
}
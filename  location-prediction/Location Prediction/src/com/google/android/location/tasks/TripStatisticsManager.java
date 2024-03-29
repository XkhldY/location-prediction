package com.google.android.location.tasks;

import android.location.Location;
import android.util.Log;

import com.google.android.lib.statistics.RouteStatistics;
import com.google.android.location.cache.DoubleBuffer;
import com.google.android.location.content.Constants;
import static com.google.android.location.content.Constants.*;
/**
 * Statistics keeper for a trip.
 * 
 * @author Andrei
 */
public class TripStatisticsManager {
  /**
   * Statistical data about the trip, which can be displayed to the user.
   */
  private final RouteStatistics data;

  /**
   * The last location that the gps reported.
   */
  private Location lastLocation;

  /**
   * The last location that contributed to the stats. It is also the last
   * location the user was found to be moving.
   */
  private Location lastMovingLocation;

  /**
   * The current speed in meters/second as reported by the gps.
   */
  private double currentSpeed;

  /**
   * The current grade. This value is very noisy and not reported to the user.
   */
  private double currentGrade;

  /**
   * Is the trip currently paused?
   * All trips start paused.
   */
  private boolean paused = true;

  /**
   * A buffer of the last speed readings in meters/second.
   */
  private final DoubleBuffer speedBuffer =
      new DoubleBuffer(Constants.SPEED_SMOOTHING_FACTOR);

  /**
   * A buffer of the recent elevation readings in meters.
   */
  private final DoubleBuffer elevationBuffer =
      new DoubleBuffer(Constants.ELEVATION_SMOOTHING_FACTOR);

  /**
   * A buffer of the distance between recent gps readings in meters.
   */
  private final DoubleBuffer distanceBuffer =
      new DoubleBuffer(Constants.DISTANCE_SMOOTHING_FACTOR);

  /**
   * A buffer of the recent grade calculations.
   */
  private final DoubleBuffer gradeBuffer =
      new DoubleBuffer(Constants.GRADE_SMOOTHING_FACTOR);

  /**
   * The total number of locations in this trip.
   */
  private long totalLocations = 0;

  private int minRecordingDistance =
      Constants.DEFAULT_MIN_RECORDING_DISTANCE;

  /**
   * Creates a new trip starting at the given time.
   * 
   * @param startTime the start time.
   */
  public TripStatisticsManager(long startTime) {
    data = new RouteStatistics();
    resumeAt(startTime);
  }

  /**
   * Creates a new trip, starting with existing statistics data.
   *
   * @param statsData the statistics data to copy and start from
   */
  public TripStatisticsManager(RouteStatistics statsData) 
  {
    data = new RouteStatistics(statsData);
    if (data.getStart_time() > 0) 
    {
      resumeAt(data.getStart_time());
    }
  }

  /**
   * Adds a location to the current trip. This will update all of the internal
   * variables with this new location.
   * 
   * @param currentLocation the current gps location
   * @param systemTime the time used for calculation of totalTime. This should
   *        be the phone's time (not GPS time)
   * @return true if the person is moving
   */
  public boolean addLocation(Location currentLocation, long systemTime) {
    if (paused) {
      Log.w(TAG,
          "Tried to account for location while track is paused");
      return false;
    }

    totalLocations++;

    double elevationDifference = updateElevation(currentLocation.getAltitude());

    // Update the "instant" values:
    data.setTotal_time(systemTime - data.getStart_time());
    currentSpeed = currentLocation.getSpeed();

    // This was the 1st location added, remember it and do nothing else:
    if (lastLocation == null) 
    {
      lastLocation = currentLocation;
      lastMovingLocation = currentLocation;
      return false;
    }

    updateBounds(currentLocation);

    // Don't do anything if we didn't move since last fix:
    double distance = lastLocation.distanceTo(currentLocation);
    if (distance < minRecordingDistance && currentSpeed < Constants.MAX_NO_MOVEMENT_SPEED) 
    {
      lastLocation = currentLocation;
      return false;
    }

    data.addTotalDistance(lastMovingLocation.distanceTo(currentLocation));
    updateSpeed(currentLocation.getTime(), currentSpeed,
        lastLocation.getTime(), lastLocation.getSpeed());

    updateGrade(distance, elevationDifference);
    lastLocation = currentLocation;
    lastMovingLocation = currentLocation;
    return true;
  }

  /**
   * Updates the track's bounding box to include the given location.
   */
  private void updateBounds(Location location) {
    data.updateLatitudeExtremities(location.getLatitude());
    data.updateLongitudeExtremities(location.getLongitude());
  }

  /**
   * Updates the elevation measurements.
   * 
   * @param elevation the current elevation
   */
  // @VisibleForTesting
  double updateElevation(double elevation) 
  {
    double oldSmoothedElevation = getSmoothedElevation();
    elevationBuffer.addNext(elevation);
    double smoothedElevation = getSmoothedElevation();
    data.updateElevationExtremities(smoothedElevation);
    double elevationDifference = elevationBuffer.isFull()
        ? smoothedElevation - oldSmoothedElevation
        : 0.0;
    if (elevationDifference > 0) 
    {
      data.addTotalElevationGain(elevationDifference);
    }
    return elevationDifference;
  }

  /**
   * Updates the speed measurements.
   * 
   * @param updateTime the time of the speed update
   * @param speed the current speed
   * @param lastLocationTime the time of the last speed update
   * @param lastLocationSpeed the speed of the last update
   */
  // @VisibleForTesting
  void updateSpeed(long updateTime, double speed, long lastLocationTime,
      double lastLocationSpeed) {
    // We are now sure the user is moving.
    long timeDifference = updateTime - lastLocationTime;
    if (timeDifference < 0) 
    {
      Log.e(TAG,
          "Found negative time change: " + timeDifference);
    }
    data.addMovingTime(timeDifference);

    if (isValidSpeed(updateTime, speed, lastLocationTime, lastLocationSpeed,speedBuffer)) 
    {
      speedBuffer.addNext(speed);
      if (speed > data.getMax_speed()) 
      {
        data.setMax_speed(speed);
      }
      double movingSpeed = data.getAverageMovingSpeed();
      if (speedBuffer.isFull() && (movingSpeed > data.getMax_speed())) 
      {
        data.setMax_speed(movingSpeed);
      }
    } 
    else 
    {
      Log.d(TAG, "TripStatistics ignoring big change: Raw Speed: " + speed + " old: " + lastLocationSpeed + " [" + toString() + "]");
    }
  }

  /**
   * Checks to see if this is a valid speed.
   * 
   * @param updateTime The time at the current reading
   * @param speed The current speed
   * @param lastLocationTime The time at the last location
   * @param lastLocationSpeed Speed at the last location
   * @param speedBuffer A buffer of recent readings
   * @return True if this is likely a valid speed
   */
  public static boolean isValidSpeed(long updateTime, double speed,
      long lastLocationTime, double lastLocationSpeed,
      DoubleBuffer speedBuffer) 
  {

    // We don't want to count 0 towards the speed.
    if (speed == 0) {
      return false;
    }
    // We are now sure the user is moving.
    long timeDifference = updateTime - lastLocationTime;

    // There are a lot of noisy speed readings.
    // Do the cheapest checks first, most expensive last.
    // The following code will ignore unlikely to be real readings.
    // - 128 m/s seems to be an internal android error code.
    if (Math.abs(speed - 128) < 1) {
      return false;
    }

    // Another check for a spurious reading. See if the path seems physically
    // likely. Ignore any speeds that imply accelerations greater than 2g's
    // Really who can accelerate faster?
    double speedDifference = Math.abs(lastLocationSpeed - speed);
    if (speedDifference > Constants.MAX_ACCELERATION * timeDifference) {
      return false;
    }

    // There are three additional checks if the reading gets this far:
    // - Only use the speed if the buffer is full
    // - Check that the current speed is less than 10x the recent smoothed speed
    // - Double check that the current speed does not imply crazy acceleration
    double smoothedSpeed = speedBuffer.getAverage();
    double smoothedDiff = Math.abs(smoothedSpeed - speed);
    return !speedBuffer.isFull() ||
        (speed < smoothedSpeed * 10
         && smoothedDiff < Constants.MAX_ACCELERATION * timeDifference);
  }

  /**
   * Updates the grade measurements.
   * 
   * @param distance the distance the user just travelled
   * @param elevationDifference the elevation difference between the current
   *        reading and the previous reading
   */
  // @VisibleForTesting
  void updateGrade(double distance, double elevationDifference) 
  {
    distanceBuffer.addNext(distance);
    double smoothedDistance = distanceBuffer.getAverage();

    // With the error in the altitude measurement it is dangerous to divide
    // by anything less than 5.
    if (!elevationBuffer.isFull() || !distanceBuffer.isFull()
        || smoothedDistance < 5.0) 
    {
      return;
    }
    currentGrade = elevationDifference / smoothedDistance;
    gradeBuffer.addNext(currentGrade);
    data.updateGradeExtremities(gradeBuffer.getAverage());
  }

  /**
   * Pauses the track at the given time.
   * 
   * @param time the time to pause at
   */
  public void pauseAt(long time) 
  {
    if (paused) { return; }

    data.setStop_time(time);
    data.setTotal_time(time - data.getStart_time());
    lastLocation = null; // Make sure the counter restarts.
    paused = true;
  }

  /**
   * Resumes the current track at the given time.
   *
   * @param time the time to resume at
   */
  public void resumeAt(long time) 
  {
    if (!paused) 
    { 
      return; 
    }

    // TODO: The times are bogus if the track is paused then resumed again
    data.setStart_time(time);
    data.setStop_time(-1);
    paused = false;
  }

  @Override
  public String toString() {
    return "TripStatistics { Data: " + data.toString()
         + "; Total Locations: " + totalLocations
         + "; Paused: " + paused
         + "; Current speed: " + currentSpeed
         + "; Current grade: " + currentGrade
         + "}";
  }

  /**
   * Returns the amount of time the user has been idle or 0 if they are moving.
   * idle time = lastLocation.getTime - lastMovingLocation.getTime 
   */
  public long getIdleTime() {
    if (lastLocation == null || lastMovingLocation == null)
      return 0;
    return lastLocation.getTime() - lastMovingLocation.getTime();
  }
  /**
   * @return last good location received from gps or network cell which is valid.
   */
  public Location getLastLocation()
  {
    return lastLocation;
  }
  /**
   * Gets the current elevation smoothed over several readings. The elevation
   * data is very noisy so it is better to use the smoothed elevation than the
   * raw elevation for many tasks.
   * 
   * @return The elevation smoothed over several readings
   */
  public double getSmoothedElevation() {
    return elevationBuffer.getAverage();
  }

  public RouteStatistics getStatistics() {
    // Take a snapshot - we don't want anyone messing with our internals
    return new RouteStatistics(data);
  }

  public void setMinRecordingDistance(int minRecordingDistance) {
    this.minRecordingDistance = minRecordingDistance;
  }
}

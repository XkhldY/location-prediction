package com.google.android.utilities;

import java.text.NumberFormat;



import com.google.android.lib.content.RouteTrackPoint;
import com.google.android.lib.content.data.DescriptionGenerator;
import com.google.android.lib.statistics.RouteStatistics;

import com.google.android.location.content.R;

import android.content.Context;

/**
 * Various string manipulation methods.
 *
 * @author Andrei
 */
public class StringsProfiler implements DescriptionGenerator {

  private final Context context;

  /**
   * Formats a number of milliseconds as a string.
   *
   * @param time - A period of time in milliseconds.
   * @return A string of the format M:SS, MM:SS or HH:MM:SS
   */
  public static String formatTime(long time) {
    return formatTimeInternal(time, false);
  }

  /**
   * Formats a number of milliseconds as a string. To be used when we need the
   * hours to be shown even when it is zero, e.g. exporting data to a
   * spreadsheet.
   *
   * @param time - A period of time in milliseconds
   * @return A string of the format HH:MM:SS even if time is less than 1 hour
   */
  public static String formatTimeAlwaysShowingHours(long time) {
    return formatTimeInternal(time, true);
  }

  private static final NumberFormat SINGLE_DECIMAL_PLACE_FORMAT = NumberFormat.getNumberInstance();
  
  static {
    SINGLE_DECIMAL_PLACE_FORMAT.setMaximumFractionDigits(1);
    SINGLE_DECIMAL_PLACE_FORMAT.setMinimumFractionDigits(1);
  }

  /**
   * Formats a double precision number as decimal number with a single decimal
   * place.
   *
   * @param number A double precision number
   * @return A string representation of a decimal number, derived from the input
   *         double, with a single decimal place
   */
  public static final String formatSingleDecimalPlace(double number) {
    return SINGLE_DECIMAL_PLACE_FORMAT.format(number);
  }



  /**
   * Formats a number of milliseconds as a string.
   *
   * @param time - A period of time in milliseconds
   * @param alwaysShowHours - Whether to display 00 hours if time is less than 1
   *        hour
   * @return A string of the format HH:MM:SS
   */
  private static String formatTimeInternal(long time, boolean alwaysShowHours) {
    int[] parts = getTimeParts(time);
    StringBuilder builder = new StringBuilder();
    if (parts[2] > 0 || alwaysShowHours) {
      builder.append(parts[2]);
      builder.append(':');
      if (parts[1] <= 9) {
        builder.append("0");
      }
    }

    builder.append(parts[1]);
    builder.append(':');
    if (parts[0] <= 9) {
      builder.append("0");
    }
    builder.append(parts[0]);

    return builder.toString();
  }

  /**
   * Gets the time as an array of parts.
   */
  public static int[] getTimeParts(long time) {
    if (time < 0) {
      int[] parts = getTimeParts(time * -1);
      parts[0] *= -1;
      parts[1] *= -1;
      parts[2] *= -1;
      return parts;
    }
    int[] parts = new int[3];

    long seconds = time / 1000;
    parts[0] = (int) (seconds % 60);
    int tmp = (int) (seconds / 60);
    parts[1] = tmp % 60;
    parts[2] = tmp / 60;

    return parts;
  }

  public StringsProfiler(Context context) {
    this.context = context;
  }
  /**
   * Generates a description for a route track point (with information about the
   * statistics).
   *
   * @return a route description
   */
  public String generateRouteTrackPointDescription(RouteTrackPoint rtp) {
    RouteStatistics stats = rtp.getRouteStatistics();

    final double distanceInKm = stats.getTotalDistance() / 1000;
    final double distanceInMiles = distanceInKm * UnitConversions.KM_TO_MI;
    final double averageSpeedInKmh = stats.getAverageSpeed() * 3.6;
    final double averageSpeedInMph =
        averageSpeedInKmh * UnitConversions.KMH_TO_MPH;
    final double movingSpeedInKmh = stats.getAverageMovingSpeed() * 3.6;
    final double movingSpeedInMph =
        movingSpeedInKmh * UnitConversions.KMH_TO_MPH;
    final double maxSpeedInKmh = stats.getMax_speed() * 3.6;
    final double maxSpeedInMph = maxSpeedInKmh * UnitConversions.KMH_TO_MPH;
    final long minElevationInMeters = Math.round(stats.getMinElevation());
    final long minElevationInFeet =
        Math.round(stats.getMinElevation() * UnitConversions.M_TO_FT);
    final long maxElevationInMeters = Math.round(stats.getMaxElevation());
    final long maxElevationInFeet =
        Math.round(stats.getMaxElevation() * UnitConversions.M_TO_FT);
    final long elevationGainInMeters =
        Math.round(stats.getTotal_elevation_gain());
    final long elevationGainInFeet = Math.round(
        stats.getTotal_elevation_gain() * UnitConversions.M_TO_FT);
    long theMinGrade = 0;
    long theMaxGrade = 0;
    double maxGrade = stats.getMaxGrade();
    double minGrade = stats.getMinGrade();
    if (!Double.isNaN(maxGrade) &&
        !Double.isInfinite(maxGrade)) {
      theMaxGrade = Math.round(maxGrade * 100);
    }
    if (!Double.isNaN(minGrade) &&
        !Double.isInfinite(minGrade)) {
      theMinGrade = Math.round(minGrade * 100);
    }
    final String percent = "%";

    return String.format(
        "%s: %.2f %s (%.1f %s)\n"
        + "%s: %s\n"
        + "%s: %s\n"
        + "%s: %.2f %s (%.1f %s)\n"
        + "%s: %.2f %s (%.1f %s)\n"
        + "%s: %.2f %s (%.1f %s)\n"
        + "%s: %d %s (%d %s)\n"
        + "%s: %d %s (%d %s)\n"
        + "%s: %d %s (%d %s)\n"
        + "%s: %d %s\n"
        + "%s: %d %s\n",
        context.getString(R.string.stat_total_distance),
            distanceInKm, context.getString(R.string.unit_kilometer),
            distanceInMiles, context.getString(R.string.unit_mile),
        context.getString(R.string.stat_total_time),
            StringsProfiler.formatTime(stats.getTotal_time()),
        context.getString(R.string.stat_moving_time),
        StringsProfiler.formatTime(stats.getMoving_time()),
        context.getString(R.string.stat_average_speed),
            averageSpeedInKmh, context.getString(R.string.unit_kilometer_per_hour),
            averageSpeedInMph, context.getString(R.string.unit_mile_per_hour),
        context.getString(R.string.stat_average_moving_speed),
            movingSpeedInKmh, context.getString(R.string.unit_kilometer_per_hour),
            movingSpeedInMph, context.getString(R.string.unit_mile_per_hour),
        context.getString(R.string.stat_max_speed),
            maxSpeedInKmh, context.getString(R.string.unit_kilometer_per_hour),
            maxSpeedInMph, context.getString(R.string.unit_mile_per_hour),
        context.getString(R.string.stat_min_elevation),
            minElevationInMeters, context.getString(R.string.unit_meter),
            minElevationInFeet, context.getString(R.string.unit_feet),
        context.getString(R.string.stat_max_elevation),
            maxElevationInMeters, context.getString(R.string.unit_meter),
            maxElevationInFeet, context.getString(R.string.unit_feet),
        context.getString(R.string.stat_elevation_gain),
            elevationGainInMeters, context.getString(R.string.unit_meter),
            elevationGainInFeet, context.getString(R.string.unit_feet),
        context.getString(R.string.stat_max_grade),
            theMaxGrade, percent,
        context.getString(R.string.stat_min_grade),
            theMinGrade, percent);
  }
}

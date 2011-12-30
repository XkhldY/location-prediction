package com.google.android.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.location.Location;
import android.util.Log;


import com.google.android.lib.content.Route;
import com.google.android.lib.statistics.RouteStatistics;
import com.google.android.location.content.Constants;
import com.google.android.maps.GeoPoint;


/**
 * Utility class for decimating tracks at a given level of precision.
 *
 * @author Andrei
 */
public class LocationFilter {
  /**
   * Computes the distance on the two sphere between the point c0 and the line
   * segment c1 to c2.
   *
   * @param c0 the first coordinate
   * @param c1 the beginning of the line segment
   * @param c2 the end of the lone segment
   * @return the distance in m (assuming spherical earth)
   */
  public static double distance(final Location c0, final Location c1, final Location c2) 
  {
    if (c1.equals(c2)) 
    {
      return c2.distanceTo(c0);
    }

    final double s0lat = c0.getLatitude() * UnitConversions.TO_RADIANS;
    final double s0lng = c0.getLongitude() * UnitConversions.TO_RADIANS;
    final double s1lat = c1.getLatitude() * UnitConversions.TO_RADIANS;
    final double s1lng = c1.getLongitude() * UnitConversions.TO_RADIANS;
    final double s2lat = c2.getLatitude() * UnitConversions.TO_RADIANS;
    final double s2lng = c2.getLongitude() * UnitConversions.TO_RADIANS;

    double s2s1lat = s2lat - s1lat;
    double s2s1lng = s2lng - s1lng;
    final double u =
        ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
            / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);
    if (u <= 0) {
      return c0.distanceTo(c1);
    }
    if (u >= 1) {
      return c0.distanceTo(c2);
    }
    Location sa = new Location("");
    sa.setLatitude(c0.getLatitude() - c1.getLatitude());
    sa.setLongitude(c0.getLongitude() - c1.getLongitude());
    Location sb = new Location("");
    sb.setLatitude(u * (c2.getLatitude() - c1.getLatitude()));
    sb.setLongitude(u * (c2.getLongitude() - c1.getLongitude()));
    return sa.distanceTo(sb);
  }

  /**
   * Decimates the given locations for a given zoom level. This uses a
   * Douglas-Peucker decimation algorithm.
   *
   * decimate a collection of points but uses a recursive version
   * @param threshold - the threshold for distances between points
   * @param locations - an array of locations
   */
   public static ArrayList<Location> decimateLocationsRecursive(double threshold, ArrayList<Location>locations)
   {
     double maxDistance = 0;
     int index = 0;
     ArrayList<Location>result1 = new ArrayList<Location>();
     ArrayList<Location>result2 = new ArrayList<Location>();
     ArrayList<Location>resultFinal = new ArrayList<Location>();
     int mLength = locations.size();
     for (int  k = 1;k < mLength-1;k++)
     {
       double distance =  calculateTotalDistance(locations.get(k) ,locations.get(0), locations.get(mLength));
       if(distance > maxDistance)
       {
          maxDistance = distance;
          index = k;
       }
     }
     if(maxDistance > threshold)
     {
        for(int i =0;i < index;i++)
        {
            result1.add(locations.get(i));
            result1 = decimateLocationsRecursive(threshold, result1);
            for(int k1 = 0;k1 < result1.size()-1;k1++)
            {
              resultFinal.add(result1.get(k1));
            }
        }
        for(int j =index+1;j < mLength;j++)
        {
           result2.add(locations.get(j));
           result2 = decimateLocationsRecursive(threshold, result2);
           for(int k2 = 0;k2 < result2.size();k2++)
           {
             resultFinal.add(result1.get(k2));
           }
        }
     }
     else
     {
         resultFinal.add(locations.get(0));
         resultFinal.add(locations.get(mLength));   
     }  
     
     return resultFinal;
   }
   public static double calculateTotalDistance(Location from , Location firstTo, Location secondTo)
   {
     return distance(from, firstTo, secondTo);
   }
  
  /**
   * Decimates the given locations for a given zoom level. This uses a
   * Douglas-Peucker decimation algorithm.
   *
   * @param tolerance in meters
   * @param locations input
   * @param decimated output
   */
  public static void decimateLocationsNonRecursive(double tolerance, ArrayList<Location> locations, ArrayList<Location> decimated) {
    final int n = locations.size();
    if (n < 1) 
    {
      return;
    }
    int idx;
    int maxIdx = 0;
    Stack<int[]> stack = new Stack<int[]>();
    double[] dists = new double[n];
    dists[0] = 1;
    dists[n - 1] = 1;
    double maxDist;
    double dist = 0.0;
    int[] current;

    if (n > 2) 
    {
      int[] stackVal = new int[] {0, (n - 1)};
      stack.push(stackVal);
      while (stack.size() > 0) 
      {
        current = stack.pop();
        maxDist = 0;
        for (idx = current[0] + 1; idx < current[1]; ++idx) 
        {
          dist = LocationFilter.distance(locations.get(idx), locations.get(current[0]), locations.get(current[1]));
          if (dist > maxDist) 
          {
            maxDist = dist;
            maxIdx = idx;
          }
        }
        if (maxDist > tolerance) 
        {
          dists[maxIdx] = maxDist;
          int[] stackValCurMax = {current[0], maxIdx};
          stack.push(stackValCurMax);
          int[] stackValMaxCur = {maxIdx, current[1]};
          stack.push(stackValMaxCur);
        }
      }
    }

    int i = 0;
    idx = 0;
    decimated.clear();
    for (Location l : locations) 
    {
      if (dists[idx] != 0) 
      {
        decimated.add(l);
        i++;
      }
      idx++;
    }
    Log.d(Constants.TAG, "Decimating " + n + " points to " + i
        + " w/ tolerance = " + tolerance);
  }

  /**
   * Decimates the given route for the given precision.
   *
   * @param route a route
   * @param precision desired precision in meters
   */
  public static void decimate(Route route, double precision) {
    ArrayList<Location> decimated = new ArrayList<Location>();
    decimateLocationsNonRecursive(precision, route.getLocations(), decimated);
    route.setLocations(decimated);
  }

  /**
   * Limits number of points by dropping any points beyond the given number of
   * points. Note: That'll actually discard points.
   *
   * @param route a route
   * @param numberOfPoints maximum number of points
   */
  public static void cut(Route route, int numberOfPoints) 
  {
    ArrayList<Location> locations = route.getLocations();
    while (locations.size() > numberOfPoints) {
      locations.remove(locations.size() - 1);
    }
  }

  /**
   * Splits a route in multiple tracks where each piece has less or equal than
   * maxPoints.
   *
   * @param route the route to split
   * @param maxPoints maximum number of points for each piece
   * @return a list of one or more route pieces
   */
  public static ArrayList<Route> split(Route route, int maxPoints) 
  {
    ArrayList<Route> result = new ArrayList<Route>();
    final int nTotal = route.getLocations().size();
    int n = 0;
    Route piece = null;
    do 
    {
      piece = new Route();
      RouteStatistics pieceStats = piece.getRouteStatistics();
      piece.setId(route.getId());
      piece.setName(route.getName());
      piece.setDescription(route.getDescription());
      piece.setCategory(route.getCategory());
      List<Location> pieceLocations = piece.getLocations();
      for (int i = n; i < nTotal && pieceLocations.size() < maxPoints; i++) 
      {
        piece.addLocation(route.getLocations().get(i));
      }
      int nPointsPiece = pieceLocations.size();
      if (nPointsPiece >= 2) 
      {
        pieceStats.setStart_time(pieceLocations.get(0).getTime());
        pieceStats.setStop_time(pieceLocations.get(nPointsPiece - 1).getTime());
        result.add(piece);
      }
      n += (pieceLocations.size() - 1);
    } while (n < nTotal && piece.getLocations().size() > 1);

    return result;
  }

  /**
   * Test if a given GeoPoint is valid, i.e. within physical bounds.
   *
   * @param geoPoint the point to be tested
   * @return true, if it is a physical location on earth.
   */
  public static boolean isValidGeoPoint(GeoPoint geoPoint) {
    return Math.abs(geoPoint.getLatitudeE6()) < 90E6 && Math.abs(geoPoint.getLongitudeE6()) <= 180E6;
  }

  /**
   * Checks if a given location is a valid (i.e. physically possible) location
   * on Earth. Note: The special separator locations (which have latitude =
   * 100) will not qualify as valid. Neither will locations with lat=0 and long=0
   * as these are most likely "bad" measurements which often cause trouble.
   *
   * @param location the location to test
   * @return true if the location is a valid location.
   */
  public static boolean isValidLocation(Location location) 
  {
    return location != null && Math.abs(location.getLatitude()) <= 90 && Math.abs(location.getLongitude()) <= 180;
  }

  /**
   * Gets a location from a GeoPoint.
   *
   * @param p a GeoPoint
   * @return the corresponding location
   */
  public static Location getLocation(GeoPoint p) {
    Location result = new Location("");
    result.setLatitude(p.getLatitudeE6() / 1.0E6);
    result.setLongitude(p.getLongitudeE6() / 1.0E6);
    return result;
  }

  public static GeoPoint getGeoPoint(Location location) {
    return new GeoPoint((int) (location.getLatitude() * 1E6),
                        (int) (location.getLongitude() * 1E6));
  }

  /**
   * This is a utility class w/ only static members.
   */
  private LocationFilter() {
  }
}

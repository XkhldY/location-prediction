package com.google.android.lib.services;
import com.google.android.lib.content.CreateRouteTrackPoint;

interface IRouteRecordingService
{
 /**
 *check if it's recording or not.
 **/
  boolean isRecording();
  /**
  * insert an reoute track point and returns 
  *@params id of the new route track points
  */
  long insertAndReturnRouteTPId(in CreateRouteTrackPoint track);
  /**
  * start a new route returning
  *@prams id of the route
  *
  */ 
  long startNewRouteId();
  /**
  * returns the id of the route
  *being recorded
  *@params id of the current recording route
  */
  long getRecordingRouteId();
  /**
  *This is used only for special GPS or Network points given the fact that we have attach a location listener
  * to the service so this should be in special cases.
  *
  */
  void recordLocation(in Location location);
  /** calculate statistics
  * for every point and route
  */
  void calculateStatistics();
  /** check if a current point already exists in the database
  *
  */
  boolean existEndPointAtId();
  /**stop recording
  *
  */
  void endCurrentRoute();
  byte[]getSensorData();
  int getSensorState();
  
}
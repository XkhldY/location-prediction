package com.google.android.lib.services;
import com.google.android.lib.content.CreateTrack;

interface IRouteRecordingService
{
 /**
 *check if it's recording or not.
 **/
  boolean isRecording();
  /**
  * insert an endpoint and returns 
  *@params id of the new endpoint
  */
  long insertEndPointWithId(in CreateTrack track);
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
  *while recording records a new locating
  *
  */
  void recordLocation();
  /** calculate statistics
  * for every point and route
  */
  void calculateStatistics();
  /** check fi a current point already exists in the database
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
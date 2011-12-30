package com.google.android.utilities;

import java.text.DateFormat;
import java.util.Date;

import com.google.android.location.content.Constants;
import com.google.android.location.content.R;

import android.content.Context;
import android.content.SharedPreferences;



/**
* Creates a default track name based on the current default track name policy.
* 
* @author Andrei
*/
public class DefaultRouteNameFactory {
 private final Context context;

 public DefaultRouteNameFactory(Context context) 
 {
   this.context = context;
 }

 /**
  * Creates a new track name.
  * 
  * @param routeId The ID for the current track.
  * @param startTime The start time, in milliseconds since the epoch, of the
  *     current track.
  * @return The new track name.
  */
 public String newTrackName(long routeId, long startTime) 
 {
   if (useTimestampTrackName()) 
   {
     DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
     return formatter.format(new Date(startTime));
   } 
   else 
   {
     return String.format(context.getString(R.string.route_name_format), routeId);
   }
 }

 /** Determines whether the preferences allow a timestamp-based track name */
 protected boolean useTimestampTrackName()
 {
   SharedPreferences prefs = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
   return prefs.getBoolean(context.getString(R.string.timestamp_route_name_key), true);
 }
}

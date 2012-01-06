package com.google.android.location.content;

import java.util.List;

import com.google.android.location.content.libcontent.Route;
import com.google.android.location.content.libdata.MyRouteProvider;
import com.google.android.location.content.libstatistics.RouteStatistics;
import com.google.android.locatiom.maps.myroutes.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;


/**
* Activity for viewing the combined statistics for all the recorded tracks.
*
* Other features to add - menu items to change setings.
*
* @author Andrei
*/
public class AggregatedStatsActivity extends Activity implements
       OnSharedPreferenceChangeListener {

 private final StatsUtilities utils;

 private MyRouteProvider routesProvider;

 private boolean metricUnits = true;

 public AggregatedStatsActivity() {
   this.utils = new StatsUtilities(this);
 }

 @Override
 public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
         String key) {
   Log.d(Constants.TAG, "StatsActivity: onSharedPreferences changed "
           + key);
   if (key != null) {
     if (key.equals(getString(R.string.metric_units_key))) {
       metricUnits = sharedPreferences.getBoolean(
               getString(R.string.metric_units_key), true);
       utils.setMetricUnits(metricUnits);
       utils.updateUnits();
       loadAggregatedStats();
     }
   }
 }

 @Override
 protected void onCreate(Bundle savedInstanceState) {
   super.onCreate(savedInstanceState);

   this.routesProvider = MyRouteProvider.Factory.getRouteProvider(this);

   // We don't need a window title bar:
   requestWindowFeature(Window.FEATURE_NO_TITLE);
   setContentView(R.layout.mylayout_statistics);

   ScrollView sv = ((ScrollView) findViewById(R.id.scrolly));
   sv.setScrollBarStyle(ScrollView.SCROLLBARS_OUTSIDE_INSET);

   SharedPreferences preferences = getSharedPreferences(
       Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
   if (preferences != null) {
     metricUnits = preferences.getBoolean(getString(R.string.metric_units_key), true);
     preferences.registerOnSharedPreferenceChangeListener(this);
   }
   utils.setMetricUnits(metricUnits);
   utils.updateUnits();
   utils.setSpeedLabel(R.id.speed_label, R.string.stat_speed, R.string.stat_pace);
   utils.setSpeedLabels();

   DisplayMetrics metrics = new DisplayMetrics();
   getWindowManager().getDefaultDisplay().getMetrics(metrics);
   if (metrics.heightPixels > 600) {
     ((TextView) findViewById(R.id.speed_register)).setTextSize(80.0f);
   }
   loadAggregatedStats();
 }

 /**
  * 1. Reads routes from the db
  * 2. Merges the trip stats from the routes
  * 3. Updates the view
  */
 private void loadAggregatedStats() {
   List<Route> routes = retrieveTracks();
   RouteStatistics rollingStats = null;
   if (!routes.isEmpty()) {
     rollingStats = new RouteStatistics(routes.iterator().next()
             .getRouteStatistics());
     for (int i = 1; i < routes.size(); i++) {
       rollingStats.merge(routes.get(i).getRouteStatistics());
     }
   }
   updateView(rollingStats);
 }

 private List<Route> retrieveTracks() {
   return routesProvider.getAllRoutes();
 }

 private void updateView(RouteStatistics aggStats) {
   if (aggStats != null) {
     utils.setAllStats(aggStats);
   }
 }
}

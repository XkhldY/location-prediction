package com.google.android.location.content;

import com.google.android.location.content.libcontent.RouteTrackPoint;
import com.google.android.location.content.libcontent.RoutesTrackPointsColumns;
import com.google.android.location.content.libdata.MyRouteProvider;
import com.google.android.location.content.libstatistics.RouteStatistics;
import com.google.android.locatiom.maps.myroutes.R;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class RouteTrackPointDetails extends Activity
implements OnClickListener {

public static final String ROUTE_TRACK_POINT_ID_EXTRA = "com.google.android.location.content.ROUTE_TRACK_POINT_ID";

/**
* The id of the way point being edited (taken from bundle, "routepointid")
*/
private Long routepointid;

private EditText name;
private EditText description;
private AutoCompleteTextView category;
private View detailsView;
private View statsView;

private StatsUtilities utils;
private RouteTrackPoint routeTrackPoint;

@Override
protected void onCreate(Bundle bundle) {
super.onCreate(bundle);
setContentView(R.layout.mytracks_waypoint_details);

utils = new StatsUtilities(this);
SharedPreferences preferences = getSharedPreferences(
    Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
if (preferences != null) {
  boolean useMetric =
      preferences.getBoolean(getString(R.string.metric_units_key), true);
  utils.setMetricUnits(useMetric);

  boolean displaySpeed =
      preferences.getBoolean(getString(R.string.report_speed_key), true);
  utils.setReportSpeed(displaySpeed);

  utils.updateWaypointUnits();
  utils.setSpeedLabels();
}

// Required extra when launching this intent:
routepointid = getIntent().getLongExtra(ROUTE_TRACK_POINT_ID_EXTRA, -1);
if (routepointid < 0) {
  Log.d(Constants.TAG,
      "MyRouteTrackPoints extra intent was launched w/o routeTrackPoint id.");
  finish();
  return;
}

// Optional extra that can be used to suppress the cancel button:
boolean hasCancelButton =
    getIntent().getBooleanExtra("hasCancelButton", true);

name = (EditText) findViewById(R.id.waypointdetails_name);
description = (EditText) findViewById(R.id.waypointdetails_description);
category =
    (AutoCompleteTextView) findViewById(R.id.waypointdetails_category);
statsView = findViewById(R.id.waypoint_stats);
ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
    this,
    R.array.waypoint_types,
    android.R.layout.simple_dropdown_item_1line);
category.setAdapter(adapter);
detailsView = findViewById(R.id.waypointdetails_description_layout);

Button cancel = (Button) findViewById(R.id.waypointdetails_cancel);
if (hasCancelButton) {
  cancel.setOnClickListener(this);
  cancel.setVisibility(View.VISIBLE);
} else {
  cancel.setVisibility(View.INVISIBLE);
}
Button save = (Button) findViewById(R.id.waypointdetails_save);
save.setOnClickListener(this);

fillDialog();
}

private void fillDialog() {
routeTrackPoint = MyRouteProvider.Factory.getRouteProvider(this).getRouteTrackPointById(routepointid);
if (routeTrackPoint != null) {
  name.setText(routeTrackPoint.getName());
  ImageView icon = (ImageView) findViewById(R.id.waypointdetails_icon);
  int iconId = -1;
  switch(routeTrackPoint.getType()) {
    case RouteTrackPoint.TYPE_OF_TRACKPOINT:
      description.setText(routeTrackPoint.getDescription());
      detailsView.setVisibility(View.VISIBLE);
      category.setText(routeTrackPoint.getCategory());
      statsView.setVisibility(View.GONE);
      iconId = R.drawable.blue_pushpin;
      break;
    case RouteTrackPoint.TRACKPOINT_STATISTICS:
      detailsView.setVisibility(View.GONE);
      statsView.setVisibility(View.VISIBLE);
      iconId = R.drawable.ylw_pushpin;
      RouteStatistics waypointStats = routeTrackPoint.getRouteStatistics();
      utils.setAllStats(waypointStats);
      utils.setAltitude(
          R.id.elevation_register, routeTrackPoint.getLocation().getAltitude());
      name.setImeOptions(EditorInfo.IME_ACTION_DONE);
      break;
  }
  icon.setImageDrawable(getResources().getDrawable(iconId));
}
}

private void saveDialog() {
ContentValues values = new ContentValues();
values.put(RoutesTrackPointsColumns.NAME, name.getText().toString());
if (routeTrackPoint != null && routeTrackPoint.getType() == RouteTrackPoint.TRACKPOINT_STATISTICS) {
  values.put(RoutesTrackPointsColumns.DESCRIPTION,
      description.getText().toString());
  values.put(RoutesTrackPointsColumns.CATEGORY, category.getText().toString());
}
getContentResolver().update(
    RoutesTrackPointsColumns.CONTENT_URI,
    values,
    "_id = " + routepointid,
    null /*selectionArgs*/);
}

@Override
public void onClick(View v) {
switch (v.getId()) {
  case R.id.waypointdetails_cancel:
    finish();
    break;
  case R.id.waypointdetails_save:
    saveDialog();
    finish();
    break;
}
}
}

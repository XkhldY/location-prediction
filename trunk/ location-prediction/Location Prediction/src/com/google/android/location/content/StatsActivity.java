package com.google.android.location.content;

import static com.google.android.location.content.Constants.TAG;

import java.util.EnumSet;

import com.google.android.location.content.libcontent.Route;
import com.google.android.location.content.libcontent.RouteTrackPoint;
import com.google.android.location.content.route.RouteManager;
import com.google.android.location.content.route.RouteMapDataListener;
import com.google.android.location.content.route.RouteManager.ListenerDataType;
import com.google.android.location.content.service.ServiceControl;
import com.google.android.locatiom.maps.myroutes.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;



public class StatsActivity extends Activity implements RouteMapDataListener {
	  /**
	   * A runnable for posting to the UI thread. Will update the total time field.
	   */
	  private final Runnable updateResults = new Runnable() {
	    public void run() {
	      if (dataHub != null && dataHub.isRecordingSelected()) 
	      {
	        utils.setTime(R.id.total_time_register,
	            System.currentTimeMillis() - startTime);
	      }
	    }
	  };

	  private StatsUtilities utils;
	  private UIUpdateThread thread;

	  /**
	   * The start time of the selected route.
	   */
	  private long startTime = -1;

	  private RouteManager dataHub;
	  private SharedPreferences preferences;

	  /**
	   * A thread that updates the total time field every second.
	   */
	  private class UIUpdateThread extends Thread {

	    public UIUpdateThread() {
	      super();
	      Log.i(TAG, "Created UI update thread");
	    }

	    @Override
	    public void run() {
	      Log.i(TAG, "Started UI update thread");
	      while (ServiceControl.isRecording(StatsActivity.this, null, preferences)) {
	        runOnUiThread(updateResults);
	        try {
	          Thread.sleep(1000L);
	        } catch (InterruptedException e) {
	          Log.w(TAG, "StatsActivity: Caught exception on sleep.", e);
	          break;
	        }
	      }
	      Log.w(TAG, "UIUpdateThread finished.");
	    }
	  }

	  /** Called when the activity is first created. */
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    preferences = getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
	    utils = new StatsUtilities(this);

	    // The volume we want to control is the Text-To-Speech volume
	   
	    // We don't need a window title bar:
	    requestWindowFeature(Window.FEATURE_NO_TITLE);

	    setContentView(R.layout.mylayout_statistics);

	    ScrollView sv = ((ScrollView) findViewById(R.id.scrolly));
	    sv.setScrollBarStyle(ScrollView.SCROLLBARS_OUTSIDE_INSET);

	    showUnknownLocation();

	    DisplayMetrics metrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
	    if (metrics.heightPixels > 600) {
	      ((TextView) findViewById(R.id.speed_register)).setTextSize(80.0f);
	    }
	  }

	  @Override
	  protected void onResume() {
	    super.onResume();

	    dataHub = ((MyRouteAppManager) getApplication()).getRouteDataHub();
	    dataHub.registerDataListener(this, EnumSet.of(
	        ListenerDataType.SELECTED_ROUTE_CHANGED,
	        ListenerDataType.ROUTE_UPDATES,
	        ListenerDataType.LOCATION_UPDATES,
	        ListenerDataType.DISPLAY_PREFERENCES));
	  }

	  @Override
	  protected void onPause() {
	    dataHub.unregisterTrackDataListener(this);
	    dataHub = null;

	    if (thread != null) {
	      thread.interrupt();
	      thread = null;
	    }

	    super.onStop();
	  }

	  @Override
	  public boolean onUnitsChanged(boolean metric) {
	    // Ignore if unchanged.
	    if (metric == utils.isMetricUnits()) return false;

	    utils.setMetricUnits(metric);
	    updateLabels();

	    return true;  // Reload data
	  }

	  @Override
	  public boolean onReportSpeedChanged(boolean displaySpeed) {
	    // Ignore if unchanged.
	    if (displaySpeed == utils.isReportSpeed()) return false;

	    utils.setReportSpeed(displaySpeed);
	    updateLabels();

	    return true;  // Reload data
	  }

	  private void updateLabels() {
	    runOnUiThread(new Runnable() {
	      @Override
	      public void run() {
	        utils.updateUnits();
	        utils.setSpeedLabel(R.id.speed_label, R.string.stat_speed, R.string.stat_pace);
	        utils.setSpeedLabels();
	      }
	    });
	  }

	  /**
	   * Updates the given location fields (latitude, longitude, altitude) and all
	   * other fields.
	   *
	   * @param l may be null (will set location fields to unknown)
	   */
	  private void showLocation(Location l) {
	    utils.setAltitude(R.id.elevation_register, l.getAltitude());
	    utils.setLatLong(R.id.latitude_register, l.getLatitude());
	    utils.setLatLong(R.id.longitude_register, l.getLongitude());
	    utils.setSpeed(R.id.speed_register, l.getSpeed() * 3.6);
	  }

	  private void showUnknownLocation() {
	    utils.setUnknown(R.id.elevation_register);
	    utils.setUnknown(R.id.latitude_register);
	    utils.setUnknown(R.id.longitude_register);
	    utils.setUnknown(R.id.speed_register);
	  }

	  @Override
	  public void onSelectedRouteChanged(Route route, boolean isRecording) {
	    /*
	     * Checks if this activity needs to update live route data or not.
	     * If so, make sure that:
	     * a) a thread keeps updating the total time
	     * b) a location listener is registered
	     * c) a content observer is registered
	     * Otherwise unregister listeners, observers, and kill update thread.
	     */
	    final boolean startThread = (thread == null) && isRecording;
	    final boolean killThread = (thread != null) && (!isRecording);
	    if (startThread) {
	      thread = new UIUpdateThread();
	      thread.start();
	    } else if (killThread) {
	      thread.interrupt();
	      thread = null;
	    }
	  }

	  @Override
	  public void onGPSCurrentLocationChanged(final Location loc) {
	    RouteManager localDataHub = dataHub;
	    if (localDataHub != null && localDataHub.isRecordingSelected()) {
	      runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	          if (loc != null) {
	            showLocation(loc);
	          } else {
	            showUnknownLocation();
	          }
	        }
	      });
	    }
	  }

	  @Override
	  public void onCurrentHeadingChanged(double heading) {
	    // We don't care.
	  }

	  @Override
	  public void onGPSProviderStateChange(MyGpsProviderState state) {
	    switch (state) {
	      case DISABLED:
	      case NO_FIX:
	        runOnUiThread(new Runnable() {
	          @Override
	          public void run() {
	            showUnknownLocation();
	          }
	        });
	        break;
	    }
	  }

	  @Override
	  public void onRouteUpdated(final Route route) {
	    RouteManager localDataHub = dataHub;
	    final boolean recordingSelected = localDataHub != null && localDataHub.isRecordingSelected();
	    runOnUiThread(new Runnable() {
	      @Override
	      public void run() {
	        if (route == null || route.getRouteStatistics() == null) {
	          utils.setAllToUnknown();
	          return;
	        }

	        startTime = route.getRouteStatistics().getStart_time();
	        if (!recordingSelected) {
	          utils.setTime(R.id.total_time_register,
	        		  route.getRouteStatistics().getTotal_time());
	          showUnknownLocation();
	        }
	        utils.setAllStats(route.getRouteStatistics());
	      }
	    });
	  }

	  @Override
	  public void clearRouteTrackPoints() {
	    // We don't care.
	  }

	  @Override
	  public void onNewRouteTrackPoint(RouteTrackPoint rtp) {
	    // We don't care.
	  }

	  @Override
	  public void onNewRouteTrackPointsDone() {
	    // We don't care.
	  }

	  @Override
	  public void clearRoutePoints() {
	    // We don't care.
	  }

	  @Override
	  public void onNewRoutePoint(Location loc) {
	    // We don't care.
	  }

	  @Override
	  public void onSegmentSplit() {
	    // We don't care.
	  }

	  @Override
	  public void onSampledOutRoutePoint(Location loc) {
	    // We don't care.
	  }

	  @Override
	  public void onNewRoutePointsDone() {
	    // We don't care.
	  }

	@Override
	public void onNetworkProviderStateChange(MyNetworkProviderState state) {
		// TODO Auto-generated method stub
		
	}
	}

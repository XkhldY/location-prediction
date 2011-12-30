package com.google.android.location.content.maps;

import android.location.Location;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.lib.content.Route;
import com.google.android.lib.content.RouteTrackPoint;
import com.google.android.location.route.RouteMapDataListener;
import com.google.android.maps.MapActivity;
/**
 * Map Activity for prediction it uses a different overlay than usual MapActivity
 * @author Andrei
 *
 */
public class MyPredictionMapActivity extends MapActivity implements RouteMapDataListener ,
View.OnTouchListener, View.OnClickListener 
{

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onGPSProviderStateChange(MyGpsProviderState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNetworkProviderStateChange(MyNetworkProviderState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGPSCurrentLocationChanged(Location loc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCurrentHeadingChanged(double heading) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelectedRouteChanged(Route route, boolean isRecording) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRouteUpdated(Route route) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearRoutePoints() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewRoutePoint(Location loc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSampledOutRoutePoint(Location loc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSegmentSplit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewRoutePointsDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearRouteTrackPoints() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewRouteTrackPoint(RouteTrackPoint route_track_point) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewRouteTrackPointsDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onUnitsChanged(boolean metric) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onReportSpeedChanged(boolean reportSpeed) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
}

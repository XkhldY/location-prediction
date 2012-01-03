package com.google.android.maps;

import static com.google.android.location.content.Constants.TAG;

import java.util.EnumSet;

import com.google.android.lib.content.Route;
import com.google.android.lib.content.RouteTrackPoint;

import com.google.android.lib.content.data.MyRouteProvider;
import com.google.android.lib.statistics.RouteStatistics;
import com.google.android.location.content.Constants;
import com.google.android.location.content.MyRouteAppManager;
import com.google.android.location.content.R;
import com.google.android.location.content.maps.GeoRectangle;
import com.google.android.location.route.RouteManager;
import com.google.android.location.route.RouteManager.ListenerDataType;
import com.google.android.location.route.RouteMapDataListener;
import com.google.android.utilities.LocationFilter;


import android.content.Intent;
import android.location.Location;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import android.view.View;
import android.view.Window;


import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MyMapActivity extends com.google.android.maps.MapActivity
		implements View.OnTouchListener, View.OnClickListener,
		RouteMapDataListener {

	// Saved instance state keys:
	// ---------------------------

	private static final String KEY_CURRENT_LOCATION = "currentLocation";
	private static final String KEY_KEEP_MY_LOCATION_VISIBLE = "keepMyLocationVisible";

	private RouteManager dataHub;

	/**
	 * True if the map should be scrolled so that the pointer is always in the
	 * visible area.
	 */
	private boolean keepMyLocationVisible;

	/**
	 * The current pointer location. This is kept to quickly center on it when
	 * the user requests.
	 */
	private Location currentLocation;

	// UI elements:
	// -------------

	private RelativeLayout screen;
	private MapView mapView;
	private MyRouteMapOverlay mapOverlay;
	private LinearLayout messagePane;
	private TextView messageText;
	private LinearLayout busyPane;


	private MenuItem myLocation;
	private MenuItem toggleLayers;

	/**
	 * We are not displaying driving directions. Just an arbitrary Route that is
	 * not associated to any licensed mapping data. Therefore it should be okay
	 * to return false here and still comply with the terms of service.
	 */
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * We are displaying a location. This needs to return true in order to
	 * comply with the terms of service.
	 */
	@Override
	protected boolean isLocationDisplayed() {
		return true;
	}

	// Application life cycle:
	// ------------------------

	@Override
	protected void onCreate(Bundle bundle) {
		Log.d(TAG, "MapActivity.onCreate");
		super.onCreate(bundle);

		// We don't need a window title bar:
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Inflate the layout:
		setContentView(R.layout.myroutes_layout);

		// Remove the window's background because the MapView will obscure it
		getWindow().setBackgroundDrawable(null);

		// Set up a map overlay:
		screen = (RelativeLayout) findViewById(R.id.screen);
		mapView = (MapView) findViewById(R.id.map);
		mapView.requestFocus();
		mapOverlay = new MyRouteMapOverlay(this);
		mapView.getOverlays().add(mapOverlay);
		mapView.setOnTouchListener(this);
		mapView.setBuiltInZoomControls(true);
		messagePane = (LinearLayout) findViewById(R.id.messagepane);
		messageText = (TextView) findViewById(R.id.messagetext);
		busyPane = (LinearLayout) findViewById(R.id.busypane);
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		Log.d(TAG, "MapActivity.onRestoreInstanceState");
		if (bundle != null) {
			super.onRestoreInstanceState(bundle);
			keepMyLocationVisible = bundle.getBoolean(
					KEY_KEEP_MY_LOCATION_VISIBLE, false);
			if (bundle.containsKey(KEY_CURRENT_LOCATION)) {
				currentLocation = (Location) bundle
						.getParcelable(KEY_CURRENT_LOCATION);
				if (currentLocation != null) {
					showCurrentLocation();
				}
			} else {
				currentLocation = null;
			}
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "MapActivity.onResume");
		super.onResume();

		dataHub = ((MyRouteAppManager) getApplication()).getRouteDataHub();
		dataHub.registerDataListener(this, EnumSet.of(
				ListenerDataType.SELECTED_ROUTE_CHANGED,
				ListenerDataType.POINT_UPDATES,
				ListenerDataType.ROUTE_TRACKPOINT_UPDATES,
				ListenerDataType.LOCATION_UPDATES,
				ListenerDataType.COMPASS_UPDATES));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "MapActivity.onSaveInstanceState");
		outState.putBoolean(KEY_KEEP_MY_LOCATION_VISIBLE, keepMyLocationVisible);
		if (currentLocation != null) {
			outState.putParcelable(KEY_CURRENT_LOCATION, currentLocation);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "MapActivity.onPause");

		dataHub.unregisterTrackDataListener(this);
		dataHub = null;

		super.onPause();
	}

	// Utility functions:
	// -------------------

	/**
	 * Shows the options button if a Route is selected, or hide it if not.
	 */
	private void updateOptionsButton(boolean trackSelected) {
		//optionsBtn.setVisibility(trackSelected ? View.VISIBLE : View.INVISIBLE);
	}

	/**
	 * Tests if a location is visible.
	 * 
	 * @param location
	 *            a given location
	 * @return true if the given location is within the visible map area needs
	 *         to be revised...
	 */
	private boolean locationIsVisible(Location location) {
		if (location == null || mapView == null) {
			return false;
		}
		GeoPoint center = mapView.getMapCenter();
		int latSpan = mapView.getLatitudeSpan();
		int lonSpan = mapView.getLongitudeSpan();

		// Bottom of map view is obscured by zoom controls/buttons.
		// Subtract a margin from the visible area:
		GeoPoint marginBottom = mapView.getProjection().fromPixels(0,
				mapView.getHeight());
		GeoPoint marginTop = mapView.getProjection().fromPixels(
				0,
				mapView.getHeight()
						- mapView.getZoomButtonsController().getZoomControls()
								.getHeight());
		int margin = Math.abs(marginTop.getLatitudeE6()
				- marginBottom.getLatitudeE6());
		GeoRectangle r = new GeoRectangle(center, latSpan, lonSpan);
		r.top += margin;

		GeoPoint geoPoint = LocationFilter.getGeoPoint(location);
		return r.contains(geoPoint);
	}

	/**
	 * Moves the location pointer to the current location and center the map if
	 * the current location is outside the visible area.
	 */
	private void showCurrentLocation() {
		if (mapOverlay == null || mapView == null) {
			return;
		}

		mapOverlay.setMyLocation(currentLocation);
		mapView.postInvalidate();

		if (currentLocation != null && keepMyLocationVisible
				&& !locationIsVisible(currentLocation)) {
			GeoPoint geoPoint = LocationFilter.getGeoPoint(currentLocation);
			MapController controller = mapView.getController();
			controller.animateTo(geoPoint);
		}
	}

	@Override
	public void onRouteUpdated(Route Route) {
		// We don't care.
	}

	/**
	 * Zooms and pans the map so that the given Route is visible.
	 * 
	 * @param Route
	 *            the Route
	 */
	private void zoomMapToBoundaries(Route route) {
		if (mapView == null) {
			return;
		}

		if (route == null || route.getNumberRoutePoints() < 2) {
			return;
		}

		RouteStatistics stats = route.getRouteStatistics();
		int bottom = stats.getLowestLatitude();
		int left = stats.getLowestLongitude();
		int latSpanE6 = stats.getHighestLatitude() - bottom;
		int lonSpanE6 = stats.getHighestLongitude() - left;
		if (latSpanE6 > 0 && latSpanE6 < 180E6 && lonSpanE6 > 0
				&& lonSpanE6 < 360E6) {
			keepMyLocationVisible = false;
			GeoPoint center = new GeoPoint(bottom + latSpanE6 / 2, left
					+ lonSpanE6 / 2);
			if (LocationFilter.isValidGeoPoint(center)) {
				mapView.getController().setCenter(center);
				mapView.getController().zoomToSpan(latSpanE6, lonSpanE6);
			}
		}
	}

	/**
	 * Zooms and pans the map so that the given waypoint is visible.
	 */
	public void showRouteTrackPoint(long routeTrackPointId) {
		MyRouteProvider providerUtils = MyRouteProvider.Factory
				.getRouteProvider(this);
		RouteTrackPoint wpt = providerUtils
				.getRouteTrackPointById(routeTrackPointId);
		if (wpt != null && wpt.getLocation() != null) {
			keepMyLocationVisible = false;
			GeoPoint center = new GeoPoint((int) (wpt.getLocation()
					.getLatitude() * 1E6), (int) (wpt.getLocation()
					.getLongitude() * 1E6));
			mapView.getController().setCenter(center);
			mapView.getController().setZoom(20);
			mapView.invalidate();
		}
	}

	@Override
	public void onSelectedRouteChanged(final Route route,
			final boolean isRecording) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				boolean trackSelected = route != null;
				updateOptionsButton(trackSelected);

				mapOverlay.setTrackDrawingEnabled(trackSelected);

				if (trackSelected) {
					busyPane.setVisibility(View.VISIBLE);

					zoomMapToBoundaries(route);

					mapOverlay.setShowEndMarker(!isRecording);
					busyPane.setVisibility(View.GONE);
				}
				mapView.invalidate();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		myLocation = menu.add(0, Constants.MENU_MY_LOCATION, 0,
				R.string.menu_map_view_my_location);
		myLocation.setIcon(android.R.drawable.ic_menu_mylocation);
		toggleLayers = menu.add(0, Constants.MENU_TOGGLE_LAYERS, 0,
				R.string.menu_map_view_satellite_mode);
		toggleLayers.setIcon(android.R.drawable.ic_menu_mapmode);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		toggleLayers
				.setTitle(mapView.isSatellite() ? R.string.menu_map_view_map_mode
						: R.string.menu_map_view_satellite_mode);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Constants.MENU_MY_LOCATION: {
			dataHub.forceUpdateLocation();
			keepMyLocationVisible = true;
			if (mapView.getZoomLevel() < 18) {
				mapView.getController().setZoom(18);
			}
			if (currentLocation != null) {
				showCurrentLocation();
			}
			return true;
		}
		case Constants.MENU_TOGGLE_LAYERS: {
			mapView.setSatellite(!mapView.isSatellite());
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) 
	{
		Log.d(TAG, "MyMapActivity: a view has been clicked");
		Toast.makeText(getApplicationContext(), "View clicked", Toast.LENGTH_SHORT).show();
	}

	/**
	 * We want the pointer to become visible again in case of the next location
	 * update:
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (keepMyLocationVisible
				&& event.getAction() == MotionEvent.ACTION_MOVE) {
			if (!locationIsVisible(currentLocation)) {
				keepMyLocationVisible = false;
			}
		}
		return false;
	}

	@Override
	public void onGPSProviderStateChange(MyGpsProviderState state) {
		final int messageId;
		final boolean isGpsDisabled;
		switch (state) {
		case DISABLED:
			messageId = R.string.gps_need_to_enable;
			isGpsDisabled = true;
			break;
		case NO_FIX:
		case BAD_FIX:
			messageId = R.string.gps_wait_for_fix;
			isGpsDisabled = false;
			break;
		case GOOD_FIX:
			// Nothing to show.
			messageId = -1;
			isGpsDisabled = false;
			break;
		default:
			throw new IllegalArgumentException("Unexpected state: " + state);
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (messageId != -1) {
					messageText.setText(messageId);
					messagePane.setVisibility(View.VISIBLE);

					if (isGpsDisabled) {
						// Give a warning about this state.
						Toast.makeText(MyMapActivity.this,
								R.string.gps_not_found, Toast.LENGTH_LONG)
								.show();

						// Make clicking take the user to the location settings.
						messagePane.setOnClickListener(MyMapActivity.this);
					} else {
						messagePane.setOnClickListener(null);
					}
				} else {
					messagePane.setVisibility(View.GONE);
				}

				screen.requestLayout();
			}
		});
	}

	@Override
	public void onGPSCurrentLocationChanged(Location location) {
		currentLocation = location;
		showCurrentLocation();
	}

	@Override
	public void onCurrentHeadingChanged(double heading) {
		synchronized (this) {
			if (mapOverlay.setHeading((float) heading)) {
				mapView.postInvalidate();
			}
		}
	}

	@Override
	public void clearRouteTrackPoints() {
		mapOverlay.clearRouteTrackPoints();
	}

	@Override
	public void onNewRouteTrackPoint(RouteTrackPoint routeTrackPoint) {
		if (LocationFilter.isValidLocation(routeTrackPoint.getLocation())) {
			// TODO: Optimize locking inside addWaypoint
			mapOverlay.addRouteTrackPoint(routeTrackPoint);
		}
	}

	@Override
	public void onNewRouteTrackPointsDone() {
		mapView.postInvalidate();
	}

	@Override
	public void clearRoutePoints() {
		mapOverlay.clearPoints();
	}

	@Override
	public void onNewRoutePoint(Location loc) {
		mapOverlay.addLocation(loc);
	}

	@Override
	public void onSegmentSplit() {
		mapOverlay.addSegmentSplit();
	}

	@Override
	public void onSampledOutRoutePoint(Location loc) {
		// We don't care.
	}

	@Override
	public void onNewRoutePointsDone() {
		mapView.postInvalidate();
	}

	@Override
	public boolean onUnitsChanged(boolean metric) {
		// We don't care.
		return false;
	}

	@Override
	public boolean onReportSpeedChanged(boolean reportSpeed) {
		// We don't care.
		return false;
	}

	@Override
	public void onNetworkProviderStateChange(MyNetworkProviderState state) {
		final int messageId;
		final boolean isGpsDisabled;
		switch (state) {
		case DISABLED:
			messageId = R.string.gps_need_to_enable;
			isGpsDisabled = true;
			break;
		case NO_FIX:
		case BAD_FIX:
			messageId = R.string.gps_wait_for_fix;
			isGpsDisabled = false;
			break;
		case GOOD_FIX:
			// Nothing to show.
			messageId = -1;
			isGpsDisabled = false;
			break;
		default:
			throw new IllegalArgumentException("Unexpected state: " + state);

		}
	}
}

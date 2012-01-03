package com.google.android.location.content;


import com.google.android.lib.content.RoutesColumns;
import com.google.android.lib.content.data.MyRouteProvider;
import com.google.android.lib.logs.MyLogClass;
import com.google.android.lib.services.IRouteRecordingService;
import com.google.android.location.content.menus.MenuManager;
import com.google.android.location.content.menus.NavigationControls;
import com.google.android.location.predict.RoutesPredictActivity;
import com.google.android.location.route.RouteManager;
import com.google.android.location.route.RouteMapDataListener;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MyMapActivity;
import com.google.android.maps.MyMapPredictionActivity;

import com.google.android.service.ServiceControl;
import com.google.android.service.ServiceManager;
import com.google.android.utilities.UriProfiler;

import android.app.Activity;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;

public class StartActivity extends TabActivity implements OnTouchListener {
	MyRouteProvider myRouteProvider;
	SharedPreferences preferences;
	MenuManager menuManager;
	SharedPreferences sharedPreference;
	boolean startNewRequested;
	NavigationControls navigationControls;
	ServiceConnectionManager managerConnection;
	IRouteRecordingService connectionService;
	private boolean startNewRecording = false;
	/**
	 * route manager of the current route
	 */
	RouteManager dataHub;
	ServiceConnectionManager serviceConnectionManager;
	private final Runnable changeTab = new Runnable() {
		public void run() {
			getTabHost().setCurrentTab(navigationControls.getCurrentIcons());
		}
	};
	private Runnable establishConnection = new Runnable() {
		@Override
		public void run() {
			synchronized (serviceConnectionManager) {
				IRouteRecordingService iService = serviceConnectionManager
						.getServiceIfBound();
				if (startNewRecording && iService != null) {
					Log.i(MyLogClass.TAG, "Start New TrackRequested");
					startNewRecording = false;
					startNewRouteRecording(iService);
				} else if (!startNewRecording) 
				{
					Log.d(MyLogClass.TAG, "Not yet recording...");
				}
			}
		}
	};

	private void startNewRouteRecording(IRouteRecordingService iService) {
		Log.i(MyLogClass.TAG, "Recording service...");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Resources res = getResources();
		dataHub = ((MyRouteAppManager)getApplication()).getRouteDataHub();
		final TabHost tabHost = getTabHost();
		tabHost.addTab(tabHost
				.newTabSpec("tab1")
				.setIndicator("Map",
						res.getDrawable(android.R.drawable.ic_menu_mapmode))
				.setContent(new Intent(this, MyMapActivity.class)));
		tabHost.addTab(tabHost
				.newTabSpec("tab2")
				.setIndicator("Map Predictions",
						res.getDrawable(android.R.drawable.ic_menu_mapmode))
				.setContent(new Intent(this, MyMapPredictionActivity.class)));
		tabHost.addTab(tabHost
				.newTabSpec("tab3")
				.setIndicator("Route Stats",
						res.getDrawable(R.drawable.menu_statistics))
				.setContent(new Intent(this, StatsActivity.class)));

		serviceConnectionManager = new ServiceConnectionManager();
		myRouteProvider = MyRouteProvider.Factory.getRouteProvider(this);
		tabHost.getTabWidget().setVisibility(View.GONE);
		preferences = getSharedPreferences(Constants.SETTINGS_NAME,
				Context.MODE_PRIVATE);
		RelativeLayout layout = new RelativeLayout(this);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		layout.setLayoutParams(params);
		navigationControls = new NavigationControls(this, layout,
				getResources().obtainTypedArray(R.array.left_icons),
				getResources().obtainTypedArray(R.array.right_icons), changeTab);
		navigationControls.show();
		tabHost.addView(layout);
		menuManager = new MenuManager(this);
		layout.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			navigationControls.show();
		}
		return false;
	}

	/**
	 * create the contextmenu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return menuManager.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menuManager.onPrepareOptionsMenu(menu,
				myRouteProvider.getLastRoute() != null, ServiceControl.isRecording(this,
								serviceConnectionManager.getServiceIfBound(),
								preferences), true);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * check if we have a connection and if the binder - connection
	 * service-activity is alive
	 * 
	 * @return
	 */
	public boolean bindingIsAlive() 
	{
		if (connectionService != null
				&& connectionService.asBinder().isBinderAlive()) {
			return true;
		}
		return false;
	}

	/**
	 * check the current status of the app and the service if it's recording
	 * already then start and bind the service
	 */
	@Override
	protected void onStart() {
		super.onStart();
		dataHub.startTracking();
		Log.i(MyLogClass.TAG, "StartActivity.dataHub:Load tracking manager");
		if (ServiceControl.isRecording(getApplicationContext(), null,
				preferences)) {
			serviceConnectionManager.startAndBindService();
		}
		Intent intent = getIntent();
	    String action = intent.getAction();
	    Uri data = intent.getData();
	    if ((Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action)) 
	        && RoutesColumns.CONTENT_ITEMTYPE.equals(intent.getType())
	        && UriProfiler.matchesContentUri(data, RoutesColumns.CONTENT_URI)) 
	    {
	      long routeId = ContentUris.parseId(data);
	      dataHub.loadRoute(routeId);
	    }
	}
    @Override
    protected void onStop() {
     	super.onStop();
     	dataHub.stopListening();

    }
	@Override
	protected void onPause() {
		Log.i(MyLogClass.TAG, "StartActivity : OnPause");
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(MyLogClass.TAG, "StartActivity : OnResume");
		serviceConnectionManager.bindServiceIfRunning();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		Log.d(MyLogClass.TAG, "StartActivity :  OnDestroy");
		serviceConnectionManager.unBindService();
		super.onDestroy();
	}

	/**
	 * the binder has lost itself so we must stop the connection
	 */
	private final DeathRecipient deathRecipient = new DeathRecipient() {
		@Override
		public void binderDied() {
			Log.d(MyLogClass.TAG, "Service died unexepectedly");
			setBoundService(null);
		}
	};

	/**
	 * the connection to the service itself
	 */

	private void setBoundService(IRouteRecordingService iRouteService) {
		connectionService = iRouteService;
		if (connectionService != null) {
			Log.i(MyLogClass.TAG, "Establishing the connection...");
			establishConnection.run();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return menuManager.onOptionsItemSelected(item) ? true : super
				.onOptionsItemSelected(item);
	}

	public void stopRecording() {
		// TODO Auto-generated method stub

	}

	public void startRecording() {
		synchronized (serviceConnectionManager) {
			startNewRecording = true;
			serviceConnectionManager.startAndBindService();
			establishConnection.run();
		}
	}

	public class ServiceConnectionManager {

		/**
		 * Unbind the service if there service wasn't bounded then an exception
		 * will be thrown
		 */
		public void unBindService() {
			Log.d(MyLogClass.TAG, "Unbinding the service");
			try {
				getApplicationContext().unbindService(conn);
			} catch (IllegalArgumentException e) {
				Toast.makeText(getApplicationContext(),
						"Error - we were not bound!", Toast.LENGTH_SHORT)
						.show();
			}
			setBoundService(null);
		}

		/**
		 * 
		 * @return the IRouteService.stub - which is implemented by the binder
		 *         in other way of speaking the binder to establish the
		 *         connection service activity
		 */
		public IRouteRecordingService getServiceIfBound() {
			if (bindingIsAlive()) {
				return connectionService;
			}
			return null;
		}

		private void bindServiceIfRunning() {
			bindService(false);
		}

		/**
		 * start and bind to the service
		 */
		private void startAndBindService() {
			bindService(true);
		}

		/**
		 * 
		 * @param start
		 *            - true to start and bind, false not
		 */
		private void bindService(boolean start) {
			if (connectionService != null) {
				return;
			}
			if (!start
					&& !ServiceControl
							.isServiceRunning(getApplicationContext())) {
				Log.i(MyLogClass.TAG, "Not Binding...");
				return;
			}
			if (start) {
				Log.i(MyLogClass.TAG, "Starting the service");
				Intent startIntent = new Intent(StartActivity.this,
						ServiceManager.class);
				getApplicationContext().startService(startIntent);
			}
			Log.i(MyLogClass.TAG, "Binding the service...");
			Intent bindStartedService = new Intent(StartActivity.this,
					ServiceManager.class);
			getApplicationContext().bindService(bindStartedService, conn,
					Context.BIND_AUTO_CREATE);
		}

		private final ServiceConnection conn = new ServiceConnection() 
		{
			@Override
			public void onServiceDisconnected(ComponentName name) 
			{
				Log.i(MyLogClass.TAG, "OnServiceDisconnected");
				setBoundService(null);
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) 
			{
				Log.i(MyLogClass.TAG, "OnServiceConnected");
				try 
				{
					service.linkToDeath(deathRecipient, 0);
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				setBoundService(IRouteRecordingService.Stub
						.asInterface(service));
			}
		};
	}
}
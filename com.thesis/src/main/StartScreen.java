package main;

import com.adapters.EndPointListAdapter;
import com.data.R;
import com.interfaces.OnRouteListener;
import com.services.LocalServiceTracking;
import com.services.LocationBinder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class StartScreen extends ListActivity{
	
	protected static final String TAG = "StartScreen";
	private Manager myApp;
	private EndPointListAdapter mAdapter;
	private Button addButton;

	private ToggleButton localTasksToggle;
	private boolean mIsBound;
	StateBinder mStateBinder;
	private static boolean toggleIsSelected;
	LocationManager myLocationManager;
	private TextView locationText;
	private Button editButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		if(mStateBinder==null)
		{
			mStateBinder = new StateBinder();
			mStateBinder.attachActivity(this);
		}
		
		doBindService();
		setupViews();
		myApp = (Manager) getApplication();
		mAdapter = new EndPointListAdapter(this, myApp.getCurrentEndPoints());
		setListAdapter(mAdapter);
	}


	
	private void setupViews() 
	{
		addButton = (Button) findViewById(R.id.add_button);
		editButton = (Button) findViewById(R.id.edit_button);
		locationText = (TextView) findViewById(R.id.location_text);
		localTasksToggle = (ToggleButton) findViewById(R.id.show_local_tasks_toggle);
		
		addButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(StartScreen.this,
						AddEndPointActivity.class);
				startActivity(myIntent);
			}
		});
		
		localTasksToggle.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				if(isMyServiceRunning())
				{
					if(!toggleIsSelected)
					{
						Toast.makeText(getApplicationContext(),"Service started...", Toast.LENGTH_SHORT).show();
						toggleIsSelected = true;
					    startLocalService(true);
					}
					else
					{
						Toast.makeText(getApplicationContext(),"Service stopped...", Toast.LENGTH_SHORT).show();
						toggleIsSelected=false;
						doUnbindService();
					}
				}
			}
		});
	}

    @Override
    protected void onStart() 
    {
      	super.onStart();
      	//startLocalService(isMyServiceRunning()); 
    }
	private void startLocalService(boolean myServiceRunning) 
	{
	   if(myServiceRunning)
	   {
		   mStateBinder.mBinder.showSomeFuckingData(mStateBinder);
	   }
	}

	public boolean isMyServiceRunning() 
	{
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
		{
			if ("com.services.LocalServiceTracking".equals(service.service.getClassName())) 
			{
				return true;
			}
		}
		return false;
	}
	void doBindService() 
	{
		Intent mIntent = new Intent(StartScreen.this, LocalServiceTracking.class);
		mIntent.putExtra(LocalServiceTracking.COUNT_VALUE, 2);
		getApplicationContext().bindService(mIntent, mStateBinder.mConnection , BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() 
	{
		if (mIsBound) 
		{
			getApplicationContext().unbindService(mStateBinder.mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		doUnbindService();
	}

	protected void editEndPointAddress() 
	{
		if (isMyServiceRunning() == true) 
		{
			doUnbindService();
			// stopService(localServiceIntent);
		}
	}
    public static class StateBinder implements OnRouteListener
    {
    	private static final String TAG = "StateBinder";
		private LocationBinder mBinder;
    	private StartScreen mCurrentActivity;
    	boolean isActivityAttached=false;
		@Override
		public void onCompleteRoute(String result) 
		{
			Log.e(TAG, "The result is:" +  result);
			Toast.makeText(mCurrentActivity,"Result from Route" + result  ,Toast.LENGTH_SHORT).show();	
		}
		void attachActivity(StartScreen m_hCurrentActivity)
		{
			this.mCurrentActivity = m_hCurrentActivity;
			isActivityAttached = true;
		}
		private ServiceConnection mConnection = new ServiceConnection() 
		{
			public void onServiceConnected(ComponentName className, IBinder service) 
			{
		 	    mBinder = (LocationBinder) service;
				if(isActivityAttached)
				{
					Toast.makeText(mCurrentActivity, R.string.local_service_connected,Toast.LENGTH_SHORT).show();	
				}
			}

			public void onServiceDisconnected(ComponentName className) 
			{
				mBinder = null;
				if(!isActivityAttached)
				{
					Toast.makeText(mCurrentActivity, R.string.local_service_disconnected, Toast.LENGTH_SHORT).show();
				}
			
			}
		};	
    }


	
}
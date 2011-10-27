package main;

import java.util.ArrayList;

import com.data.database.DatabaseHelper;
import com.generic.EndLocation;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.locationmanager.ProviderLocation;




import android.app.Application;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;

public class Manager extends Application implements com.generic.OnLoadCompleteListener
{
	private ArrayList<EndLocation> mCurrentTasks;
	ProviderLocation managerLocation;
	@Override
	public void onCreate() 
	{
		super.onCreate();
		managerLocation = new ProviderLocation(this);
	    if(mCurrentTasks==null)
	    {
	    	loadEndPoints();
	    }
	}
	private void loadEndPoints() 
	{
		   managerLocation =  new ProviderLocation(this);
		   managerLocation.locationLoading();
	}
	private void addLocation()
	{
		
	}
	private void deleteLocation()
	{
		
	}
	private void editLocation()
	{
		
	}

	@Override
	public void onLoadComplete() 
	{
			
	}
}

package com.services;

import com.interfaces.OnRouteListener;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Binder;
import android.util.Log;
import android.widget.Toast;

public class LocationBinder extends Binder 
{
	
	public static final String TAG = "LocalBinder";
	Context context;
    void onCreate(Context context)
    {
    	this.context = context;
    	Toast.makeText(context, "We are in the binder",Toast.LENGTH_SHORT).show();
    }
    public void showSomeFuckingData(OnRouteListener mRouteListenr)
    {
    	new AsynTask(mRouteListenr).execute();
    }
    
    public class AsynTask extends AsyncTask<String, String, String> {

    	OnRouteListener mOnRouteListener;
    	public AsynTask(OnRouteListener m_hOnRouteListener)
    	{
    		this.mOnRouteListener = m_hOnRouteListener;
    	}
    	@Override
    	protected String doInBackground(String... arg0) 
    	{
    		try 
    		{
    			Thread.sleep(10000);
    			Log.i(TAG, "doInBackGround:");
    		} catch (InterruptedException e) 
    		{
    			e.printStackTrace();
    		}
    		return "Andrei";
    	}
    	@Override
    	protected void onPostExecute(String result) 
    	{
    	    mOnRouteListener.onCompleteRoute(result);
    	}

    }
    
}

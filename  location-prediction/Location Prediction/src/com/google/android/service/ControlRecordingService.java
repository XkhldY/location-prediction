package com.google.android.service;

import static com.google.android.location.content.Constants.TAG;

import com.google.android.lib.services.IRouteRecordingService;
import com.google.android.location.content.R;



import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * A service to control starting and stopping of a recording. This service,
 * through the AndroidManifest.xml, is configured to only allow components of
 * the same application to invoke it. Thus this service can be used my MyTracks
 * app widget, {@link RouteWidgetProvider}, but not by other applications. This
 * application delegates starting and stopping a recording to
 * {@link ServiceManager} using RPC calls.
 * For the moment RouteWidgetProvider is not configure or created.
 *
 * @author Andrei
 */
public class ControlRecordingService extends IntentService implements ServiceConnection {

  private IRouteRecordingService routeRecordingService;
  private boolean connected = false;

  public ControlRecordingService() {
    super(ControlRecordingService.class.getSimpleName());
  }

  @Override
  public void onCreate() {
    super.onCreate();

    Intent newIntent = new Intent(this, ServiceManager.class);
    startService(newIntent);
    bindService(newIntent, this, 0);
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    routeRecordingService = IRouteRecordingService.Stub.asInterface(service);
    notifyConnected();
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    connected = false;
  }

  /**
   * Notifies all threads that connection to {@link TrackRecordingService} is
   * available.
   */
  private synchronized void notifyConnected() {
    connected = true;
    notifyAll();
  }

  /**
   * Waits until the connection to {@link TrackRecordingService} is available.
   */
  private synchronized void waitConnected() {
    while (!connected) 
    {
      try 
      {
        wait();
      } 
      catch (InterruptedException e) 
      {
        // can safely ignore
      }
    }
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    waitConnected();
    String action = intent.getAction();
    if (action != null) {
      try 
      {
        if (action.equals(getString(R.string.track_action_start))) 
        {
          routeRecordingService.startNewRouteId();
        } 
        else 
          if (action.equals(getString(R.string.track_action_end))) 
          {
            routeRecordingService.endCurrentRoute();
          }
      } 
      catch (RemoteException e) 
      {
        Log.d(TAG, "ControlRecordingService onHandleIntent RemoteException", e);
      }
    }
    unbindService(this);
    connected = false;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (connected) {
      unbindService(this);
      connected = false;
    }
  }
}

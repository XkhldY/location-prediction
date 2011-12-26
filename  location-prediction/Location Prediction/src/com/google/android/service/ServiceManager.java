package com.google.android.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.lib.content.CreateTrack;
import com.google.android.lib.content.data.MyRouteProvider;
import com.google.android.lib.logs.MyLogClass;
import com.google.android.lib.services.IRouteRecordingService;
import com.google.android.utilities.LocationListenerPolicy;
import com.google.android.utilities.MyLocationListenerPolicy;

public class ServiceManager extends Service {
	private ServiceBinder mBinder = new ServiceBinder(this);
    private boolean isRecording = false;
    private long recordingRouteId;
	private boolean isAlreadyStarted = false;
	private NotificationManager notificationManager;
	@Override
	public void onCreate() {
		Log.d(MyLogClass.TAG, "Service:Oncreate");
		MyRouteProvider myRouteProvider = MyRouteProvider.Factory.getRouteProvider(this);
		notificationManager =
		        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	public long startNewRoute() {
	   Log.d(MyLogClass.TAG, "Service:Starts new Route");
	   if(isAlreadyStarted )
	   {
		   return -1L;
	   }
		return 0;
	}
	public boolean isRecording() 
	{
		return isRecording;
	}
	public static class ServiceBinder extends IRouteRecordingService.Stub {
		
		DeathRecipient deathRecipient;
        ServiceManager service;
		public ServiceBinder(ServiceManager m_hRouteRecordingService) {
			this.service = m_hRouteRecordingService;
		}

		@Override
		public void linkToDeath(DeathRecipient recipient, int flags) {
			deathRecipient = recipient;
		}

		@Override
		public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
			if (!isBinderAlive()) {
				return false;
			}

			deathRecipient = null;
			return true;
		}

		@Override
		public boolean isRecording() throws RemoteException {
			return service.isRecording();
		}

		@Override
		public long insertEndPointWithId(CreateTrack track)
				throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}
          @Override
        public boolean isBinderAlive() {
        	return service!=null;
        }
		@Override
		public long startNewRouteId() throws RemoteException {
			return service.startNewRoute();
		}

		@Override
		public long getRecordingRouteId() throws RemoteException {
			return service.recordingRouteId;
		}

		@Override
		public void recordLocation() throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void calculateStatistics() throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean existEndPointAtId() throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void endCurrentRoute() throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public byte[] getSensorData() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getSensorState() throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

	}
	public void setMinRecordingDistance(int minRecordingDistance) {
		// TODO Auto-generated method stub
		
	}

	public void setAutoResumeTrackTimeout(int int1) {
		// TODO Auto-generated method stub
		
	}

	public void setSplitFrequency(int int1) {
		// TODO Auto-generated method stub
		
	}

	public void setMetricUnits(boolean boolean1) {
		// TODO Auto-generated method stub
		
	}

	public void setRecordingTrackId(long recordingTrackId) {
		// TODO Auto-generated method stub
		
	}

	public void setRecordingRouteId(long recordingTrackId) {
		// TODO Auto-generated method stub
		
	}

	public void setMinRequiredAccuracy(int int1) {
		// TODO Auto-generated method stub
		
	}

	public void setAnnouncementFrequency(int int1) {
		// TODO Auto-generated method stub
		
	}

	public void setMaxRecordingDistance(int int1) {
		// TODO Auto-generated method stub
		
	}

	public void setLocationListenerPolicy(
			LocationListenerPolicy myLocationListenerPolicy) {
		// TODO Auto-generated method stub
		
	}




	
}

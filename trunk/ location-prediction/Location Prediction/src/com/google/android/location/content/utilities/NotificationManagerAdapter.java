package com.google.android.location.content.utilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.util.Log;
import static com.google.android.location.content.Constants.*;

public class NotificationManagerAdapter implements INotificationAdapter 
{

	@Override
	public void startForeground(Service service,
			NotificationManager notificationManager, int id,
			Notification notification) 
	{
		setServiceForeground(service, true);
		notificationManager.notify(id, notification);
	}

	@Override
	public void stopForeground(Service service,
			NotificationManager notificationManager, int id) 
	{
		if (id != -1) 
		{
			notificationManager.cancel(id);
		}
	}

	private void setServiceForeground(Service service, boolean foreground) 
	{
		// setForeground has been completely removed in API level 11, so we use
		// reflection.
		try 
		{
			Method setForegroundMethod = Service.class.getMethod(
					"setForeground", boolean.class);
			setForegroundMethod.invoke(service, foreground);
		} 
		catch (SecurityException e) 
		{
			Log.e(TAG, "Unable to set service foreground state", e);
		} catch (NoSuchMethodException e) 
		{
			Log.e(TAG, "Unable to set service foreground state", e);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Unable to set service foreground state", e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "Unable to set service foreground state", e);
		} catch (InvocationTargetException e) {
			Log.e(TAG, "Unable to set service foreground state", e);
		}
	}
}

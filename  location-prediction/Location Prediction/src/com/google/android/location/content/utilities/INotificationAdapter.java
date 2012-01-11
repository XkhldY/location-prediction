package com.google.android.location.content.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;



public interface INotificationAdapter 
{
	 /**
	   * Puts the specified service into foreground.
	   * 
	   * @param service the service to be put in foreground.
	   * @param notificationManager the notification manager used to post the given
	   *        notification.
	   * @param id the ID of the notification, unique within the application.
	   * @param notification the notification to post.
	   */
	  void startForeground(Service service, NotificationManager notificationManager,
	      int id, Notification notification);
	  
	  /**
	   * Puts the given service into background.
	   * 
	   * @param service the service to put into background.
	   * @param notificationManager the notification manager to user when removing
	   *        notifications. 
	   * @param id the ID of the notification to be remove, or -1 if the
	   *        notification shouldn't be removed.
	   */
	  void stopForeground(Service service, NotificationManager notificationManager,
	      int id);

}

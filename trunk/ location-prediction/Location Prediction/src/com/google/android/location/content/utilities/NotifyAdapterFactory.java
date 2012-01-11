package com.google.android.location.content.utilities;

public class NotifyAdapterFactory 
{
	private static NotifyAdapterFactory notifyAdapter;
	NotificationManagerAdapter notifyManagerAdapter;
	public static NotifyAdapterFactory getInstance()
    {
	   if(notifyAdapter==null)
	   {
		   notifyAdapter =  new NotifyAdapterFactory();
	   }
	   return notifyAdapter;
	   
    }
	public NotifyAdapterFactory()
	{
		notifyManagerAdapter =  new NotificationManagerAdapter();
	}
	public NotificationManagerAdapter getNotifyManagerAdapter()
	{
		return notifyManagerAdapter;
	}
}

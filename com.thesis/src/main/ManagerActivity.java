package main;

import android.app.Activity;

public class ManagerActivity extends Activity 
{
   public ManagerActivity()
   {
	   
   }
   protected Manager getManagerApplication()
   {
	   Manager  myApplication = (Manager)getApplication();
	   return myApplication;
   }
}

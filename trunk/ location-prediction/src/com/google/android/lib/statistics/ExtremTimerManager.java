package com.google.android.lib.statistics;

import android.util.Log;
import com.google.android.lib.logs.MyLogClass;
public class ExtremTimerManager 
{
   private double min_time;
   private double max_time;
   private double delay;
   public ExtremTimerManager()
   {
	   resetTime();
   }

   private void resetTime() 
   {
	   Log.i(MyLogClass.TAG, "reseting the time");
	   min_time = -1;
	   max_time = -1;
	   delay = -1;
   }
   public double getMin()
   {
	   return min_time;
   }
   public double getMax()
   {
	   return max_time;
   }
   public double getDelay()
   {
	   return delay;
   }
   public void setDelay(long delay)
   {
	   this.delay = delay;
   }
   public void setMinTime(long min_time)
   {
	   this.min_time = min_time;
   }
   public void setMaxTime(long max_time)
   {
	   this.max_time = max_time;
   }
   public void setMinMax(double minInterval, double maxInterval)
   {
	   this.min_time = minInterval;
	   this.max_time = maxInterval;
   }
   public boolean checkInterval()
   {
	   return (min_time!=-1 && max_time!=-1);
   }
   
}

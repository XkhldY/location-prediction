package com.google.android.location.tasks;

import android.util.Log;


import com.google.android.utilities.LocationListenerPolicy;
import com.google.android.utilities.UnitConversions;

import static com.google.android.location.content.Constants.*;

/**
 * Execute a task on a time or distance schedule.
 *
 * @author Andrei
 */
public class PeriodicTaskExecutor {

  /**
   * The frequency of the task.
   * A value greater than zero is a frequency in time.
   * A value less than zero is considered a frequency in distance.
   */
  private int taskFrequency = 0;

  /**
   * The next distance when the task should execute.
   */
  private double nextTaskDistance = 0;

  /**
   * Time based executor.
   */
  private TimerTaskExecutor timerExecutor = null;
  /**
   * for now we use only metric units - imperial units later on
   */
  private boolean metricUnits;
  
  LocationListenerPolicy locationListenerPolicy;
  private final PeriodicTaskFactory factory;
  
  /**
   *  task that will update the mapview and the database
   */
  private PeriodicTask task;
  private boolean isRecording;

  public PeriodicTaskExecutor(PeriodicTaskFactory factory, boolean isRecording, 
		                      LocationListenerPolicy locationListenerPPolicy) 
  {
  
    this.factory = factory;
    this.isRecording = isRecording;
    this.locationListenerPolicy = locationListenerPPolicy;
  }
  
  /**
   * Restores the manager.
   */
  public void restore(boolean isRecording) {
    // TODO: Decouple service from this class once and forever.
    if (!isRecording) 
    {
      return;
    }

    if (!isTimeFrequency()) 
    {
      if (timerExecutor != null) 
      {
        timerExecutor.shutdown();
        timerExecutor = null;
      }
    }
    if (taskFrequency == 0) 
    {
      return;
    }

    // Try to make the task.
    //PeriodicTask creation
    //implemented by SplitTask
    task = factory.create();
    // Returning null is ok.
    if (task == null) 
    {
      return;
    }
    //does nothing...arranging steps in the PeriodicTaskExecutor
    task.start();

    if (isTimeFrequency()) 
    {
      if (timerExecutor == null) 
      {
        timerExecutor = new TimerTaskExecutor(task);
      }
      timerExecutor.scheduleTask(taskFrequency * 60000L);
    } 
    else 
    {
      // For distance based splits.
      calculateNextTaskDistance();
    }
  }

  /**
   * Shuts down the manager.
   */
  public void shutdown() {
    if (task != null) {
      task.shutdown();
      task = null;
    }
    if (timerExecutor != null) {
      timerExecutor.shutdown();
      timerExecutor = null;
    }
  }

  /**
   * Calculates the next distance when the task should execute.
   */
  void calculateNextTaskDistance() {
//    // TODO: Decouple service from this class once and forever.
//    if (!service.isRecording() || task == null) {
//      return;
//    }
//
//    if (!isDistanceFrequency()) {
//      nextTaskDistance = Double.MAX_VALUE;
//      Log.d(TAG, "SplitManager: Distance splits disabled.");
//      return;
//    }
//
//    double distance = service.getTripStatistics().getTotalDistance() / 1000;
//    if (!metricUnits) 
//    {
//      distance *= UnitConversions.KM_TO_MI;
//    }
//    // The index will be negative since the frequency is negative.
//    int index = (int) (distance / taskFrequency);
//    index -= 1;
//    nextTaskDistance = taskFrequency * index;
//    Log.d(TAG, "SplitManager: Next split distance: " + nextTaskDistance);
  }

  /**
   * Updates executer with new trip statistics.
   */
  public void update() 
  {
//    if (!isDistanceFrequency() || task == null) 
//    {
//      return;
//    }
//    // Convert the distance in meters to km or mi.
//    double distance = service.getTripStatistics().getTotalDistance() / 1000.0;
//    if (!metricUnits)
//    {
//      distance *= UnitConversions.KM_TO_MI;
//    }
//
//    if (distance > nextTaskDistance) 
//    {
//      task.run(service);
//      calculateNextTaskDistance();
//    }
  }

  private boolean isTimeFrequency() 
  {
    return taskFrequency > 0;
  }

  private boolean isDistanceFrequency() 
  {
    return taskFrequency < 0;
  }

  /**
   * Sets the task frequency.
   * &lt; 0 Use the absolute value as a distance in the current measurement km
   *  or mi
   *   0 Turn off the task
   * &gt; 0 Use the value as a time in minutes
   * @param taskFrequency The frequency in time or distance
   */
  public void setTaskFrequency(int taskFrequency) 
  {
    Log.d(TAG, "setTaskFrequency: taskFrequency = " + taskFrequency);
    this.taskFrequency = taskFrequency;
    restore(true);
  }

  public void setMetricUnits(boolean metricUnits) 
  {
    this.metricUnits = metricUnits;
    calculateNextTaskDistance();
  }

  double getNextTaskDistance() 
  {
    return nextTaskDistance;
  }
}

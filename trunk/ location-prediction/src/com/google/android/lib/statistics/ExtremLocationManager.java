package com.google.android.lib.statistics;

public class ExtremLocationManager 
{
  private double min;
  private double max;
  
  public ExtremLocationManager()
  {
	 resetValues();
  }
  public void resetValues()
  {
	  min  = Double.NEGATIVE_INFINITY;
	  max = Double.POSITIVE_INFINITY;
  }
  public boolean updateValue(double value)
  {
	  boolean hasChanged = false;
	  if(value < min)
	  {
		  min = value;
		  hasChanged = true;
	  }
	  if(value > max)
	  {
		  max = value;
		  hasChanged = true;
	  }
	  return hasChanged;
  }
  public void setMinMax(double min , double max)
  {
	  this.max = max;
	  this.min = min;
  }
  public double getMax()
  {
	  return max;
  }
  
  public double getMin()
  {
	  return min;
  }
  public void setMax(double max)
  {
	  this.max = max;
  }
  public void setMin(double min)
  {
	  this.min = min;
  }
  public boolean hasMin()
  {
	  return (min!=Double.NEGATIVE_INFINITY);
  }
  public boolean hasMax()
  {
	  return (max!=Double.POSITIVE_INFINITY);
  }
  @Override
  public String toString() {
    return "Min: " + min + " Max: " + max;
  }
  
}

package com.interfaces;

public interface ISequence 
{
  public void setStartPoint(String point);
  public void setEndPoint(String point);
  public String getStartPoint();
  public String getEndPoint();
  public void updatePoint(int x,int y);
  public void removePoint(int x, int y);
  public void updateRoute();
}

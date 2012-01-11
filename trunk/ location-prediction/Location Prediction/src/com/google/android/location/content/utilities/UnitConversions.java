package com.google.android.location.content.utilities;

public abstract class UnitConversions 
{

	public static final double KM_TO_MI = 0.621371192;
	public static final double M_TO_FT = 3.2808399;
	public static final double MI_TO_M = 1609.344;
	public static final double MI_TO_FEET = 5280.0;
	public static final double KMH_TO_MPH = 1000 * M_TO_FT / MI_TO_FEET;
	public static final double TO_RADIANS = Math.PI / 180.0;
	public static final double MPH_TO_KMH = 1.609344;

	protected UnitConversions() 
	{
	}
}
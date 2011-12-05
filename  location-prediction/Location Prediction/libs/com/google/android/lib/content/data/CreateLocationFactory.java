package com.google.android.lib.content.data;

import android.location.Location;

public interface CreateLocationFactory 
{
	Location createGPSLocation();
	Location createNetworkLocation();
}

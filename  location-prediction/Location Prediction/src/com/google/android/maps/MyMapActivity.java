package com.google.android.maps;

import com.google.android.location.content.R;

import android.os.Bundle;
import android.util.Log;

public class MyMapActivity extends MapActivity {

   private static final String TAG = "MyMapActivity";
@Override
   protected void onCreate(Bundle bundle) 
   {
	   Log.d(TAG , "MapActivity.onCreate");
	    super.onCreate(bundle);
	    setContentView(R.layout.mytracks_layout);
    }
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}

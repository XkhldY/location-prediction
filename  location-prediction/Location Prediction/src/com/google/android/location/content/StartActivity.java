package com.google.android.location.content;

import com.google.android.lib.content.data.MyRouteProvider;
import com.google.android.location.predict.RoutesPredictActivity;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MyMapActivity;
import com.google.android.maps.MyMapPredictionActivity;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TabHost;

public class StartActivity extends TabActivity implements OnTouchListener 
{
	MyRouteProvider myRouteProvider;
   
    MenuManager menuManager;
    SharedPreferences sharedPreference;
    boolean startNewRequested;
    NavigationControls navigationControls;
    private final Runnable changeTab = new Runnable() 
    {
      public void run() 
      {
        getTabHost().setCurrentTab(navigationControls.getCurrentIcons());
      }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Resources res = getResources();
        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec("tab1")
            .setIndicator("Map", res.getDrawable(
                android.R.drawable.ic_menu_mapmode))
            .setContent(new Intent(this, MyMapActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("tab2").
        		setIndicator("Map Predictions",res.getDrawable(android.R.drawable.ic_menu_mapmode))
        		.setContent(new Intent(this,MyMapPredictionActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("tab3").
        		setIndicator("Route Stats",res.getDrawable(R.drawable.menu_statistics))
        		.setContent(new Intent(this,ChartActivity.class)));       
        // Hide the tab widget itself. We'll use overlayed prev/next buttons to
        // switch between the tabs:
        tabHost.getTabWidget().setVisibility(View.GONE);

        RelativeLayout layout = new RelativeLayout(this);
        LayoutParams params =
            new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        layout.setLayoutParams(params);
         navigationControls = new NavigationControls(this, layout, 
        		                                     getResources().obtainTypedArray(R.array.left_icons), 
        		                                     getResources().obtainTypedArray(R.array.right_icons), 
        		                                     changeTab);
        //navControls = new NavControls(this, layout, getResources().obtainTypedArray(R.array.left_icons), getResources().obtainTypedArray(R.array.right_icons), changeTab);
        //navControls.show();
         navigationControls.show();
        tabHost.addView(layout);
        layout.setOnTouchListener(this);
    }
	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{	
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			navigationControls.show();
		    }
		    return false;
	}
}
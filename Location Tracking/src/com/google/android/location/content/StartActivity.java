package com.google.android.location.content;

import com.google.android.location.predict.RoutesPredictActivity;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MyMapActivity;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TabHost;

public class StartActivity extends TabActivity 
{
    NavControls navControls;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Resources res = getResources();
        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec("tab1")
            .setIndicator("Map", res.getDrawable(
                android.R.drawable.ic_menu_mapmode))
            .setContent(new Intent(this, MyMapActivity.class)));
        

        // Hide the tab widget itself. We'll use overlayed prev/next buttons to
        // switch between the tabs:
        tabHost.getTabWidget().setVisibility(View.GONE);

        RelativeLayout layout = new RelativeLayout(this);
        LayoutParams params =
            new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        layout.setLayoutParams(params);
        

    }
}
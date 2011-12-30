package com.google.android.location.content.menus;





import com.google.android.location.content.R;
import com.google.android.location.content.SettingsActivity;
import com.google.android.location.content.StartActivity;
import com.google.android.location.content.R.id;
import com.google.android.location.content.R.menu;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Manage the application menus.
 *
 * @author Andrei
 */
public class MenuManager {

  private final StartActivity activity;

  public MenuManager(StartActivity activity) {
    this.activity = activity;
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    activity.getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  public void onPrepareOptionsMenu(Menu menu, boolean hasRecorded,
      boolean isRecording, boolean hasSelectedTrack) {
    menu.findItem(R.id.menu_markers)
        .setEnabled(hasRecorded && hasSelectedTrack);
    menu.findItem(R.id.menu_record_route)
        .setEnabled(!isRecording)
        .setVisible(!isRecording);
    menu.findItem(R.id.menu_stop_recording)
        .setEnabled(isRecording)
        .setVisible(isRecording);
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_record_route: {
        activity.startRecording();
        return true;
      }
      case R.id.menu_stop_recording: {
        activity.stopRecording();
        return true;
      }
      case R.id.menu_settings:
      {
    	return startActivity(SettingsActivity.class);
      }
      case R.id.menu_aggregated_statistics:
      {
    	  Toast.makeText(this.activity.getApplicationContext(), "Nothing for now", Toast.LENGTH_LONG).show();
    	  return true;
      }
    }
    return false;
  }

  private boolean startActivity(Class<? extends Activity> activityClass) {
    activity.startActivity(new Intent(activity, activityClass));
    return true;
  }
}

package com.google.android.location.content;



import com.google.android.utilities.UnitConversions;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * An activity that let's the user see and edit the settings.
 * 
 * @author Andrei
 */
public class SettingsActivity extends PreferenceActivity {

  // Value when the task frequency is off.
  private static final String TASK_FREQUENCY_OFF = "0";
  
  // Value when the recording interval is 'Adapt battery life'.
  private static final String RECORDING_INTERVAL_ADAPT_BATTERY_LIFE = "-2";
  
  // Value when the recording interval is 'Adapt accuracy'.
  private static final String RECORDING_INTERVAL_ADAPT_ACCURACY = "-1";
  
  // Value for the recommended recording interval.
  private static final String RECORDING_INTERVAL_RECOMMENDED = "0";

  // Value when the auto resume timeout is never.
  private static final String AUTO_RESUME_TIMEOUT_NEVER = "0";
  
  // Value when the auto resume timeout is always.
  private static final String AUTO_RESUME_TIMEOUT_ALWAYS = "-1";
  
  // Value for the recommended recording distance.
  private static final String RECORDING_DISTANCE_RECOMMENDED = "5";

  // Value for the recommended track distance.  
  private static final String TRACK_DISTANCE_RECOMMENDED = "200";

  // Value for the recommended GPS accuracy.
  private static final String GPS_ACCURACY_RECOMMENDED = "200";
  
  // Value when the GPS accuracy is for excellent GPS signal.
  private static final String GPS_ACCURACY_EXCELLENT = "10";
  
  // Value when the GPS accuracy is for poor GPS signal.
  private static final String GPS_ACCURACY_POOR = "5000";

  

  
  /** Called when the activity is first created. */
  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);


    // Tell it where to read/write preferences
    PreferenceManager preferenceManager = getPreferenceManager();
    preferenceManager.setSharedPreferencesName(Constants.SETTINGS_NAME);
    preferenceManager.setSharedPreferencesMode(0);

    
    // Load the preferences to be displayed
    addPreferencesFromResource(R.xml.preferences);

  
    
    setRecordingIntervalOptions();
    setAutoResumeTimeoutOptions();

    // Hook up switching of displayed list entries between metric and imperial
    // units
    CheckBoxPreference metricUnitsPreference = (CheckBoxPreference) findPreference(getString(R.string.metric_units_key));
    
    metricUnitsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
        {
          @Override
          public boolean onPreferenceChange(Preference preference,
              Object newValue) {
            boolean isMetric = (Boolean) newValue;
            updateDisplayOptions(isMetric);
            return true;
          }
        });
    updateDisplayOptions(metricUnitsPreference.isChecked());

   // customizeTrackColorModePreferences();
 
    
    
  }
  
  /**
   * Sets the display options for the 'Time between points' option.
   */
  private void setRecordingIntervalOptions() 
  {
    String[] values = getResources().getStringArray(R.array.recording_interval_values);
    String[] options = new String[values.length];
    for (int i = 0; i < values.length; i++) 
    {
      if (values[i].equals(RECORDING_INTERVAL_ADAPT_BATTERY_LIFE)) 
      {
        options[i] = getString(R.string.value_adapt_battery_life);
      } 
      else if (values[i].equals(RECORDING_INTERVAL_ADAPT_ACCURACY)) 
      {
        options[i] = getString(R.string.value_adapt_accuracy);
      } 
      else if (values[i].equals(RECORDING_INTERVAL_RECOMMENDED)) 
      {
        options[i] = getString(R.string.value_smallest_recommended);
      } 
      else 
      {
        int value = Integer.parseInt(values[i]);
        String format;
        if (value < 60) 
        {
          format = getString(R.string.value_integer_second);
        }
        else 
        {
          value = value / 60;
          format = getString(R.string.value_integer_minute);
        }
        options[i] = String.format(format, value);
      }
    }
    ListPreference list = (ListPreference) findPreference(
        getString(R.string.min_recording_interval_key));
    list.setEntries(options);
  }

  /**
   * Sets the display options for the 'Auto-resume timeout' option.
   */
  private void setAutoResumeTimeoutOptions() {
    String[] values = getResources().getStringArray(R.array.recording_auto_resume_timeout_values);
    String[] options = new String[values.length];
    for (int i = 0; i < values.length; i++) 
    {
      if (values[i].equals(AUTO_RESUME_TIMEOUT_NEVER)) 
      {
        options[i] = getString(R.string.value_never);
      } 
      else if (values[i].equals(AUTO_RESUME_TIMEOUT_ALWAYS)) 
      {
        options[i] = getString(R.string.value_always);
      } 
      else 
      {
        int value = Integer.parseInt(values[i]);
        String format = getString(R.string.value_integer_minute);
        options[i] = String.format(format, value);
      }
    }
    ListPreference list = (ListPreference) findPreference(getString(R.string.auto_resume_route_timeout_key));
    list.setEntries(options);
  }

  @Override
  protected void onResume() {
    super.onResume();
    
    

    // If recording, disable backup/restore/reset
    // (we don't want to get to inconsistent states)
    //boolean recording = preferences.getLong(getString(R.string.recording_track_key), -1) != -1;
    
  }

  @Override
  protected void onDestroy() 
  {
    super.onPause();
  }
  
  /**
   * Updates display options that depends on the preferred distance units, metric or imperial.
   *
   * @param isMetric true to use metric units, false to use imperial
   */
  private void updateDisplayOptions(boolean isMetric) 
  {
    setTaskOptions(isMetric, R.string.route_update_idletime_key);
    setRecordingDistanceOptions(isMetric, R.string.min_recording_distance_key);
    setTrackDistanceOptions(isMetric, R.string.max_recording_distance_key);
    setGpsAccuracyOptions(isMetric, R.string.min_required_accuracy_key);
  }

  /**
   * Sets the display options for a periodic task.
   */
  private void setTaskOptions(boolean isMetric, int listId)
  {
    String[] values = getResources().getStringArray(R.array.recording_task_frequency_values);
    String[] options = new String[values.length];
    for (int i = 0; i < values.length; i++) 
    {
      if (values[i].equals(TASK_FREQUENCY_OFF)) 
      {
        options[i] = getString(R.string.value_off);
      } 
      else if (values[i].startsWith("-")) 
      {
        int value = Integer.parseInt(values[i].substring(1));
        int stringId = isMetric ? R.string.value_integer_kilometer : R.string.value_integer_mile;
        String format = getString(stringId);
        options[i] = String.format(format, value);
      } 
      else 
      {
        int value = Integer.parseInt(values[i]);
        String format = getString(R.string.value_integer_minute);
        options[i] = String.format(format, value);
      }
    }

    ListPreference list = (ListPreference) findPreference(getString(listId));
    list.setEntries(options);
  }
  
  /**
   * Sets the display options for 'Distance between points' option.
   */
  private void setRecordingDistanceOptions(boolean isMetric, int listId) 
  {
    String[] values = getResources().getStringArray(R.array.recording_distance_values);
    String[] options = new String[values.length];
    for (int i = 0; i < values.length; i++) 
    {
      int value = Integer.parseInt(values[i]);
      if (!isMetric) 
      {
        value = (int) (value * UnitConversions.M_TO_FT);
      }
      String format;
      if (values[i].equals(RECORDING_DISTANCE_RECOMMENDED)) 
      {
        int stringId = isMetric ? R.string.value_integer_meter_recommended
            : R.string.value_integer_feet_recommended;
        format = getString(stringId);
      } 
      else 
      {
        int stringId = isMetric ? R.string.value_integer_meter : R.string.value_integer_feet;
        format = getString(stringId);
      }
      options[i] = String.format(format, value);
    }

    ListPreference list = (ListPreference) findPreference(getString(listId));
    list.setEntries(options);
  }
  
  /**
   * Sets the display options for 'Distance between Tracks'.
   */
  private void setTrackDistanceOptions(boolean isMetric, int listId) 
  {
    String[] values = getResources().getStringArray(R.array.recording_track_distance_values);
    String[] options = new String[values.length];
    for (int i = 0; i < values.length; i++) 
    {
      int value = Integer.parseInt(values[i]);
      String format;
      if (isMetric) 
      {
        int stringId = values[i].equals(TRACK_DISTANCE_RECOMMENDED) 
            ? R.string.value_integer_meter_recommended : R.string.value_integer_meter;
        format = getString(stringId);
        options[i] = String.format(format, value);
      } 
      else 
      {
        value = (int) (value * UnitConversions.M_TO_FT);
        if (value < 2000) 
        {
          int stringId = values[i].equals(TRACK_DISTANCE_RECOMMENDED) 
              ? R.string.value_integer_feet_recommended : R.string.value_integer_feet;
          format = getString(stringId);
          options[i] = String.format(format, value);
        } 
        else 
        {
          double mile = value / UnitConversions.MI_TO_FEET;
          format = getString(R.string.value_float_mile);
          options[i] = String.format(format, mile);
        }
      }
    }

    ListPreference list = (ListPreference) findPreference(getString(listId));
    list.setEntries(options);
  }
  
  /**
   * Sets the display options for 'GPS accuracy'.
   */
  private void setGpsAccuracyOptions(boolean isMetric, int listId) 
  {
    String[] values = getResources().getStringArray(R.array.recording_gps_accuracy_values);
    String[] options = new String[values.length];
    for (int i = 0; i < values.length; i++) 
    {
      int value = Integer.parseInt(values[i]);
      String format;
      if (isMetric) 
      {
        if (values[i].equals(GPS_ACCURACY_RECOMMENDED)) 
        {
          format = getString(R.string.value_integer_meter_recommended);
        } 
        else if (values[i].equals(GPS_ACCURACY_EXCELLENT)) 
        {
          format = getString(R.string.value_integer_meter_excellent_gps);
        } 
        else if (values[i].equals(GPS_ACCURACY_POOR)) {
          format = getString(R.string.value_integer_meter_poor_gps);
        } 
        else 
        {
          format = getString(R.string.value_integer_meter);
        }
        options[i] = String.format(format, value);
      } 
      else 
      {
        value = (int) (value * UnitConversions.M_TO_FT);
        if (value < 2000) 
        {
          if (values[i].equals(GPS_ACCURACY_RECOMMENDED)) {
            format = getString(R.string.value_integer_feet_recommended);
          } 
          else if (values[i].equals(GPS_ACCURACY_EXCELLENT)) 
          {
            format = getString(R.string.value_integer_feet_excellent_gps);
          } 
          else 
          {
            format = getString(R.string.value_integer_feet);
          }
          options[i] = String.format(format, value);
        } 
        else 
        {
          double mile = value / UnitConversions.MI_TO_FEET;
          if (values[i].equals(GPS_ACCURACY_POOR)) 
          {
            format = getString(R.string.value_float_mile_poor_gps);
          } 
          else {
            format = getString(R.string.value_float_mile);
          }
          options[i] = String.format(format, mile);    
        }
      }
    }
    ListPreference list = (ListPreference) findPreference(getString(listId));
    list.setEntries(options);
  }
}

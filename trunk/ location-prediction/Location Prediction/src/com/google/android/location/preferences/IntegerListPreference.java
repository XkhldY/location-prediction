package com.google.android.location.preferences;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * A list preference which persists its values as integers instead of strings.
 * Code reading the values should use
 * {@link android.content.SharedPreferences#getInt}.
 * When using XML-declared arrays for entry values, the arrays should be regular
 * string arrays containing valid integer values.
 *
 * @author Andrei
 */
public class IntegerListPreference extends ListPreference {

  public IntegerListPreference(Context context) {
    super(context);

    verifyEntryValues(null);
  }

  public IntegerListPreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    verifyEntryValues(null);
  }

  @Override
  public void setEntryValues(CharSequence[] entryValues) {
    CharSequence[] oldValues = getEntryValues();
    super.setEntryValues(entryValues);
    verifyEntryValues(oldValues);
  }

  @Override
  public void setEntryValues(int entryValuesResId) {
    CharSequence[] oldValues = getEntryValues();
    super.setEntryValues(entryValuesResId);
    verifyEntryValues(oldValues);
  }

  @Override
  protected String getPersistedString(String defaultReturnValue) {
    // During initial load, there's no known default value
    int defaultIntegerValue = Integer.MIN_VALUE;
    if (defaultReturnValue != null) {
      defaultIntegerValue = Integer.parseInt(defaultReturnValue);
    }

    // When the list preference asks us to read a string, instead read an
    // integer.
    int value = getPersistedInt(defaultIntegerValue);
    return Integer.toString(value);
  }

  @Override
  protected boolean persistString(String value) {
    // When asked to save a string, instead save an integer
    return persistInt(Integer.parseInt(value));
  }

  private void verifyEntryValues(CharSequence[] oldValues) {
    CharSequence[] entryValues = getEntryValues();
    if (entryValues == null) {
      return;
    }

    for (CharSequence entryValue : entryValues) {
      try {
        Integer.parseInt(entryValue.toString());
      } catch (NumberFormatException nfe) {
        super.setEntryValues(oldValues);
        throw nfe;
      }
    }
  }
}

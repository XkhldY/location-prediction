package com.google.android.location.content;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/*
* Utilities for the EULA preference value.
*/
public class EulaUtil {
 private static final String EULA_PREFERENCE_FILE = "eula";
 private static final String EULA_PREFERENCE_KEY = "eula.accepted";

 private EulaUtil() {}

 public static boolean getEulaValue(Context context) {
   SharedPreferences preferences = context.getSharedPreferences(
       EULA_PREFERENCE_FILE, Context.MODE_PRIVATE);
   return preferences.getBoolean(EULA_PREFERENCE_KEY, false);
 }

 public static void setEulaValue(Context context) {
   SharedPreferences preferences = context.getSharedPreferences(
       EULA_PREFERENCE_FILE, Context.MODE_PRIVATE);
   Editor editor = preferences.edit();
   editor.putBoolean(EULA_PREFERENCE_KEY, true);
   editor.commit();
 }
}

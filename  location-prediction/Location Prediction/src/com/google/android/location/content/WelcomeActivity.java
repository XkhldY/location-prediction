package com.google.android.location.content;

import com.google.android.locatiom.maps.myroutes.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * An activity that displays a welcome screen.
 *
 * @author Andrei
 */
public class WelcomeActivity extends Activity {

  private static final int DIALOG_ABOUT_ID = 0;
  private static final int DIALOG_EULA_ID = 1;
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.welcome_layout);
    findViewById(R.id.welcome_ok).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });
    findViewById(R.id.welcome_about).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        showDialog(DIALOG_ABOUT_ID);
      }
    });
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    AlertDialog.Builder builder;
    switch (id) {
      case DIALOG_ABOUT_ID:
        LayoutInflater layoutInflator = LayoutInflater.from(this);
        View view = layoutInflator.inflate(R.layout.about, null);
        TextView aboutVersionTextView = (TextView) view.findViewById(R.id.about_version);
        aboutVersionTextView.setText("Version 1");
        
        builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setPositiveButton(R.string.generic_ok, null);
        builder.setNegativeButton(R.string.about_license, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            showDialog(DIALOG_EULA_ID);
          }
        });
        return builder.create();
      case DIALOG_EULA_ID:
        builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.eula_title);
        builder.setMessage(R.string.eula_message);
        builder.setPositiveButton(R.string.generic_ok, null);
        builder.setCancelable(true);
        return builder.create();     
      default:
        return null;
    }
  }
}

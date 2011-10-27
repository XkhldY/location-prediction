package main;



import com.data.R;

import com.data.database.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import android.os.Bundle;

public class StartScreen extends OrmLiteBaseActivity<DatabaseHelper> 
{

	public StartScreen()
	{
	   super();	
	}
	protected Manager getManagerApplication() { 
		Manager  mApp = (Manager)getApplication();
		return mApp;
	}
}
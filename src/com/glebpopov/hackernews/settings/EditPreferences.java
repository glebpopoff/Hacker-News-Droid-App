package com.glebpopov.hackernews.settings;

import com.glebpopov.hackernews.R;
import android.os.Bundle;
import android.content.Intent;
import android.preference.PreferenceActivity;

public class EditPreferences extends PreferenceActivity
{  	
	public void onCreate(Bundle savedInstanceState)
	{  
		super.onCreate(savedInstanceState);  

		addPreferencesFromResource(R.xml.preferences);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		
	}
}

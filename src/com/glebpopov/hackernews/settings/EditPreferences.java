package com.glebpopov.hackernews.settings;

import java.io.File;

import com.glebpopov.hackernews.R;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

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

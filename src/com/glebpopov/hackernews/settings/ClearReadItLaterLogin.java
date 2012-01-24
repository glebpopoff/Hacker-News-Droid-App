package com.glebpopov.hackernews.settings;

import com.glebpopov.hackernews.util.AppSettings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

public class ClearReadItLaterLogin extends Preference
{
	private Context mContext;
	// This is the constructor called by the inflater
	public ClearReadItLaterLogin(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;
	}

	@Override
	protected void onClick() {
	    
		AppSettings settings = new AppSettings(mContext);
		settings.removeReadItLaterCredentials();
		
		Toast.makeText(getContext(), "Successfully removed Read It Later Authentication tokens.", Toast.LENGTH_SHORT).show();
        notifyChanged();
    }
} 

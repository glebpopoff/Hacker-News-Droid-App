package com.glebpopov.hackernews.settings;

import com.glebpopov.hackernews.AskActivity;
import com.glebpopov.hackernews.MainActivity;
import com.glebpopov.hackernews.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

public class ThemeSwitcherSetting extends CheckBoxPreference
{
	private Context mContext;
	private String TAG = "ThemeSwitcherSetting";
	
	public ThemeSwitcherSetting(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;
	}

	@Override
	protected void onClick() {
	    
		if (isChecked())
		{
			Toast.makeText(getContext(), "Default Theme enabled. Please restart the app.", Toast.LENGTH_LONG).show();
			setChecked(false);
			
		} else
		{
			Toast.makeText(getContext(), "Night Mode enabled. Please restart the app.", Toast.LENGTH_LONG).show();
			setChecked(true);
		}
			
        notifyChanged();
        
        //can't redirect to Main Activity b/c next time the user starts the app this screen (Settings) will come up
        /*
         * this enables user-selected theme without restarting the app
         * Intent intent = new Intent(mContext, MainActivity.class);
         * mContext.startActivity(intent);
         */
    }
} 
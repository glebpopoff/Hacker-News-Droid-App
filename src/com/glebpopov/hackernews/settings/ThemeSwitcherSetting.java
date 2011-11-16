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
			Toast.makeText(getContext(), "Default Theme Enabled", Toast.LENGTH_SHORT).show();
			setChecked(false);
			
		} else
		{
			Toast.makeText(getContext(), "Night Mode Enabled", Toast.LENGTH_SHORT).show();
			setChecked(true);
		}
			
        notifyChanged();
        
        Intent intent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(intent);
    }
} 
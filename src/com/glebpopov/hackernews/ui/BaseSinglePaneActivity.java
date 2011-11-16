package com.glebpopov.hackernews.ui;

import com.glebpopov.hackernews.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * A {@link BaseActivity} that simply contains a single fragment. The intent used to invoke this
 * activity is forwarded to the fragment as arguments during fragment instantiation. Derived
 * activities should only need to implement
 *
 * Copied from Google IO app
 */
public abstract class BaseSinglePaneActivity extends BaseActivity {
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //check to see if we need to enable night mode
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("app_nite_mode", false))
        {
        	setTheme(R.style.Theme_HNDroid_HomeInverse);
        } else
        {
        	setTheme(R.style.Theme_HNDroid_Home);
        }
        setContentView(R.layout.activity_singlepane_empty);
        getActivityHelper().setupActionBar(getTitle());

        final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        getActivityHelper().setActionBarTitle(customTitle != null ? customTitle : getTitle());

        if (savedInstanceState == null) {
            mFragment = onCreatePane();
            mFragment.setArguments(intentToFragmentArguments(getIntent()));

            try
            {
	            getSupportFragmentManager().beginTransaction().add(R.id.root_container, mFragment).commit();
	            
            } catch (Exception ex)
            {
            	Log.e("BaseSinglePaneActivity", "Exception in fragment transaction: " + ex);
            }
        }
    }

    /**
     * Called in <code>onCreate</code> when the fragment constituting this activity is needed.
     * The returned fragment's arguments will be set to the intent used to invoke this activity.
     */
    protected abstract Fragment onCreatePane();
    
    public abstract void refreshData();
}

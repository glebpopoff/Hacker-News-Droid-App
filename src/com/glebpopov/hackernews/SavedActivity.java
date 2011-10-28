package com.glebpopov.hackernews;

import com.glebpopov.hackernews.fragments.SavedNewsFragment;
import com.glebpopov.hackernews.ui.BaseSinglePaneActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class SavedActivity extends BaseSinglePaneActivity 
{
	private static final String TAG = "SavedActivity";
	private SavedNewsFragment newsFragment = null;
	
	@Override
    protected Fragment onCreatePane() {
		newsFragment = new SavedNewsFragment(this);
		return newsFragment; 
		
    }
	
	public void displayPreview(String url) {
   		Intent intent = new Intent(this, PreviewActivity.class);
   		intent.putExtra("url", url);
        startActivity(intent);
	}
	
	public void refreshData()
	{
		Log.d(TAG, "Refreshing Data...");
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
    }
}
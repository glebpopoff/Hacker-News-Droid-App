package com.glebpopov.hackernews;

import com.glebpopov.hackernews.fragments.CommentsFragmentView;
import com.glebpopov.hackernews.ui.BaseSinglePaneActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class CommentsActivity extends BaseSinglePaneActivity {
    
	private CommentsFragmentView fragment = null;
	private static final String TAG = "CommentsActivity";

	@Override
    protected Fragment onCreatePane() {
		fragment = new CommentsFragmentView(this);
		return fragment;
		
    }
	
	public void refreshData()
	{
		Log.d(TAG, "Refreshing Data...");
		if (fragment != null)
		{
			fragment.refreshData();
		} else
		{
			try
			{
				fragment = new CommentsFragmentView(this);
				fragment.refreshData();
				
			} catch (Exception ex)
			{
				Log.e(TAG, "Unable to instantiate CommentsFragment or refresh data");
			}
		}
	}
	
	public void displayPreview(String url) {
   		Intent intent = new Intent(this, PreviewActivity.class);
   		intent.putExtra("url", url);
        startActivity(intent);
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
    }
}
package com.glebpopov.hackernews;

import com.glebpopov.hackernews.fragments.PreviewFragment;
import com.glebpopov.hackernews.ui.BaseSinglePaneActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class PreviewActivity extends BaseSinglePaneActivity 
{
	private static final String TAG = "PreviewActivity";
	private PreviewFragment fragment = null;
	
	@Override
    protected Fragment onCreatePane() {
		
		fragment = new PreviewFragment();
		return fragment;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        
    }

	@Override
	public void refreshData() {
		fragment.refresh();
	}
}
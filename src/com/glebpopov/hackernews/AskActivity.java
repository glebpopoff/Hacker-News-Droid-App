package com.glebpopov.hackernews;

import com.glebpopov.hackernews.fragments.NewsFragment;
import com.glebpopov.hackernews.settings.EditPreferences;
import com.glebpopov.hackernews.ui.BaseSinglePaneActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;

public class AskActivity extends BaseSinglePaneActivity 
{
	private static final String TAG = "AskActivity";
	private NewsFragment newsFragment = null;
	private static final int DIALOG_MENU_ID = 1;
	private final static int REQUEST_CODE_PREFERENCES = 1;
	
	@Override
    protected Fragment onCreatePane() {
		newsFragment = new NewsFragment(this, R.string.hn_url_ask);
		return newsFragment; 
		
    }
	
	public void refreshData()
	{
		Log.d(TAG, "Refreshing Data...");
		String startUpUrl = getResources().getString(R.string.hn_url_ask);
		if (newsFragment != null)
		{
			newsFragment.refreshData(startUpUrl);
		} else
		{
			try
			{
				newsFragment = new NewsFragment(this, R.string.hn_url_ask);
				newsFragment.refreshData(startUpUrl);
				
			} catch (Exception ex)
			{
				Log.e(TAG, "Unable to instantiate NewsFragment or refresh data");
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
	        Log.d(TAG, "MENU pressed");
	        
	        showDialog(DIALOG_MENU_ID);
	        
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
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
    
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
	        case DIALOG_MENU_ID:
	        	final CharSequence[] items = getResources().getStringArray(R.array.actionbar_menu);
	
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);// new ContextThemeWrapper(this, R.style.AlertDialogInverse));
	        	builder.setTitle("Hacker News");
	        	builder.setItems(items, new DialogInterface.OnClickListener() {
	        	    public void onClick(DialogInterface dialog, int item) {
	        	    	if (items[item] != null)
	        	    	{
	        	    		Log.d(TAG, "Menu Selection: " + items[item]);
	        	    		
		        	    	if (items[item].equals("Best"))
		        	        {
		        	        	startBestHNActivity();
		        	        } else if (items[item].equals("Newest"))
		        	        {
		        	        	startNewestHNActivity();
		        	        } else if (items[item].equals("Ask HN"))
		        	        {
		        	        	startAskHNActivity();
		        	        } else if (items[item].equals("Saved"))
		        	        {
		        	        	startSavedActivity();
		        	        	
		        	        } else if (items[item].equals("Settings"))
		        	        {
		        	        	displayPreferencesDialog();
		        	        }
	        	    	}
	        	    }
	        	});
	        	dialog = builder.create();
	        	break;
	        default:
	        	dialog = null;
        }
        return dialog;
    }
   
   public void displayPreferencesDialog()
   {
	   startActivityForResult(new Intent(this, EditPreferences.class), REQUEST_CODE_PREFERENCES); 
   }

	public void displayMainNavMenu() {
		showDialog(DIALOG_MENU_ID);
	}
	

	private void startAskHNActivity() {
		Intent intent = new Intent(this, AskActivity.class);
        startActivity(intent);
	}
	
	private void startSavedActivity() {
		Intent intent = new Intent(this, SavedActivity.class);
        startActivity(intent);
	}

	private void startNewestHNActivity() {
		Intent intent = new Intent(this, NewestActivity.class);
        startActivity(intent);
	}

	private void startBestHNActivity() {
		Intent intent = new Intent(this, BestActivity.class);
        startActivity(intent);
	}
}
package com.glebpopov.hackernews.settings;

import com.glebpopov.hackernews.util.DatabaseUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

public class ClearSavedContent extends Preference
{
	private String TAG = "ClearSavedContent";
	private Context mContext;

	// This is the constructor called by the inflater
	public ClearSavedContent(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;
	}

	@Override
	protected void onClick() {
	    
		try
		{
			final SQLiteDatabase db = (new DatabaseUtil(mContext)).getWritableDatabase();
			db.execSQL("DELETE FROM hackernews_saved");
			db.close();
			
		} catch (Exception ex)
		{
			Log.e(TAG, "Exception while saving record: " + ex);
			Toast.makeText(mContext, "Unable to save record", 50000).show();
		}
		
		Toast.makeText(getContext(), "Successfully removed saved content.", Toast.LENGTH_SHORT).show();
        notifyChanged();
    }
} 

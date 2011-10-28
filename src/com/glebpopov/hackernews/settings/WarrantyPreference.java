package com.glebpopov.hackernews.settings;

import com.glebpopov.hackernews.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class WarrantyPreference extends DialogPreference
{
	// This is the constructor called by the inflater
	public WarrantyPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder){
	    // Data has changed, notify so UI can be refreshed!
		builder.setTitle("Warranty Information");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder.setMessage(R.string.warranty_info);
		builder.setNegativeButton(null, null);
    }

} 

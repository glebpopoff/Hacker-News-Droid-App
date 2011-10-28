package com.glebpopov.hackernews.settings;

import com.glebpopov.hackernews.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.widget.TextView;

public class AboutPreference extends DialogPreference
{
	private Context mContext;

	// This is the constructor called by the inflater
	public AboutPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext = context;
	}

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder){
	    // Data has changed, notify so UI can be refreshed!
		builder.setTitle("About the App");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		final TextView message = new TextView(mContext);
		  /*
		  final SpannableString s = 
		               new SpannableString(mContext.getText(R.string.about_app));
		  Linkify.addLinks(s, Linkify.WEB_URLS);
		  message.setText(s);
		  message.setMovementMethod(LinkMovementMethod.getInstance());
		  builder.setView(message);
		  */
		
		builder.setMessage(R.string.about_app);
		builder.setNegativeButton(null, null);
    }

} 
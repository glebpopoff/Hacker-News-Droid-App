package com.glebpopov.hackernews.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class AppSettings 
{
     private static final String APP_SHARED_PREFS = "com.glebpopov.hackernews.util.AppSettings_preferences"; 
     private SharedPreferences appSharedPrefs;
     private Editor prefsEditor;
     private String TAG = "AppSettings";

     public AppSettings(Context context)
     {
         this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
         this.prefsEditor = appSharedPrefs.edit();
     }

     public String getInstapaperUsername() {
        try 
        {
			return EncryptionUtil.decrypt("username", appSharedPrefs.getString("instapaper_username", ""));
		} catch (Exception e) {
			Log.e(TAG, "Unable to decrypt or retrieve username setting: " + e);
		}
		return null;
     }
     
     public String getInstapaperPassword() {
    	try 
        {
 			return EncryptionUtil.decrypt("password", appSharedPrefs.getString("instapaper_password", ""));
 		} catch (Exception e) {
 			Log.e(TAG, "Unable to decrypt or retrieve password setting: " + e);
 		}
 		return null;
     }
     
     public void removeInstapaperCredentials()
     {
         try 
         {
        	//remove username
			prefsEditor.remove("instapaper_username");
			prefsEditor.commit();
			//remove password
			prefsEditor.remove("instapaper_password");
			prefsEditor.commit();
		} catch (Exception e) {
			Log.e(TAG, "Unable to remove or commit username/password setting: " + e);
		}
         
     }

     public void setInstapaperUsername(String text) 
     {
         try 
         {
			prefsEditor.putString("instapaper_username", EncryptionUtil.encrypt("username", text));
			prefsEditor.commit();
		} catch (Exception e) {
			Log.e(TAG, "Unable to encrypt or commit username setting: " + e);
		}
         
     }
     
     public void setInstapaperPassword(String text) 
     {
         try 
         {
			prefsEditor.putString("instapaper_password", EncryptionUtil.encrypt("password", text));
			prefsEditor.commit();
		} catch (Exception e) {
			Log.e(TAG, "Unable to encrypt or commit password setting: " + e);
		}
         
     }
}

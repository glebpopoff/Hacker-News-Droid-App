package com.glebpopov.hackernews.net;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class InstapaperIntegration extends DataParser
{
	private String TAG = "InstapaperIntegration";
	
	public InstapaperIntegration(String url) {
		super(url);
	}

	//returns HTTP code
	public int submitData()
	{
		if (getUrl() == null || getUrl().length() == 0)
		{
			Log.e(TAG, "getJsonData: invalid URL");
			return 500;
		}
		// Create the httpclient
        HttpClient httpclient = new DefaultHttpClient();
 
        // Prepare a request object
        HttpGet httpget = new HttpGet(getUrl());
 
        // Execute the request
        HttpResponse response;
 
        try 
        {
        	// Open the webpage.
            response = httpclient.execute(httpget);
            return response.getStatusLine().getStatusCode();
        }
        catch (IOException  ex) 
        {
            // thrown by line 80 - getContent();
            // Connection was not established
            Log.e(TAG, "getJsonData: Connection failed: " + ex.getMessage());
        }
        finally
        {
        	
        }
        return 500;
	}
}

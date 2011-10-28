package com.glebpopov.hackernews.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.glebpopov.hackernews.domain.CommentItem;
import com.glebpopov.hackernews.domain.NewsItem;

import android.util.Log;

public class DataParser 
{
	private String dataUrl = null;
	private String TAG = "DataParser";
	
	public DataParser(String url)
	{
		dataUrl = url;
	}
	
	public String getUrl()
	{
		return dataUrl;
	}
	
	public ArrayList<NewsItem> getNews()
	{
		if (dataUrl == null || dataUrl.length() == 0)
		{
			Log.e(TAG, "getNews: invalid URL");
			return null;
		}
		Log.d(TAG, "URL: " + dataUrl);
		
		ArrayList<NewsItem> data = new ArrayList<NewsItem>();
		JSONArray jsonData = getJsonData();
		if (jsonData != null)
		{
			
			NewsItem item = null;
			for (int i=0; i<jsonData.length(); i++)
			{
				item = new NewsItem();
				try
				{
					if (jsonData.getJSONObject(i).getString("title") != null &&
						jsonData.getJSONObject(i).getString("title").toLowerCase().equals("nextid")
						)
					{
						item.setTitle(jsonData.getJSONObject(i).getString("title"));
						item.setUrl("http://hndroidapi.appspot.com" + jsonData.getJSONObject(i).getString("url"));
						
					} else
					{
						int id = 0;
						try
						{
							id = Integer.parseInt(jsonData.getJSONObject(i).getString("item_id"));
						} catch (Exception ex) {}
						
						item.setTitle(jsonData.getJSONObject(i).getString("title"));
						item.setUrl(jsonData.getJSONObject(i).getString("url"));
						item.setPostedDate(jsonData.getJSONObject(i).getString("time"));
						item.setPoints(jsonData.getJSONObject(i).getString("score"));
						item.setAuthor(jsonData.getJSONObject(i).getString("user"));
						item.setComments(jsonData.getJSONObject(i).getString("comments"));
						item.setId(id);
					}
				} catch (Exception ex)
				{
					Log.e(TAG, "getNews: exception while parsing JSON data: " + ex);
				}
				data.add(item);
			}
			
		}
        
        Log.d(TAG, "getNews: returning " + data.size() + " elements");
        
		return data;
	}
	
	public ArrayList<CommentItem> getComments()
	{
		if (dataUrl == null || dataUrl.length() == 0)
		{
			Log.e(TAG, "getComments: invalid URL");
			return null;
		}
		JSONArray jsonData = getJsonData();
		ArrayList<CommentItem> data = parseComments(jsonData);
        Log.d(TAG, "getComments: returning " + data.size() + " elements");
		return data;
	}
	
	public ArrayList<CommentItem> parseComments(JSONArray jsonData)
	{
		ArrayList<CommentItem> data = new ArrayList<CommentItem>();
		if (jsonData != null)
		{
			//Log.d(TAG, "parseComments: Container Count=" + jsonData.length());
			CommentItem item = null;
			for (int i=0; i<jsonData.length(); i++)
			{
				item = new CommentItem();
				try
				{
					int id = 0;
					try
					{
						id = Integer.parseInt(jsonData.getJSONObject(i).getString("item_id"));
					} catch (Exception ex) {}
					
					item.setComment(jsonData.getJSONObject(i).getString("comment"));
					item.setReplyId(jsonData.getJSONObject(i).getString("reply_id"));
					item.setPostedDate(jsonData.getJSONObject(i).getString("time"));
					item.setAuthor(jsonData.getJSONObject(i).getString("username"));
					if (jsonData.getJSONObject(i).getJSONArray("children") != null)
					{
						item.setChildren(parseComments(jsonData.getJSONObject(i).getJSONArray("children")));
					}
					item.setId(id);
				} catch (Exception ex)
				{
					Log.e(TAG, "parseComments: exception while parsing JSON data: " + ex);
				}
				data.add(item);
			}
		}
		return data;
	}
	
	public JSONArray getJsonData()
	{
		if (dataUrl == null || dataUrl.length() == 0)
		{
			Log.e(TAG, "getJsonData: invalid URL");
			return null;
		}
		// Create the httpclient
        HttpClient httpclient = new DefaultHttpClient();
 
        // Prepare a request object
        HttpGet httpget = new HttpGet(dataUrl);
 
        // Execute the request
        HttpResponse response;
 
        try {
 
        	// return string
            String returnString = null;
            
            // Open the webpage.
            response = httpclient.execute(httpget);
 
            if(response.getStatusLine().getStatusCode() == 200){
                // Connection was established. Get the content.
 
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to worry about connection release
 
                if (entity != null) {
                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    
                    returnString = convertStreamToString(instream);
                    
                    //Log.d(TAG, "Return String: " + returnString);
 
                    // Load the requested page converted to a string into a JSONObject.
                    JSONObject myAwway = new JSONObject(returnString);
 
                    // Get the query value'
                    //String query = myAwway.getString("query");
 
                    // Make array of the suggestions
                    JSONArray recs = myAwway.getJSONArray("items");
                    if (recs != null)
                    {
                    	Log.d(TAG, "getJsonData: Got JSON Data: " + recs.length());
                    } else
                    {
                    	Log.w(TAG, "getJsonData: no data returned");
                    }
                    // Cose the stream.
                    instream.close();
                    return recs;
                    
                }
              
            }
            else {
                // code here for a response othet than 200.  A response 200 means the webpage was ok
                // Other codes include 404 - not found, 301 - redirect etc...
                // Display the response line.
            	Log.e(TAG, "getJsonData: Unable to load page. Status: " + response.getStatusLine());
            }
        }
        catch (IOException  ex) {
            // thrown by line 80 - getContent();
            // Connection was not established
            Log.e(TAG, "getJsonData: Connection failed: " + ex.getMessage());
        }
        catch (JSONException ex){
            // JSON errors
        	Log.e(TAG, "getJsonData: JSONException: " + ex.getMessage());
        } finally
        {
        	
        }
        return null;
	}
	
	public String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

	public NewsItem getLatestNews() {
		Log.d(TAG, "getLatestNews");
		ArrayList<NewsItem> data = getNews();
		if (data != null && data.size() > 0 && data.get(0) != null && data.get(0).getTitle() != null)
		{
			Log.d(TAG, "getLatestNews: successfully retrieved " + data.size() + " records");
			return data.get(0);
		} else
		{
			Log.w(TAG, "getLatestNews: no data returned");
			return null;
		}
	}
}

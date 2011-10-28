package com.glebpopov.hackernews.service;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Random;

import com.glebpopov.hackernews.R;
import com.glebpopov.hackernews.domain.NewsItem;
import com.glebpopov.hackernews.net.DataParser;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class HackerNewsService extends Service {
	public static final String UPDATENEWS = "UpdateMood";	
	private String currentMood;
	private NewsItem newsItem = null;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStart(intent, startId);
		
        //Log.i(HackerNewsWidgetProvider.WIDGETTAG, "onStartCommand");
        
        final DataParser dataService = new DataParser("http://hndroidapi.appspot.com/latest");
        newsItem = dataService.getLatestNews();

        updateUI(intent);
			
		stopSelf(startId);
		
		return START_STICKY;
	}

	private void updateUI(Intent intent) 
	{
        Log.i(HackerNewsWidgetProvider.WIDGETTAG, "Intent: " + intent);
        if (intent != null){
    		String requestedAction = intent.getAction();
            Log.i(HackerNewsWidgetProvider.WIDGETTAG, "Action: " + requestedAction);
    		if (requestedAction != null && requestedAction.equals(UPDATENEWS))
    		{ 	
    			if (newsItem != null)
    			{
    				int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
		            Log.d(HackerNewsWidgetProvider.WIDGETTAG, "Widget Id: " + widgetId);
		            AppWidgetManager appWidgetMan = AppWidgetManager.getInstance(this);
		            RemoteViews views = new RemoteViews(this.getPackageName(),R.layout.widgetlayout);
		            views.setTextViewText(R.id.widget_title, newsItem.getTitle());
		            views.setTextViewText(R.id.widget_hour, newsItem.getPostedDate());
		            views.setTextViewText(R.id.widget_points, newsItem.getPoints());
		            views.setTextViewText(R.id.widget_author_comments, newsItem.getComments());
		            appWidgetMan.updateAppWidget(widgetId, views);
		            
		            Log.i(HackerNewsWidgetProvider.WIDGETTAG, "Data updated!");
    			} else
    			{
    				Log.w(HackerNewsWidgetProvider.WIDGETTAG, "Invalid News Data Object");
    			}
    		}
        }
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
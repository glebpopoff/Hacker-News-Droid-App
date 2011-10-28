package com.glebpopov.hackernews.service;

import com.glebpopov.hackernews.MainActivity;
import com.glebpopov.hackernews.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class HackerNewsWidgetProvider  extends AppWidgetProvider {
	public static final String WIDGETTAG = "HackerNewsWidget";
	
	/*@Override
	public void onEnabled(Context context) {  
        //Log.v("toggle_widget","Enabled is being called"); 

        AppWidgetManager mgr = AppWidgetManager.getInstance(context); 
        //retrieve a ref to the manager so we can pass a view update 

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widgetlayout);
        
        /*Intent intent = new Intent(); 
	    intent.setClassName("com.glebpopov.hackernews", "com.glebpopov.hackernews.MainActivity"); 
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0); 
		*/
        /*ComponentName comp = new ComponentName(context.getPackageName(), HackerNewsWidgetProvider.class.getName());
	    
        Intent intent = new Intent(context, HackerNewsService.class);
	    intent.setAction(HackerNewsService.UPDATENEWS);
	    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, comp);
	    PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
	    
	    views.setOnClickPendingIntent(R.id.widget_refresh, pendingIntent);
	    Log.i(WIDGETTAG, "pending intent set");
	    
	    
	    // Tell the AppWidgetManager to perform an update on the current App Widget
	    mgr.updateAppWidget(comp, views);
	} */
	
	public void onEnabled(Context context) {  
        //Log.v("toggle_widget","Enabled is being called"); 

        AppWidgetManager mgr = AppWidgetManager.getInstance(context); 
        //retrieve a ref to the manager so we can pass a view update 

        
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        // first param is app package name, second is package.class of the main activity
        ComponentName cn = new ComponentName(context.getPackageName(), MainActivity.class.getName());
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        PendingIntent myPI = PendingIntent.getActivity(context, 0, intent, 0); 
        
        //intent to start service 

      // Get the layout for the App Widget 
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widgetlayout); 

      //attach the click listener for the service start command intent 
      views.setOnClickPendingIntent(R.id.widget_title, myPI); 

      //define the componenet for self 
      ComponentName comp = new ComponentName(context.getPackageName(), HackerNewsWidgetProvider.class.getName()); 

      //tell the manager to update all instances of the toggle widget with the click listener 
      mgr.updateAppWidget(comp, views); 
	} 
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
	    //Log.i(WIDGETTAG, "onUpdate");
		
		final int N = appWidgetIds.length;
		int appWidgetId = -1;
		if (N == 1)  
		{  
			appWidgetId = appWidgetIds[0];  
		} 
		
		if (appWidgetId == -1)
		{
			//Log.e(WIDGETTAG, "Invalid Widget Id");
			return;
		}
	    //Log.i(WIDGETTAG, "updating widget " + appWidgetId);
	    
	    Intent intent = new Intent(context, HackerNewsService.class);
	    intent.setAction(HackerNewsService.UPDATENEWS);
	    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	    
	    
	    PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
	    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widgetlayout);
	    views.setOnClickPendingIntent(R.id.widget_refresh, pendingIntent);
	    //Log.i(WIDGETTAG, "pending intent set");
	    
	    // Tell the AppWidgetManager to perform an update on the current App Widget
	    appWidgetManager.updateAppWidget(appWidgetId, views);
	    
	    context.startService(intent);
	}	
}
package com.glebpopov.hackernews.fragments;

import java.net.URLEncoder;
import java.util.ArrayList;

import com.glebpopov.hackernews.AskActivity;
import com.glebpopov.hackernews.BestActivity;
import com.glebpopov.hackernews.CommentsActivity;
import com.glebpopov.hackernews.MainActivity;
import com.glebpopov.hackernews.NewestActivity;
import com.glebpopov.hackernews.R;
import com.glebpopov.hackernews.SavedActivity;
import com.glebpopov.hackernews.domain.NewsItem;
import com.glebpopov.hackernews.net.DataParser;
import com.glebpopov.hackernews.net.InstapaperIntegration;
import com.glebpopov.hackernews.util.AppSettings;
import com.glebpopov.hackernews.util.DatabaseUtil;
import com.glebpopov.hackernews.util.ImageLoader;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SavedNewsFragment extends ListFragment 
{
	private String TAG = "SavedNewsFragment";
	private ArrayList<NewsItem> data = new ArrayList<NewsItem>();
	private ProgressDialog m_ProgressDialog = null;
	private NewsItemAdapter mAdapter = null;
	private Activity mActivity;
	private TextView mEditUsername, mEditPassword;
	private Button mInstapaperButton;
	private Dialog mInstapaperDialog;
	private CheckBox mRememberLogin;
	private AppSettings appSettings = null;
	private int instapaperReturnCode = -1;
	protected SharedPreferences sharedPref = null;
	protected boolean isNiteMode = false;
	
	public SavedNewsFragment()
	{
		mActivity = getActivity();
	}
	
	public SavedNewsFragment(Activity m)
	{
		Log.d(TAG, "New Instance");
		mActivity = m;
	}
	
	private void getSavedNewsItems()
	{
		Log.d(TAG, "getSavedNewsItems: getting data from db");
		SQLiteDatabase db = null;
		Cursor cs = null;
		try
		{
			db = (new DatabaseUtil(mActivity)).getWritableDatabase();
			cs = db.rawQuery("SELECT _id, item_id, title, url, author,posted_date,points,comments FROM hackernews_saved ORDER BY created_date DESC", new String [] {});
			
			cs.moveToFirst();
		    if (cs != null) {
		        do {
		        	if (cs.getCount() > 0)
		        	{
			        	Log.d(TAG, "Got some data");
						//add first record
						NewsItem item = new NewsItem();
						item.setAuthor(cs.getString(cs.getColumnIndex("author")));
						item.setId(cs.getInt(cs.getColumnIndex("item_id")));
						item.setUrl(cs.getString(cs.getColumnIndex("url")));
						item.setSystemId(cs.getInt(cs.getColumnIndex("_id")));
						item.setTitle(cs.getString(cs.getColumnIndex("title")));
						item.setPostedDate(cs.getString(cs.getColumnIndex("posted_date")));
						item.setComments(cs.getString(cs.getColumnIndex("comments")));
						item.setPoints(cs.getString(cs.getColumnIndex("points")));
						
						Log.d(TAG, "Adding Item: " + item.getTitle());
						data.add(item);
		        	}
					
		        } while (cs.moveToNext());
		    }
		} catch (Exception ex)
		{
			Log.e(TAG, "getView Exception: " + ex);
    		Toast.makeText(mActivity, "Unable to display records", 50000).show();
		} finally
		{
			if (cs != null)
			{
				cs.close();
			}
			if (db != null)
			{
				db.close();
			}
		}
		
		mActivity.runOnUiThread(new Runnable() {
            public void run() 
            {
            	if (data != null && 
            		data.size() > 0 && 
            		data.get(0) != null && 
            		data.get(0).getId() > 0)
            	{
            		Log.d(TAG, "getNewsItems: got " + data.size() + " elements");
            	} else
            	{
            		Log.d(TAG, "getNewsItems: no data returned");
            		setEmptyText("No items saved.");
            		//Toast.makeText(getActivity(), R.string.toast_now_not_visible, Toast.LENGTH_SHORT).show();
            	}
            	
            	mAdapter = new NewsItemAdapter(mActivity, R.id.header_news, data);
            	setListAdapter(mAdapter);
            	registerForContextMenu(getListView());
            	m_ProgressDialog.hide();
            } });
	}
		
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        
        if (sharedPref == null)
    	{
    		sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
    	}
        
        if (sharedPref.getBoolean("app_nite_mode", false))
        {
        	Log.d(TAG, "App Nite Mode");
    		isNiteMode = true;
        }
        
        if (isNiteMode)
    	{
    		getListView().setBackgroundColor(Color.BLACK);
    	}
        
        retrieveSavedData();
        
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.d(TAG, "onCreateContextMenu");
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		final NewsItem item = (NewsItem)mAdapter.getItem(info.position);
		if (item != null)
		{
			menu.setHeaderTitle(item.getTitle());
	        Log.d(TAG, "Title: " + item.getTitle());
		    String[] menuItems = getResources().getStringArray(R.array.saved_menu);
		    for (int i = 0; i<menuItems.length; i++) 
		    {
		    	menu.add(Menu.NONE, i, i, menuItems[i]);
		    }
		} else
		{
			Log.w(TAG, "onCreateContextMenu: Unable to get selected item");
		}
	}
	
	private int saveToInstapaper(CharSequence username, CharSequence password, String articleUrl, String title)
	{
		try
		{
			String url = "https://www.instapaper.com/api/add?username=" + URLEncoder.encode(username.toString()) + "&password=" + URLEncoder.encode(password.toString()) + "&url=" + URLEncoder.encode(articleUrl) + "&title=" + URLEncoder.encode(title);
			InstapaperIntegration instapaperNet = new InstapaperIntegration(url);
			instapaperReturnCode = instapaperNet.submitData();
			Log.d(TAG, "Instapaper Return code: " + instapaperReturnCode);
			
			 mActivity.runOnUiThread(new Runnable() {
		            public void run() 
		            {
		            	
		            	if (instapaperReturnCode == 201)
	            		{
	            			Toast.makeText(mActivity, "Saved successfully", 50000).show();
	            			if (mInstapaperDialog != null)
	            			{
	            				mInstapaperDialog.cancel();
	            			}
	            		} else if (instapaperReturnCode == 400)
	            		{
	            			Toast.makeText(mActivity, "Unable to save: Bad request or exceeded the rate limit.", 50000).show();
	            			if (mInstapaperDialog != null)
	            			{
	            				mInstapaperDialog.cancel();
	            			}
	            		} else if (instapaperReturnCode == 403)
	            		{
	            			Toast.makeText(mActivity, "Unable to save: Invalid username or password. Please try again.", 50000).show();
	            		} else if (instapaperReturnCode == 500)
	            		{
	            			Toast.makeText(mActivity, "Unable to save: The service encountered an error. Please try again later.", 50000).show();
	            			if (mInstapaperDialog != null)
	            			{
	            				mInstapaperDialog.cancel();
	            			}
	            		} 
		            	
		            	if (m_ProgressDialog != null)
		            	{
		            		m_ProgressDialog.hide();
		            	}
		            } });
			
			
		} catch (Exception ex)
		{
			Log.e(TAG, "Exception while saving to Instapaper: " + ex);
			mActivity.runOnUiThread(new Runnable() {
	            public void run() 
	            {
	            	Toast.makeText(mActivity, "Unable to save: application error.", 50000).show();
	            	
	            	if (m_ProgressDialog != null)
	            	{
	            		m_ProgressDialog.hide();
	            	}
	            }
	            });
		}
		return instapaperReturnCode;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		Log.d(TAG, "onContextItemSelected");
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		final String[] menuItems = getResources().getStringArray(R.array.saved_menu);
		final String menuItemName = menuItems[menuItemIndex];
		
		final NewsItem newsItem = (NewsItem)mAdapter.getItem(info.position);
		if (newsItem != null && newsItem.getUrl() != null && 
			(newsItem.getUrl().startsWith("http") || newsItem.getUrl().startsWith("https"))
			)
		{
			Log.d(TAG, "Selected: " + newsItem.getTitle());
			if (menuItemName.equals("Read Original"))
			{
				Log.d(TAG, "Read Article");
				final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getUrl()));
				startActivity(browserIntent);
				
			} else if (menuItemName.equals("Read Text"))
			{
				Log.d(TAG, "Preview");
				if (mActivity instanceof MainActivity) 
				{
					((MainActivity)mActivity).displayPreview(newsItem.getUrl());
					
				} else if (mActivity instanceof BestActivity) 
				{
					((BestActivity)mActivity).displayPreview(newsItem.getUrl());
					
				} else if (mActivity instanceof NewestActivity) 
				{
					((NewestActivity)mActivity).displayPreview(newsItem.getUrl());
					
				} else if (mActivity instanceof AskActivity) 
				{
					((AskActivity)mActivity).displayPreview(newsItem.getUrl());
					
				} else if (mActivity instanceof SavedActivity) 
				{
					((SavedActivity)mActivity).displayPreview(newsItem.getUrl());
				}
				
			} else if (menuItemName.equals("Share"))
			{
				final Intent intent = new Intent(Intent.ACTION_SEND);
		        intent.setType("text/plain");
		        intent.putExtra(Intent.EXTRA_TEXT, newsItem.getUrl());

		        try 
		        {
		        	startActivity(Intent.createChooser(intent, "Share via"));
		        } catch (android.content.ActivityNotFoundException ex) 
		        {
		        	Log.e(TAG, "Exception while displaying Share Intent: " + ex);
					Toast.makeText(mActivity, "Unable to start Share This intent", 50000).show();
		        }
			}  else if (menuItemName.equals("Save to Instapaper"))
			{
				Runnable instapaperRunner = new Runnable(){
		            @Override
		            public void run() {
		            	
		            
				try
				{
					boolean showDialog = true;
					if (appSettings == null)
	        		{
	        			appSettings = new AppSettings(getActivity());
	        		}
					
					//saved login
					String savedUsername = appSettings.getInstapaperUsername();
					String savedPassword = appSettings.getInstapaperPassword();
					
					//ok, if we already saved credentials then don't display the dialog
					if (savedUsername != null && 
						savedUsername.length() > 0 &&
						savedPassword != null && 
						savedPassword.length() > 0
						)
					{
						showDialog = false;
						//submit to instapaper
                		int code = saveToInstapaper(savedUsername,
                								savedPassword,
                								  newsItem.getUrl(),
                								  newsItem.getTitle()
                									);
                		
	                	if (code == 403)
	            		{
	            			showDialog = true;
	            		}
					}
					
					if (showDialog)
					{
						mActivity.runOnUiThread(new Runnable() {
				            public void run() 
				            {
						mInstapaperDialog = new Dialog(getActivity());
						mInstapaperDialog.setContentView(R.layout.login_dialog);
						mInstapaperDialog.setTitle("Save to Instapaper");
						mInstapaperDialog.setCancelable(true);
		                //there are a lot of settings, for dialog, check them all out!
		 
		                mEditUsername = (TextView) mInstapaperDialog.findViewById(R.id.editUsername);
		                mEditPassword = (TextView) mInstapaperDialog.findViewById(R.id.editPassword);
		                mRememberLogin = (CheckBox) mInstapaperDialog.findViewById(R.id.checkBoxRemember);
		                
		                //set up button
		                mInstapaperButton = (Button) mInstapaperDialog.findViewById(R.id.buttonLogin);
		                mInstapaperButton.setOnClickListener(new OnClickListener() {
		                @Override
		                    public void onClick(View v) {
			                
		                		//save login if the 'remember me' checked
			                	if (mRememberLogin.isChecked() && 
			                		mEditUsername.getText() != null && 
			                		mEditPassword.getText() != null
			                		) 
			                    {
			                		
			                    	appSettings.setInstapaperUsername(mEditUsername.getText().toString());
			                    	appSettings.setInstapaperPassword(mEditPassword.getText().toString());
			                    }
		                	
		                		//submit to instapaper
		                		int code = saveToInstapaper(mEditUsername.getText(),
		                								  mEditPassword.getText(),
		                								  newsItem.getUrl(),
		                								  newsItem.getTitle()
		                									);
		                		
		                    }
		                });
		                //now that the dialog is set up, it's time to show it    
		                mInstapaperDialog.show();
				            }});
					}
				} catch (Exception ex)
				{
					Log.e(TAG, "SaveToInstapaper UI/Network/Data Errors: " + ex);
					Toast.makeText(mActivity, "Unable to save: application error", 50000).show();
				}   
		        
	            }};
	            
	            m_ProgressDialog = ProgressDialog.show(mActivity, "Saving", "Please wait...", true);
	            
	            Thread thread =  new Thread(null, instapaperRunner, "HackerNewsInstaPaperBackground");
	            thread.start();
	            
			} else if (menuItemName.equals("Remove"))
			{
				Log.d(TAG, "Remove Article");
				if (newsItem.getId() > 0)
				{
					try
					{
						final SQLiteDatabase db = (new DatabaseUtil(mActivity)).getWritableDatabase();
						db.delete("hackernews_saved","_id=?", new String [] { String.valueOf(newsItem.getSystemId()) });
						db.close();
						Toast.makeText(mActivity, "Removed successfully", 50000).show();
						refreshData();
					} catch (Exception ex)
					{
						Log.e(TAG, "Exception while removing record: " + ex);
						Toast.makeText(mActivity, "Unable to remove record", 50000).show();
					}
				} else
				{
					Toast.makeText(mActivity, "Invalid Record Id", 50000).show();
				}
				
			} else if (menuItemName.equals("Comments"))
			{
				Log.d(TAG, "Comments Article");
				if (newsItem.getId() > 0)
				{
		            Intent intent = new Intent(mActivity, CommentsActivity.class);
		            intent.putExtra("story_item", newsItem);
		            startActivity(intent);
				} else
				{
					Toast.makeText(mActivity, "Invalid Record Id", 50000).show();
				}
			}
		}
		  
		return true;
	}
	
	@Override
    public void onResume() {
        super.onResume();
        
    }

    @Override
    public void onPause() {
        super.onPause();
        m_ProgressDialog.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
    }

    /** {@inheritDoc} */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(TAG, "onListItemClick");
        if (mAdapter == null)
        {
        	Log.w(TAG, "onListItemClick: adapter is null. Exiting...");
        	return;
        }
        final NewsItem newsItem = (NewsItem)mAdapter.getItem(position);
        Log.d(TAG, "URL: " + newsItem.getUrl());
        if (newsItem != null && newsItem.getUrl() != null && 
        	(newsItem.getUrl().startsWith("http") || newsItem.getUrl().startsWith("https"))
        	)
		{
        	Log.d(TAG, "onListItemClick: URL: " + newsItem.getUrl());
        	final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getUrl()));
        	startActivity(browserIntent);
		}
    }
	    
	private class NewsItemAdapter extends ArrayAdapter<NewsItem> {

        private ArrayList<NewsItem> items;
        private Context context;
        
        public NewsItemAdapter(Context c, int textViewResourceId, ArrayList<NewsItem> items) {
                super(c, textViewResourceId, items);
                context = c;
                this.items = items;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	try
        	{
                
        		View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.fragment_news, null);
                    
                    if (isNiteMode)
                    {
                    	v.setBackgroundColor(Color.BLACK);
                    }
                }
                NewsItem o = items.get(position);
                if (o != null && o.getTitle() != null) 
	            {
	                TextView titleView = (TextView) v.findViewById(R.id.news_title);
	                TextView hourView = (TextView) v.findViewById(R.id.news_hour);
	                TextView authorView = (TextView) v.findViewById(R.id.news_author);
	                TextView commentsView = (TextView) v.findViewById(R.id.news_comments);
	                TextView pointView = (TextView) v.findViewById(R.id.news_points);
	                TextView domainView = (TextView) v.findViewById(R.id.news_domain);
	                
	                if (!o.getTitle().toLowerCase().equals("nextid"))
	                {
	                    if (domainView != null && o.getDomain() != null)
	                    {
	                    	domainView.setText(o.getDomainReadable());
	                    	if (isNiteMode)
	                    	{
	                    		domainView.setBackgroundColor(Color.TRANSPARENT);
	                    		domainView.setTextAppearance(mActivity, R.style.DomainTextInverse);
	                    	}
	                    }
	                    
	                    if (titleView != null) 
	                    {
	                    	titleView.setText(o.getTitle());  
	                    	if (isNiteMode)
	                    	{
	                    		titleView.setTextAppearance(mActivity, R.style.TextHeaderInverse);
	                    	}
	                    }
	                    
	                    if (hourView != null && o.getPostedDate() != null) 
	                    {
	                    	hourView.setText(o.getPostedDate());  
	                    	if (isNiteMode)
	                    	{
	                    		hourView.setTextColor(Color.LTGRAY);
	                    	}
	                    }
	                    
	                    if (authorView != null && o.getAuthor() != null) 
	                    {
	                    	authorView.setText(o.getAuthor() + " | ");
	                    	if (isNiteMode)
	                    	{
	                    		authorView.setTextColor(Color.LTGRAY);
	                    	}
	                    }
	                    
	                    if (commentsView != null && o.getComments() != null) 
	                    {
	                    	commentsView.setText(o.getComments());
	                    	if (isNiteMode)
	                    	{
	                    		commentsView.setTextColor(Color.LTGRAY);
	                    	}
	                    }
	                    
	                    if (pointView != null && o.getPoints() != null) 
	                    {
	                    	pointView.setText(o.getPoints());    
	                    	if (isNiteMode)
	                    	{
	                    		pointView.setTextColor(Color.LTGRAY);
	                    	}
	                    }
	                }                  
                }
                return v;
        	}
            catch (Exception ex)
        	{
        		Log.e(TAG, "getView Exception: " + ex);
        		Toast.makeText(mActivity, "Unable to display records", 50000).show();
        	}
        	
        	//return dummy view so app doesn't crash
        	setEmptyText("No data returned");
        	LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return vi.inflate(R.layout.blank_view, null);
        }
	}
	
	private void retrieveSavedData()
	{
		Runnable newsRetriever = new Runnable(){
            @Override
            public void run() {
            	getSavedNewsItems();    	
            }
        };
        
        m_ProgressDialog = ProgressDialog.show(mActivity, "Downloading", "Please wait...", true);
        
        Thread thread =  new Thread(null, newsRetriever, "HackerNewsAPIBackground");
        thread.start();
	}
	
	public void refreshData() {
		data = new ArrayList<NewsItem>();
		retrieveSavedData();
	}
}

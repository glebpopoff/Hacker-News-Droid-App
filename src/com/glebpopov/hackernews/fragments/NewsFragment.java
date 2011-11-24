package com.glebpopov.hackernews.fragments;

import java.net.URLEncoder;
import java.util.ArrayList;

import com.glebpopov.hackernews.CommentsActivity;
import com.glebpopov.hackernews.MainActivity;
import com.glebpopov.hackernews.SavedActivity;
import com.glebpopov.hackernews.AskActivity;
import com.glebpopov.hackernews.BestActivity;
import com.glebpopov.hackernews.NewestActivity;
import com.glebpopov.hackernews.R;
import com.glebpopov.hackernews.domain.NewsContainer;
import com.glebpopov.hackernews.domain.NewsItem;
import com.glebpopov.hackernews.net.DataParser;
import com.glebpopov.hackernews.net.InstapaperIntegration;
import com.glebpopov.hackernews.util.AppSettings;
import com.glebpopov.hackernews.util.DatabaseUtil;
import com.glebpopov.hackernews.util.ImageLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewsFragment extends ListFragment 
{
	private String TAG = "NewsFragment";
	private ArrayList<NewsItem> data = new ArrayList<NewsItem>();
	private ProgressDialog m_ProgressDialog = null;
	private ImageLoader imageLoader = null;
	private NewsItemAdapter mAdapter = null;
	private Activity mActivity;
	private int dataUrlResource;
	private String nextDataUrl = null;
	private TextView mEditUsername, mEditPassword;
	private Button mInstapaperButton;
	private Dialog mInstapaperDialog;
	private CheckBox mRememberLogin;
	private AppSettings appSettings = null;
	private int instapaperReturnCode = -1;
	protected SharedPreferences sharedPref = null;
	protected boolean isNiteMode = false;
	protected NewsItem moreLink = null;
	
	private View.OnClickListener loadMoreListener = new View.OnClickListener() {
        public void onClick(View view) {
        	if (nextDataUrl != null)
        	{
        		refreshData(nextDataUrl);
        	} else
        	{
        		refreshData(getResources().getString(dataUrlResource));
        	}
        }
    };
    
    public NewsFragment()
    {
    	mActivity = getActivity();
    	dataUrlResource = R.string.hn_url_news;
    }
	
	public NewsFragment(Activity m, int urlRes)
	{
		Log.d(TAG, "New Instance");
		mActivity = m;
		dataUrlResource = urlRes;
	}
	
	private void getNewsItems(String url)
	{
		Log.d(TAG, "getNewsItems: getting data: " + url);
		final DataParser d = new DataParser(url);
        NewsContainer container = d.getNews();
        if (container != null)
		{
        	data = container.getNewsContainer();
        	moreLink = container.getMoreNewsLink();
		}
        
        //try again (in case of a network glitch?)
		if (!(container != null && 
				data != null && 
        		data.size() > 0 && 
        		data.get(0) != null && 
        		data.get(0).getId() > 0))
        {
			data = container.getNewsContainer();
        	moreLink = container.getMoreNewsLink();
        }
        
		//check data container
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
            		setEmptyText("Unable to retrieve data. Please try again.");
            	}
            	
            	//set more link
            	if (moreLink != null && !moreLink.getUrl().equals(""))
            	{
            		nextDataUrl = moreLink.getUrl();
            	}
            	
            	mAdapter = new NewsItemAdapter(mActivity, R.id.header_news, data);
            	setListAdapter(mAdapter);
            	registerForContextMenu(getListView());
            	if (m_ProgressDialog != null)
            	{
            		m_ProgressDialog.hide();
            	}
            	getListView().setFastScrollEnabled(true);
            } });
	}
		
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        String startUpUrl = getResources().getString(dataUrlResource);
        
        if (mActivity == null)
        {
        	mActivity = getActivity();
        }
        
        if (mActivity == null)
        {
        	Toast.makeText(mActivity, "Something went wrong. Please restart the app.", 50000).show();
        	return;
        }
        
        if (sharedPref == null)
    	{
    		sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
    	}
        
        if (sharedPref.getBoolean("app_nite_mode", false))
        {
        	Log.d(TAG, "App Nite Mode");
    		isNiteMode = true;
        }
    	
        //add footerview
    	LayoutInflater vi = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.news_footer_view, null);
    	if (v != null)
    	{
    		Button b = (Button) v.findViewById(R.id.get_more_button);
        	if (b != null)
        	{
        		Log.d(TAG, "Adding Footer View");
        		b.setVisibility(View.VISIBLE);
            	b.setOnClickListener(loadMoreListener);
            	
            	if (isNiteMode)
            	{
            		v.setBackgroundColor(Color.BLACK);
            		getListView().setBackgroundColor(Color.BLACK);
            	}
            	
            	getListView().addFooterView(v);
        	}
    	}
        
        downloadData(startUpUrl);
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.d(TAG, "onCreateContextMenu");
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		final NewsItem item = (mAdapter != null && 
								mAdapter.items != null && 
								mAdapter.items.size() > 0
								) ? (NewsItem)mAdapter.getItem(info.position) : null;
		if (item != null)
		{
			menu.setHeaderTitle(item.getTitle());
			
			Log.d(TAG, "Title: " + item.getTitle());
		    String[] menuItems = getResources().getStringArray(R.array.article_menu);
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
		final String[] menuItems = getResources().getStringArray(R.array.article_menu);
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
			} else if (menuItemName.equals("Save to Instapaper"))
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
				            	
				            	if (m_ProgressDialog != null)
				            	{
				            		m_ProgressDialog.hide();
				            	}
				            	
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
			                	
			                	m_ProgressDialog = ProgressDialog.show(mActivity, "Saving", "Please wait...", true);
		                	
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
		            
			} else if (menuItemName.equals("Save for Later"))
			{
				Log.d(TAG, "Save Article");
				if (newsItem.getId() > 0)
				{
					try
					{
						final SQLiteDatabase db = (new DatabaseUtil(mActivity)).getWritableDatabase();
						ContentValues cv=new ContentValues();
						cv.put("item_id", newsItem.getId());
						cv.put("title", newsItem.getTitle());
						cv.put("url", newsItem.getUrl());
						cv.put("author", newsItem.getAuthor());
						cv.put("posted_date", newsItem.getPostedDate());
						cv.put("points", newsItem.getPoints());
						cv.put("comments", newsItem.getComments());
						db.insert("hackernews_saved", null, cv);
						db.close();
						Toast.makeText(mActivity, "Saved successfully", 50000).show();
						
					} catch (Exception ex)
					{
						Log.e(TAG, "Exception while saving record: " + ex);
						Toast.makeText(mActivity, "Unable to save record", 50000).show();
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
        if (m_ProgressDialog != null)
        {
        	m_ProgressDialog.dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
    }

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
        	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    		boolean openURL = sharedPref.getBoolean("article_onlick", true);
        	
    		if (!newsItem.isHNUrl() && openURL)
        	{
        		Log.d(TAG, "onListItemClick: URL: " + newsItem.getUrl());
        		final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getUrl()));
        		startActivity(browserIntent);
        	} else
        	{
        		Log.d(TAG, "onListItemClick: Displaying Comments for Internal HN Resource");
        		Intent intent = new Intent(mActivity, CommentsActivity.class);
	            intent.putExtra("story_item", newsItem);
	            startActivity(intent);
        	}
		}
    }
    
    public boolean isMoreLink(String title, String url)
    {
    	if (title != null && url != null &&
    		(title.toLowerCase().equals("nextid") || title.toLowerCase().equals("more"))
    		)
    	{
    		return true;
    	}
    	return false;
    }
	    
	private class NewsItemAdapter extends ArrayAdapter<NewsItem> {

        private ArrayList<NewsItem> items;
        private Context context;
        private int counter = 1;
        
        public NewsItemAdapter(Context c, int textViewResourceId, ArrayList<NewsItem> items) {
                super(c, textViewResourceId, items);
                context = c;
                this.items = items;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
            //Log.d(TAG, "getView");
            
        	try
        	{
	    		View v = convertView;
	            if (v == null) {
	                //Log.d(TAG, "Inflating Layout");
	            	LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                v = vi.inflate(R.layout.fragment_news, null);
	            }
	            if (isNiteMode)
	            {
	            	v.setBackgroundColor(Color.BLACK);
	            }
	            NewsItem o = items.get(position);
	            if (o != null && 
	            	o.getTitle() != null) 
	            {
	            	Log.d(TAG, "Displaying: " + o.getTitle());
	            	
	                TextView titleView = (TextView) v.findViewById(R.id.news_title);
	                TextView hourView = (TextView) v.findViewById(R.id.news_hour);
	                TextView authorView = (TextView) v.findViewById(R.id.news_author);
	                TextView commentsView = (TextView) v.findViewById(R.id.news_comments);
	                TextView pointView = (TextView) v.findViewById(R.id.news_points);
	                TextView domainView = (TextView) v.findViewById(R.id.news_domain);
	                ImageView imageView = (ImageView) v.findViewById(R.id.news_image);
	                
	                if (!isMoreLink(o.getTitle(), o.getUrl()))
	                {
	                    if (imageView != null && o.getFavIcon() != null)
	                    {
	                    	//lazy load images
	                    	imageLoader.DisplayImage(o.getFavIcon(), mActivity, imageView);
	                    }
	                    
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
	            counter++;
	            return v;
        	} catch (Exception ex)
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
	
	private void downloadData(final String url)
	{
		imageLoader = new ImageLoader(mActivity);
        Runnable newsRetriever = new Runnable(){
            @Override
            public void run() {
            	getNewsItems(url);    	
            }
        };
        
        try
        {
        	m_ProgressDialog = ProgressDialog.show(mActivity, "Downloading", "Please wait...", true);
        } catch (Exception ex)
        {
        	Log.e(TAG, "Exception while creating ProgressDialog: " + ex);
        }
        
        Thread thread =  new Thread(null, newsRetriever, "HackerNewsAPIBackground");
        thread.start();
	}

	public void refreshData(String url) {
		data = new ArrayList<NewsItem>();
		downloadData(url);
	}
}

package com.glebpopov.hackernews.fragments;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.glebpopov.hackernews.R;
import com.glebpopov.hackernews.domain.CommentItem;
import com.glebpopov.hackernews.domain.NewsItem;
import com.glebpopov.hackernews.net.DataParser;
import com.glebpopov.hackernews.util.ImageLoader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CommentsFragment extends ListFragment 
{
	private String TAG = "CommentsFragment";
	private ArrayList<CommentItem> data = new ArrayList<CommentItem>();
	private ProgressDialog m_ProgressDialog = null;
	private CommentsItemAdapter mAdapter = null;
	private NewsItem storyItem = null;
	private Activity mActivity;
	
	public CommentsFragment()
	{
		mActivity = getActivity();
	}
	
	public CommentsFragment(Activity m)
	{
		Log.d(TAG, "New Instance");
		mActivity = m;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        storyItem = (NewsItem) getActivity().getIntent().getSerializableExtra("story_item");
        if (storyItem != null && storyItem.getId() > 0 && storyItem.getTitle() != null)
        {
        	if (mActivity == null)
            {
            	mActivity = getActivity();
            }
            
            if (mActivity == null)
            {
            	Toast.makeText(mActivity, "Something went wrong. Please restart the app.", 50000).show();
            	return;
            }
            
        	//add headerview
        	LayoutInflater vi = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.comments_header_view, null);
        	if (v != null)
        	{
        		TextView storyTitleView = (TextView) v.findViewById(R.id.story_title);
                TextView storyPostedWhenView = (TextView) v.findViewById(R.id.story_hour);
                TextView storyDomainView = (TextView) v.findViewById(R.id.story_domain);
                
            	if (storyTitleView != null && storyItem.getTitle() != null)
                {
                	storyTitleView.setText(storyItem.getTitle());
                	storyTitleView.setVisibility(View.VISIBLE);
                }
                
                if (storyPostedWhenView != null && storyItem.getPostedDate() != null)
                {
                	storyPostedWhenView.setText(storyItem.getPostedDate());
                	storyPostedWhenView.setVisibility(View.VISIBLE);
                }
                
                if (storyDomainView != null && storyItem.getAuthor() != null)
                {
                	storyDomainView.setText(storyItem.getAuthor());
                	storyDomainView.setVisibility(View.VISIBLE);
                }
                
        		Log.d(TAG, "Adding Header View");
        		getListView().addHeaderView(v);
        	}
        	
        	//get data and set listadapter
	        downloadData();
        } else
        {
        	Log.d(TAG, "getComments: invalid story id");
    		setEmptyText("No data returned");
        }
    }
	
	private void downloadData() {
		Runnable newsRetriever = new Runnable(){
            @Override
            public void run() {
            	getComments();    	
            }
        };
        
        m_ProgressDialog = ProgressDialog.show(getActivity(), "Downloading", "Please wait...", true);
        
        Thread thread =  new Thread(null, newsRetriever, "HackerNewsAPIBackgroundComments");
        thread.start();
	}
	
	private void getComments()
	{
		Log.d(TAG, "getComments: getting data. URL: " + getResources().getString(R.string.hn_comments_url) + storyItem.getId());
		
		DataParser d = new DataParser(getResources().getString(R.string.hn_comments_url) + storyItem.getId());
        try
        {
        	data = d.getComments();
        	//try again
        	if (!(data != null && data.size() > 0))
        	{
        		data = d.getComments();
        	}
        } catch (Exception ex)
        {
        	Log.e(TAG, "Error(s) while getting comments: " + ex);
        }
		//check data container
    	getActivity().runOnUiThread(new Runnable() {
            public void run() 
            {
            	if (data != null && data.size() > 0)
            	{
            		Log.d(TAG, "getComments: got " + data.size() + " elements");
            	} else
            	{
            		Log.d(TAG, "getComments: no data returned");
            		setEmptyText("Unable to retrieve data. Please try again.");
            	}
            	mAdapter = new CommentsItemAdapter(getActivity(), R.id.header_news, data);
            	setListAdapter(mAdapter);
            	registerForContextMenu(getListView());
            	m_ProgressDialog.hide();
            } });
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		Log.d(TAG, "onCreateContextMenu");
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		Log.d(TAG, "onContextItemSelected");
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		String[] menuItems = getResources().getStringArray(R.array.comments_menu);
		String menuItemName = menuItems[menuItemIndex];
		Log.d(TAG, "Selected: " + menuItemName);
		
		final CommentItem newsItem = (CommentItem)mAdapter.getItem(info.position);
		if (newsItem != null && newsItem.getReplyId() != null)
		{
			if (menuItemName.equals("Reply"))
			{
				String replyUrl = getResources().getString(R.string.hn_url_reply) + "?id=";
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(replyUrl + newsItem.getReplyId()));
				startActivity(browserIntent);
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
        final CommentItem cursor = (CommentItem)mAdapter.getItem(position);
        final String replyId = cursor.getReplyId();
        
        Log.d(TAG, "onListItemClick: ReplyId: " + replyId);
    }
	    
	private class CommentsItemAdapter extends ArrayAdapter<CommentItem> {

        private ArrayList<CommentItem> items;
        private ArrayList<String> parentChildRegistry = new ArrayList<String>();
        private Context context;

        public CommentsItemAdapter(Context c, int textViewResourceId, ArrayList<CommentItem> items) {
                super(c, textViewResourceId, items);
                context = c;
                this.items = items;
        }
        
        private void addChildItems(CommentItem parent, 
        						   LinearLayout ll)
        {
        	if (parent.getChildren() != null && parent.getChildren().size() > 0)
        	{
        		LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                
        		CommentItem childItem = null;
        		String parentChildKey = null;
        	
	        	for (int i=0;i<parent.getChildren().size();i++)
	            {
	        		childItem = parent.getChildren().get(i);
	        		
	        		parentChildKey = parent.getId() + "_" + childItem.getId();
	        		
	        		if (parentChildRegistry.contains(parentChildKey) ||
	        				parent.getId() != childItem.getParentId()
						)
					{
						break;
					}
	        		
	        		View childView = vi.inflate(R.layout.fragment_comments, null);
	        		TextView commentViewChild = (TextView) childView.findViewById(R.id.comment_comment);
	                TextView hourViewChild = (TextView) childView.findViewById(R.id.comment_whenposted);
	                TextView authorViewChild = (TextView) childView.findViewById(R.id.comment_author);
	            	
	                if (commentViewChild != null) 
	                {
	                	commentViewChild.setText(childItem.getComment()); 
	                	int color = (parent.getColor() == Color.WHITE) ?
	                				Color.LTGRAY : 
	                				Color.WHITE;
	                	commentViewChild.setBackgroundColor(color);
	                	childItem.setColor(color);
	                	Linkify.addLinks(commentViewChild, Linkify.ALL);
	                }
	                if (hourViewChild != null) 
	                {
	                	hourViewChild.setText(childItem.getPostedDate());                           
	                }
	                if (authorViewChild != null) 
	                {
	                	authorViewChild.setText(childItem.getAuthor());                            
	                }
	                
	                ll.addView(childView);
	                
	                LinearLayout cll = (LinearLayout) childView.findViewById(R.id.comments_layout);
	                cll.setPadding(45, 0, 0, 0);
	            	if (cll != null && childItem.getChildren() != null && childItem.getChildren().size() > 0)
	            	{
	            		addChildItems(childItem, cll);
	            	}
	            	
	            	parentChildRegistry.add(parentChildKey);
	            }
        	}
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	Log.d(TAG, "getView: Position" + position);
        	try
        	{ 
        		/*
        		 * View v;
        		LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (convertView == null) {
                	convertView = vi.inflate(R.layout.fragment_comments, null);
                	v = convertView;
                	convertView.setTag(v);
                } else
                {
                	v = (View) convertView.getTag();
                }
        		 */
        		
        		View v ;
        		LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (convertView == null)
        		{
                	v = vi.inflate(R.layout.fragment_comments, null);
        		} else
        		{
        			v = convertView;
        		}
                
                CommentItem o = items.get(position);
                Log.d(TAG, "getView: ID=" + o.getId() + ";Author=" + o.getAuthor() + ";Children Size=" + o.getChildren().size());
                if (o != null && o.getComment() != null && o.getComment().trim().length() > 0) 
                {
                	TextView commentView = (TextView) v.findViewById(R.id.comment_comment);
                    TextView hourView = (TextView) v.findViewById(R.id.comment_whenposted);
                    TextView authorView = (TextView) v.findViewById(R.id.comment_author);
                    LinearLayout ll = (LinearLayout) v.findViewById(R.id.comments_layout);
                    
                    if (commentView != null) 
                    {
                    	commentView.setText(o.getComment()); 
                    	Linkify.addLinks(commentView, Linkify.ALL);
                    }
                    
                    if (ll != null && o.getChildren() != null && o.getChildren().size() > 0)
                    {
                    	o.setColor(Color.WHITE);
                    	addChildItems(o, ll);
                    }
                    
                    if (hourView != null) 
                    {
                    	hourView.setText(o.getPostedDate());                           
                    }
                    if (authorView != null) 
                    {
                    	authorView.setText(o.getAuthor());                            
                    }
                } else
                {
                	Log.d(TAG, "Invalid Comment Object...");
                	setEmptyText("No data returned");
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

	public void refreshData() {
		downloadData();
	}
}

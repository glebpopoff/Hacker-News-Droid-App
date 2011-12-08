package com.glebpopov.hackernews.fragments;

import java.util.ArrayList;

import com.glebpopov.hackernews.R;
import com.glebpopov.hackernews.domain.CommentItem;
import com.glebpopov.hackernews.domain.NewsItem;
import com.glebpopov.hackernews.net.DataParser;
import com.glebpopov.hackernews.util.ImageLoader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CommentsFragmentView extends Fragment 
{
	private String TAG = "CommentsFragment";
	private ArrayList<CommentItem> data = new ArrayList<CommentItem>();
	private ProgressDialog m_ProgressDialog = null;
	private NewsItem storyItem = null;
	private Activity mActivity;
	private View mCommentHeaderView;
	private LayoutInflater mInflator;
	private ImageLoader imageLoader = null;
	protected SharedPreferences sharedPref = null;
	protected boolean isNiteMode = false;
	
	public CommentsFragmentView()
	{
		mActivity = getActivity();
	}
	
	public CommentsFragmentView(Activity m)
	{
		Log.d(TAG, "New Instance");
		mActivity = m;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

		storyItem = (NewsItem) getActivity().getIntent().getSerializableExtra("story_item");
		mInflator = inflater;
		mCommentHeaderView = inflater.inflate(R.layout.comments_header_view, null);
        if (storyItem != null && storyItem.getId() > 0 && storyItem.getTitle() != null)
        {
        	if (mActivity == null)
            {
            	mActivity = getActivity();
            }
            
            if (mActivity == null)
            {
            	Toast.makeText(mActivity, "Something went wrong. Please restart the app.", 50000).show();
            	return null;
            }
            
            if (sharedPref == null)
        	{
        		sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        	}
            
            if (sharedPref.getBoolean("app_nite_mode", false))
            {
            	Log.d(TAG, "App Nite Mode");
        		isNiteMode = true;
        		container.setBackgroundColor(Color.BLACK);
            }
            
        	//add headerview
        	if (mCommentHeaderView != null && storyItem != null)
        	{
        		TextView titleView = (TextView) mCommentHeaderView.findViewById(R.id.news_title);
                TextView hourView = (TextView) mCommentHeaderView.findViewById(R.id.news_hour);
                TextView authorView = (TextView) mCommentHeaderView.findViewById(R.id.news_author);
                TextView commentsView = (TextView) mCommentHeaderView.findViewById(R.id.news_comments);
                TextView pointView = (TextView) mCommentHeaderView.findViewById(R.id.news_points);
                TextView domainView = (TextView) mCommentHeaderView.findViewById(R.id.news_domain);
                
                TextView noDataView = (TextView) mCommentHeaderView.findViewById(R.id.no_data_retrieved);
    			noDataView.setVisibility(View.GONE);
    			
    			if (isNiteMode)
    			{
    				mCommentHeaderView.setBackgroundColor(Color.BLACK);
    			}
    			
    			if (domainView != null && storyItem.getDomain() != null)
                {
                	domainView.setText(storyItem.getDomainReadable());
                	domainView.setOnClickListener(new View.OnClickListener() {
        	            public void onClick(View v) {
        	            	final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(storyItem.getUrl()));
        					startActivity(browserIntent);
        	            }
        	        });
                	if (isNiteMode)
                	{
                		domainView.setBackgroundColor(Color.TRANSPARENT);
                		domainView.setTextAppearance(mActivity, R.style.DomainTextInverse);
                	}
                }
                
                if (titleView != null) 
                {
                	titleView.setText(storyItem.getTitle());    
                	titleView.setOnClickListener(new View.OnClickListener() {
        	            public void onClick(View v) {
        	            	final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(storyItem.getUrl()));
        					startActivity(browserIntent);
        	            }
        	        });
                	if (isNiteMode)
                	{
                		titleView.setTextAppearance(mActivity, R.style.TextHeaderInverse);
                	}
                }
                
                if (hourView != null && storyItem.getPostedDate() != null) 
                {
                	hourView.setText(storyItem.getPostedDate());    
                	if (isNiteMode)
                	{
                		hourView.setTextColor(Color.LTGRAY);
                	}
                }
                
                if (authorView != null && storyItem.getAuthor() != null) 
                {
                	authorView.setText(storyItem.getAuthor() + " | ");
                	if (isNiteMode)
                	{
                		authorView.setTextColor(Color.LTGRAY);
                	}
                }
                
                if (commentsView != null && storyItem.getComments() != null) 
                {
                	commentsView.setText(storyItem.getComments());
                	if (isNiteMode)
                	{
                		commentsView.setTextColor(Color.LTGRAY);
                	}
                }
                
                if (pointView != null && storyItem.getPoints() != null) 
                {
                	pointView.setText(storyItem.getPoints()); 
                	if (isNiteMode)
                	{
                		pointView.setTextColor(Color.LTGRAY);
                	}
                }
                
        		Log.d(TAG, "Adding Header View");
        		
        	}
        	
        	//get data and build the view
	        downloadData();
	        
	        if (mCommentHeaderView != null && storyItem.getFavIcon() != null && imageLoader != null)
            {
	        	ImageView imageView = (ImageView) mCommentHeaderView.findViewById(R.id.news_image);
	        	if (imageView != null)
	        	{
	        		//lazy load images
	        		imageLoader.DisplayImage(storyItem.getFavIcon(), mActivity, imageView);
	        	}
            }
        } else
        {
        	Log.d(TAG, "getComments: invalid story id");
    		setEmptyText("No data returned");
        }
        
        return mCommentHeaderView;
    }
	
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if (isNiteMode)
    	{
    		getView().setBackgroundColor(Color.BLACK);
    	}
    }
	
	private void downloadData() {
		imageLoader = new ImageLoader(mActivity);
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
            	if (data != null && data.size() > 0 && mInflator != null && mCommentHeaderView != null)
            	{
            		Log.d(TAG, "getComments: got " + data.size() + " elements");
            		
            		LinearLayout ll = (LinearLayout) mCommentHeaderView.findViewById(R.id.comments_header_layout);
            		
            		if (isNiteMode)
            		{
            			ll.setBackgroundColor(Color.BLACK);
            		}
            		
                	for (int i=0;i<data.size();i++)
                	{
                		CommentItem o = data.get(i);
	                    if (o != null && o.getComment() != null && o.getComment().trim().length() > 0) 
	                    {
	                    	View childView = mInflator.inflate(R.layout.fragment_comments, null);
	                    	TextView commentView = (TextView) childView.findViewById(R.id.comment_comment);
	                        TextView hourView = (TextView) childView.findViewById(R.id.comment_whenposted);
	                        TextView authorView = (TextView) childView.findViewById(R.id.comment_author);
	                        LinearLayout llChild = (LinearLayout) childView.findViewById(R.id.comments_layout);
	                        
	                        if (isNiteMode)
	                    	{
	                        	childView.setBackgroundColor(Color.BLACK);
	                    	}
	                        
	                        if (commentView != null) 
	                        {
	                        	commentView.setText(o.getComment()); 
	                        	Linkify.addLinks(commentView, Linkify.ALL);
	                        	if (isNiteMode)
	                        	{
	                        		commentView.setTextColor(Color.WHITE);
	                        	}
	                        }
	                        
	                        if (ll != null && o.getChildren() != null && o.getChildren().size() > 0)
	                        {
	                        	if (isNiteMode)
	                        	{
	                        		o.setColor(Color.BLACK);
	                        	} else
                        		{
                        			o.setColor(Color.WHITE);
                        		}
	                        	addChildItems(o, llChild);
	                        }
	                        
	                        if (hourView != null) 
	                        {
	                        	hourView.setText(o.getPostedDate());                           
	                        }
	                        if (authorView != null) 
	                        {
	                        	authorView.setText(o.getAuthor());                            
	                        }
	                        
	                        ll.addView(childView);
	                    } else
	                    {
	                    	Log.d(TAG, "Invalid Comment Object...");
	                    	setEmptyText("No data returned");
	                    }   
                	}
                    
            	} else
            	{
            		Log.d(TAG, "getComments: no data returned");
            		setEmptyText("Unable to retrieve data. Please try again.");
            	}
            	
            	m_ProgressDialog.hide();
            } });
	}

	private void setEmptyText(String s)
	{
		if (mCommentHeaderView != null)
    	{
			TextView noDataView = (TextView) mCommentHeaderView.findViewById(R.id.no_data_retrieved);
			noDataView.setVisibility(View.VISIBLE);
    	}
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

    private void addChildItems(CommentItem parent, 
    						   LinearLayout ll)
    {
    	if (parent.getChildren() != null && parent.getChildren().size() > 0 && mInflator != null)
    	{
    		CommentItem childItem = null;
    		
        	for (int i=0;i<parent.getChildren().size();i++)
            {
        		childItem = parent.getChildren().get(i);
        		
        		View childView = mInflator.inflate(R.layout.fragment_comments, null);
        		TextView commentViewChild = (TextView) childView.findViewById(R.id.comment_comment);
                TextView hourViewChild = (TextView) childView.findViewById(R.id.comment_whenposted);
                TextView authorViewChild = (TextView) childView.findViewById(R.id.comment_author);
            	
                if (commentViewChild != null) 
                {
                	commentViewChild.setText(childItem.getComment()); 
                	int color = -1;
                	if (isNiteMode)
                	{
                		commentViewChild.setBackgroundColor(Color.BLACK);
                		if (parent.getColor() == Color.BLACK)
                		{
                			commentViewChild.setTextAppearance(mActivity, R.style.TextCommentInverse);
                			childItem.setColor(Color.WHITE);
                		} else
                		{
                			commentViewChild.setTextColor(Color.WHITE);
                			childItem.setColor(Color.BLACK);
                		}
                	} else
            		{
                		color = (parent.getColor() == Color.WHITE) ?
                				Color.LTGRAY : 
                				Color.WHITE;
                		commentViewChild.setBackgroundColor(color);
                    	childItem.setColor(color);
            		}
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
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                Log.d(TAG, "Screen Width: " + displaymetrics.widthPixels);
                cll.setPadding(30, 0, 0, 0);
            	if (cll != null && childItem.getChildren() != null && childItem.getChildren().size() > 0)
            	{
            		addChildItems(childItem, cll);
            	}
            	
            }
    	}
    }
        
        

	public void refreshData() {
		downloadData();
	}
}

package com.glebpopov.hackernews.fragments;

import java.util.ArrayList;

import com.glebpopov.hackernews.R;
import com.glebpopov.hackernews.domain.CommentItem;
import com.glebpopov.hackernews.domain.NewsItem;
import com.glebpopov.hackernews.net.DataParser;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            
        	//add headerview
        	if (mCommentHeaderView != null)
        	{
        		TextView storyTitleView = (TextView) mCommentHeaderView.findViewById(R.id.story_title);
                TextView storyPostedWhenView = (TextView) mCommentHeaderView.findViewById(R.id.story_hour);
                TextView storyDomainView = (TextView) mCommentHeaderView.findViewById(R.id.story_domain);
                TextView noDataView = (TextView) mCommentHeaderView.findViewById(R.id.no_data_retrieved);
    			noDataView.setVisibility(View.GONE);
    			
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
        		
        	}
        	
        	//get data and build the view
	        downloadData();
        } else
        {
        	Log.d(TAG, "getComments: invalid story id");
    		setEmptyText("No data returned");
        }
        
        return mCommentHeaderView;
    }
	
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
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
            	if (data != null && data.size() > 0 && mInflator != null && mCommentHeaderView != null)
            	{
            		Log.d(TAG, "getComments: got " + data.size() + " elements");
            		
            		LinearLayout ll = (LinearLayout) mCommentHeaderView.findViewById(R.id.comments_header_layout);
            		
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
	                        
	                        if (commentView != null) 
	                        {
	                        	commentView.setText(o.getComment()); 
	                        	Linkify.addLinks(commentView, Linkify.ALL);
	                        }
	                        
	                        if (ll != null && o.getChildren() != null && o.getChildren().size() > 0)
	                        {
	                        	o.setColor(Color.WHITE);
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
            	
            }
    	}
    }
        
        

	public void refreshData() {
		downloadData();
	}
}

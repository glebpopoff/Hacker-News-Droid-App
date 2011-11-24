package com.glebpopov.hackernews.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import com.glebpopov.hackernews.util.StringEscapeUtils;

import android.text.Html;
import android.util.Log;

public class CommentItem 
{
	private String TAG = "CommentItem";
	private String postedDate, author, replyId, comment;
	private int id;
	private int parentID;
	private int color;
	private ArrayList<CommentItem> childrenContainer;
	
	public int getParentId()
	{
		return parentID;
	}
	
	public void setParentId(int i)
	{
		parentID = i;
	}
	
	public int getColor()
	{
		return color;
	}
	
	public void setColor(int i)
	{
		color = i;
	}
	
	public String getPostedDate() {
        return postedDate;
    }
    
    public void setPostedDate(String t) {
        this.postedDate = t;
    }
    
    public String getReplyId() {
        return replyId;
    }
    
    public void setReplyId(String t) {
        this.replyId = t;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String t) {
        this.author = t;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int t) {
        this.id = t;
    }
    
    public ArrayList<CommentItem> getChildren()
    {
    	return childrenContainer;
    }
    
    public void setChildren(ArrayList<CommentItem> container)
    {
    	childrenContainer = container;
    }
    
    /**
     * Returns actual comment text
     * The API doesn't return clean text (blame python, my dev skills, appengine, etc)
     * as a result we need do lots of searching & replacing to clean that up
     * We also need to replace poorman's break characters with \n
     */
    public String getComment() {
        if (comment != null)
        {
        	if (comment.contains("__BR__"))
        	{
        		comment = comment.replaceAll("__BR__", "\n\n");
        	}
        	
			try
			{
				comment = StringEscapeUtils.unescapeHtml(comment);
			} catch (Exception e) 
			{
				Log.w(TAG, "Exception(s) unescapeHtml in comment: " + e);
			}
			
			try {
				comment = comment.replace("\\", "");
				comment = comment.replace("&#62;", ">");
				comment = comment.replace("&#38;", "&");
				comment = comment.replace("&#60;", "<");
				comment = comment.replace("&euro;&trade;", "'");
			} catch (Exception e) 
			{
				Log.w(TAG, "Exception(s) replacing char in comment: " + e);
			}

			try {
				comment = URLDecoder.decode(comment, "UTF-8");
			} catch (Exception e) 
			{
				Log.w(TAG, "Exception(s) decoding comment: " + e);
			}
        }
        return comment;
    }
    
    public void setComment(String t) {
        this.comment = t;
    }
}

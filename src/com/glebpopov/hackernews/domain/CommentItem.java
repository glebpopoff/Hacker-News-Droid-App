package com.glebpopov.hackernews.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

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
	
	public int getParentID()
	{
		return parentID;
	}
	
	public void setParentID(int i)
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
    
    public String getComment() {
        if (comment != null)
        {
        	//Log.d(TAG, "Comment:\n" + comment);
        	
        	if (comment.contains("__BR__"))
        	{
        		comment = comment.replaceAll("__BR__", "\n\n");
        	}
        	
        	try {
				comment = URLDecoder.decode(comment, "UTF-8");
			} catch (Exception e) 
			{
				Log.w(TAG, "Exception(s) decoding comment: " + e);
			}
			try {
				comment = comment.replace("\\", "");
				comment = comment.replace("&amp;#62;", ">");
			} catch (Exception e) 
			{
				Log.w(TAG, "Exception(s) replacing char in comment: " + e);
			}
        }
        return comment;
    }
    
    public void setComment(String t) {
        this.comment = t;
    }
}

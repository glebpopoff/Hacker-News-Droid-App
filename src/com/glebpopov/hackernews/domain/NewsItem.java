package com.glebpopov.hackernews.domain;

import java.io.Serializable;

import android.text.Html;

public class NewsItem implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String commentNum, points, title, url, postedDate, author, domain, defaultDomain = "http://news.ycombinator.org";
	private int id, systemId;
	private boolean isHNUrl;

	public String getTitle() {
        return (title != null) ? Html.fromHtml(title).toString().replaceAll("[^\\p{ASCII}]", "").trim() : null;
    }
	
    public void setTitle(String t) {
        this.title = t;
    }
    
    public String getDomain() {
    	return domain;
    }
    
    public boolean isHNUrl()
    {
    	return isHNUrl;
    }
    
    public String getDomainReadable() {
    	if (domain != null)
    	{
    		return domain.replaceAll("http://", "").replaceAll("https://", "").replaceAll("www.", "");
    	}
    	return domain;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String t) {
    	//get domain from URL
    	if (t != null)
    	{
    		//external domain
    		if (t.startsWith("http://") || t.startsWith("https://"))
    		{
    			domain = (t.startsWith("http://")) ? t.substring(7) : t.substring(8);
    			if (domain.contains("/"))
	    		{
	    			domain = domain.substring(0, domain.indexOf('/'));
	    		}
    		} else
    		{
    			//most likely HN url
    			domain = defaultDomain;
    			if (t.startsWith("/"))
    			{
    				t = domain + t;
    			} else
    			{
    				t = domain + "/" + t;
    			}
    			
    			if (t.contains("news.ycombinator.org"))
    			{
    				isHNUrl = true;
    			}
    		}
    		this.url = t;
    	}
    }
    
    public int getSystemId() {
        return systemId;
    }
    
    public void setSystemId(int t) {
        this.systemId = t;
    }
    
    public String getPostedDate() {
        return postedDate;
    }
    
    public void setPostedDate(String t) {
        this.postedDate = t;
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
    
    public String getComments() {
        return commentNum;
    }
    
    public void setComments(String t) {
        this.commentNum = t;
    }
    
    public String getPoints() {
        return points;
    }
    
    public void setPoints(String t) {
        this.points = t;
    }

	public String getFavIcon() {
		return "http://g.etfv.co/http://" + domain;
	}
}

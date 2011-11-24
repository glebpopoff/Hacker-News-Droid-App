package com.glebpopov.hackernews.domain;

import java.util.ArrayList;

public class NewsContainer 
{
	private ArrayList<NewsItem> newsContainer = null;
	private NewsItem moreLink = null;
	
	public void setNewsContainer(ArrayList<NewsItem> n)
	{
		newsContainer = n;
	}
	
	public ArrayList<NewsItem> getNewsContainer()
	{
		return newsContainer;
	}
	
	public void setMoreNewsLink(NewsItem n)
	{
		moreLink = n;
	}
	
	public NewsItem getMoreNewsLink()
	{
		return moreLink;
	}
}

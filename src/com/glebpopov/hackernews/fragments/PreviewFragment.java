package com.glebpopov.hackernews.fragments;

import com.glebpopov.hackernews.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class PreviewFragment extends Fragment 
{
	private String TAG = "PreviewFragment";
	private String resourceUrl = null;
	private WebView mWebView;
    private View mLoadingSpinner;
	
	public PreviewFragment()
	{
		Log.d(TAG, "New Instance");
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resourceUrl = getActivity().getIntent().getStringExtra("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.preview_dialog, null);
        final TextView mNoDataView = (TextView) root.findViewById(R.id.preview_nodata);
        mWebView = (WebView) root.findViewById(R.id.webview);
        
        if (resourceUrl != null && (
        							resourceUrl.toLowerCase().startsWith("http://") || 
        							resourceUrl.toLowerCase().startsWith("https://")
        							))
    	{
	        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
	        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
	        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
	                ViewGroup.LayoutParams.FILL_PARENT));
	
	        mLoadingSpinner = root.findViewById(R.id.loading_spinner);
	        
	        mWebView.setWebViewClient(mWebViewClient);
	
	        mWebView.post(new Runnable() {
	            public void run() {
	                mWebView.getSettings().setJavaScriptEnabled(true);
	                mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
	                try {
	                    mWebView.loadUrl("http://viewtext.org/article?url=" + resourceUrl);
	                } catch (Exception e) {
	                    Log.e(TAG, "Could not construct the URL", e);
	                    mNoDataView.setVisibility(View.VISIBLE);
	                    mWebView.setVisibility(View.GONE);
	                    Toast.makeText(getActivity(), "Unable to load page", Toast.LENGTH_SHORT).show();
	                }
	            }
	        });
    	} else
    	{
    		mNoDataView.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
    	}

        return root;
    }

    public void refresh() {
        mWebView.reload();
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mLoadingSpinner.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mLoadingSpinner.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
        }

        //opens a new window
        /*
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("javascript")) {
                return false;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }*/
    };
    
    @Override
    public void onResume() {
        super.onResume();
        
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
    }
	
	
	/*
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		String url = getActivity().getIntent().getStringExtra("url");
		if (url != null && url.startsWith("http://"))
		{
			Log.d(TAG, "Dispalying URL: " + url);
			mWebView = new WebView(getActivity());
			mWebView.setWebChromeClient(new WebChromeClient() {
				 public void onProgressChanged(WebView view, int progress)   
				 {
				  //Make the bar disappear after URL is loaded, and changes string to Loading...
				  mActivity.setTitle("Loading...");
				  mActivity.setProgress(progress * 100); //Make the bar disappear after URL is loaded
				 
				  // Return the app name after finish loading
				     if(progress == 100)
				     {mActivity.setTitle(R.string.app_name);
				   }
				 });
			
		    mWebView.getSettings().setJavaScriptEnabled(true);
		    mWebView.loadUrl("http://viewtext.org/article?url=" + url);
		    return mWebView;
		} else
		{
			return null;
		}
    }
	
	
 
	
	
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		String url = getActivity().getIntent().getStringExtra("url");
		if (url != null && url.startsWith("http://"))
		{
			Log.d(TAG, "Dispalying URL: " + url);
			mWebView = new WebView(getActivity());
			mWebView.setWebChromeClient(new WebChromeClient() {
				 public void onProgressChanged(WebView view, int progress)   
				 {
				  //Make the bar disappear after URL is loaded, and changes string to Loading...
				  mActivity.setTitle("Loading...");
				  mActivity.setProgress(progress * 100); //Make the bar disappear after URL is loaded
				 
				  // Return the app name after finish loading
				     if(progress == 100)
				     {mActivity.setTitle(R.string.app_name);
				   }
				 });
			
		    mWebView.getSettings().setJavaScriptEnabled(true);
		    mWebView.loadUrl("http://viewtext.org/article?url=" + url);
		    return mWebView;
		} else
		{
			return null;
		}
    }
	
	@Override
    public void onResume() {
        super.onResume();
        
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
    }
 	*/
    
}

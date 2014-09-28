package com.example.msample.ui.activity;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.example.msample.R;

public class ContactDetailWebview extends Activity{

	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_detail_layout);
		
		mWebView = (WebView)findViewById(R.id.web);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(new JSInterface(), "JSInterface");
		
		mWebView.loadUrl("file:///android_asset/contactDetail.html");
		final int pos = getIntent().getExtras().getInt("pos");
		
		String name = MainActivity.sContactList.get(pos).getName();
		String jsNameString = "javascript:updateName('" + name + "');";
		mWebView.loadUrl(jsNameString);
		
		String phone = MainActivity.sContactList.get(pos).getPhone();
		String jsPhoneString = "javascript:updatePhone('" + phone + "');";
		mWebView.loadUrl(jsPhoneString);
		
		String email = MainActivity.sContactList.get(pos).getEmail();
		String jsEmailString = "javascript:updateEmail('" + email + "');";
		mWebView.loadUrl(jsEmailString);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String name = MainActivity.sContactList.get(pos).getName();
				String jsNameString = "javascript:updateName('" + name + "');";
				mWebView.loadUrl(jsNameString);
				
				String phone = MainActivity.sContactList.get(pos).getPhone();
				String jsPhoneString = "javascript:updatePhone('" + phone + "');";
				mWebView.loadUrl(jsPhoneString);
				
				String email = MainActivity.sContactList.get(pos).getEmail();
				String jsEmailString = "javascript:updateEmail('" + email + "');";
				mWebView.loadUrl(jsEmailString);
			}
		}, 500);
	}
	
	
	class JSInterface{
		@JavascriptInterface
		public void getRandomAddress(){
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					int count = MainActivity.sContactList.size();
					Random random = new Random();
					int pos = random.nextInt(count);
					Log.d("vineet", "Before " + pos);
					if (pos == count)
						pos = pos -1;
					
					Log.d("vineet", "After " + pos);
					String name = MainActivity.sContactList.get(pos).getName();
					String jsNameString = "javascript:updateName('" + name + "');";
					mWebView.loadUrl(jsNameString);
					
					String phone = MainActivity.sContactList.get(pos).getPhone();
					String jsPhoneString = "javascript:updatePhone('" + phone + "');";
					mWebView.loadUrl(jsPhoneString);
					
					String email = MainActivity.sContactList.get(pos).getEmail();
					String jsEmailString = "javascript:updateEmail('" + email + "');";
					mWebView.loadUrl(jsEmailString);
				}
			});
		}
		
		@JavascriptInterface
		public void exit(){
			finish();
		}
	}
}

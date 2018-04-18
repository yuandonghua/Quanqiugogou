package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class KuaidiActivity extends Activity {
	
	private WebView webview = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		
		webview = (WebView)findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new WebViewClient(){
			
			ProgressDialog pBar = ProgressDialog.show(KuaidiActivity.this, null, "正在加载...");

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);   
				return super.shouldOverrideUrlLoading(view, url);
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				pBar.show();  
				super.onPageStarted(view, url, favicon);
			} 
		  
		    @Override  
		    public void onPageFinished(WebView view, String url) {  
		    	pBar.dismiss();
		    	super.onPageFinished(view, url);  
		    }
			
		});
		
		String code = getIntent().getStringExtra("code");
		String id = getIntent().getStringExtra("id");
		webview.loadUrl("http://m.kuaidi100.com/index_all.html?type=" + code + "&postid=" + id);
	}
	
}
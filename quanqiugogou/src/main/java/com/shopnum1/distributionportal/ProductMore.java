package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProductMore extends Activity {
	
	private WebView webview = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.product_more);
		initLayout();
	}
	//初始化
	public void initLayout() {
		//返回
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		final ProgressDialog pBar = ProgressDialog.show(ProductMore.this, null, "正在加载...");
		webview = (WebView)findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new WebViewClient(){
			
			

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
		
		String MobileDetail = getIntent().getStringExtra("MobileDetail");
		if(MobileDetail == null || ("").equals(MobileDetail) || MobileDetail.equals("null")){
			pBar.dismiss();
			webview.setVisibility(View.GONE);
			((TextView)findViewById(R.id.nocontent)).setVisibility(View.VISIBLE);
		} else {
			webview.loadDataWithBaseURL("", MobileDetail, "text/html", "utf-8", "");
		}
		
	}

}
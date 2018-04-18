package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AlipayActivity extends Activity {
	
	private WebView webview = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);     
		webview = (WebView)findViewById(R.id.webview);
		webview.addJavascriptInterface(new MyObject(this), "MyObject");//支付完成后还是在html5界面
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new WebViewClient(){
			
			ProgressDialog pBar = ProgressDialog.show(AlipayActivity.this, null, "正在加载...");

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
		String order = getIntent().getStringExtra("order");
		String total = getIntent().getStringExtra("total");
		if(total.indexOf("元")!=-1){
			total=total.substring(0, total.indexOf("元"));
		}
		if(getIntent().getStringExtra("source")!=null){
			if(getIntent().getStringExtra("source").equals("PayModeActivity")){//代表是从商品的那里下单来的
				webview.loadUrl(HttpConn.hostName + "/PayReturn/ZFPay/alipay/payment.aspx?out_trade_no=" + order + "&subject=订单" + order + "&total_fee=" + total); 
			}
		}else{
			webview.loadUrl(HttpConn.hostName + "/PayReturn/CZPay/alipay/recharge.aspx?out_trade_no=" + order + "&subject=订单" + order + "&total_fee=" + total); 
		}
		
		
	}
	public class MyObject {
		private Context mContext;
		public MyObject(Context mContext) {
			this.mContext = mContext;
		}
		@JavascriptInterface
		public void  startMainActivity(){
			Intent mIntent = new Intent(AlipayActivity.this, MainActivity.class);
			mContext.startActivity(mIntent);
		} 
	}
	@Override
	public void onBackPressed() {
		Intent intent = new Intent(new Intent(getApplicationContext(), OrderActivity.class));
		intent.putExtra("type", 0);
		intent.putExtra("title", "全部订单");
		startActivity(intent);
		finish();
		super.onBackPressed();
	}
	
}
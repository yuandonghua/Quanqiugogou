package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AlipayRechargeActivity extends Activity {
	
	private WebView webview = null;
	private Dialog pBar; //加载进度

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);     
		
		pBar = new Dialog(AlipayRechargeActivity.this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		
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
		
		String order = getIntent().getStringExtra("order");
		String total = getIntent().getStringExtra("total");
		if(getIntent().getStringExtra("source")!=null){
			if(getIntent().getStringExtra("source").equals("PayModeActivity")){//代表是从商品的那里下单来的
				webview.loadUrl(HttpConn.hostName + "/PayReturn/ZFPay/alipay/payment.aspx?out_trade_no=" + order + "&subject=订单" + order + "&total_fee=" + total); 
			}
		}else{
			webview.loadUrl(HttpConn.hostName + "/PayReturn/CZPay/alipay/recharge.aspx?out_trade_no=" + order + "&subject=订单" + order + "&total_fee=" + total); 
		}
	
	
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(new Intent(getApplicationContext(), PaymentDetail.class)));
		RechargeActivity.rechargeActivity.finish();
		finish();
		super.onBackPressed();
	}
	
}
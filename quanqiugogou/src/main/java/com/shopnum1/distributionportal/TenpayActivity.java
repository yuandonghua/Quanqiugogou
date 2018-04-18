package com.shopnum1.distributionportal;

import java.net.URLEncoder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
public class TenpayActivity extends Activity {

	private WebView webview = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		webview = (WebView) findViewById(R.id.webview);
		webview.addJavascriptInterface(new MyObject(this), "MyObject");
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new WebViewClient() {

			ProgressDialog pBar = ProgressDialog.show(TenpayActivity.this,
					null, "正在加载...");

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

		String order = getIntent().getStringExtra("order_no");
		String total = getIntent().getStringExtra("order_price");
		if(total.indexOf("元")!=-1){
			total=total.substring(0, total.indexOf("元"));
		}		
		if(getIntent().getStringExtra("source")!=null){
			if(getIntent().getStringExtra("source").equals("RechargeActivity")){//代表是从商品的那里下单来的
				webview.loadUrl(HttpConn.hostName + "/PayReturn/CZPay/tenpay/CZ_payRequest.aspx?order_no="+ order + "&order_price=" + total+"&product_name=1"+"&remarkexplain=1");
			}
		}else{
			webview.loadUrl(HttpConn.hostName + "/PayReturn/ZFPay/tenpay/payRequest.aspx?order_no="+ order + "&order_price=" + total+"&product_name=1"+"&remarkexplain=1");
		}
	}

	public class MyObject {

		private Context mContext;

		public MyObject(Context mContext) {
			this.mContext = mContext;
		}

		@JavascriptInterface
		public void startMainActivity() {
			Intent mIntent = new Intent(TenpayActivity.this, MainActivity.class);
			mContext.startActivity(mIntent);
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(new Intent(getApplicationContext(),
				OrderActivity.class));
		intent.putExtra("type", 0);
		intent.putExtra("title", "全部订单");
		startActivity(intent);
		finish();
		super.onBackPressed();
	}

}

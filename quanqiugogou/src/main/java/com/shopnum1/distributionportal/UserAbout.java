package com.shopnum1.distributionportal;
//关于

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
public class UserAbout extends Activity {
	
	private HttpConn httpget = new HttpConn();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_about);
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
		//快捷方式
		((LinearLayout)findViewById(R.id.more)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				httpget.showMenu(UserAbout.this, findViewById(R.id.user_about));
			}
		});
		
		String str = "\t\t分销商城一站式采购，通过整合本地商家资源，旨在连接上游供货商和下游分销商，以PC+APP为通道，帮助店铺解决采购难题，一站式打包采购进货，建立同城批发采购交易平台。";
		CharSequence text = Html.fromHtml(str);
		((TextView) findViewById(R.id.tv1)).setText(text);
	}

}
package com.shopnum1.distributionportal;
//用户协议
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.app.Activity;
import com.shopnum1.distributionportal.R;
public class UserAgree extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_agree);
		
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
	}

}
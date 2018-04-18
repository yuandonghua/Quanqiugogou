package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyMessageDetail extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mymsg_detail);
		initData();
	}

	private void initData() {

		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setResult(1, getIntent());
				finish();
			}
		});

		((TextView)findViewById(R.id.title)).setText(getIntent().getStringExtra("title"));
		((TextView)findViewById(R.id.time)).setText(getIntent().getStringExtra("time"));
		((TextView)findViewById(R.id.content)).setText(getIntent().getStringExtra("content"));
	}
	
	@Override
	public void onBackPressed() {
		setResult(1, getIntent());
		finish();
		super.onBackPressed();
	}
	
}
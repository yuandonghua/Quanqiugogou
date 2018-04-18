package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
//使用积分
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class UseScore extends Activity {

	private HttpConn httpget = new HttpConn();
	private int Score, CanByScores;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.use_score);
		initLayout();
	}
	// 初始化
	public void initLayout() {

		// 返回
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		CanByScores = getIntent().getIntExtra("CanByScores", 0);
		getData();
	}
	// 获取用户信息
	public void getData() {
		new Thread() {
			public void run() {
				try {
					StringBuffer result = httpget.getArray("/api/accountget/?MemLoginID="
									+ HttpConn.username + "&AppSign="
									+ HttpConn.AppSign);
					Score = new JSONObject(result.toString()).getJSONObject("AccoutInfo").getInt("Score");
					if(CanByScores > Score){
						CanByScores = Score;
					}	
					Message msg = Message.obtain();
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			setData();
			super.handleMessage(msg);
		}
	};

	public void setData(){
		((TextView)findViewById(R.id.score)).setText("你有" + Score + "积分，可用" + CanByScores);
		if(CanByScores > Score){
			CanByScores = Score;
		}
		((Button)findViewById(R.id.post)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String number = ((EditText)findViewById(R.id.editText)).getText().toString();
				if(number.equals("")){
					Toast.makeText(getApplicationContext(), "请输入积分", Toast.LENGTH_SHORT).show();
				} else if(CanByScores == 0){
					Toast.makeText(getApplicationContext(), "不支持积分抵用", Toast.LENGTH_SHORT).show();
				} else {
					int UseScore = Integer.parseInt(number);
					if(UseScore > Score){
						Toast.makeText(getApplicationContext(), "可用积分不足" + UseScore, Toast.LENGTH_SHORT).show();
					} else if(UseScore > CanByScores){
						Toast.makeText(getApplicationContext(), "最多可使用积分" + CanByScores, Toast.LENGTH_SHORT).show();
					} else {
						Intent intent = getIntent();
						intent.putExtra("UseScore", UseScore);
						intent.putExtra("CanByScores", CanByScores);
						setResult(3, intent);
						finish();
					}
				}
			}
		});
	}
	
}
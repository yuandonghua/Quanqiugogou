package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class RefundGoods2 extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private Dialog pBar; //加载进度

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.refund_goods2);
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
				httpget.showMenu(RefundGoods2.this, findViewById(R.id.refund_goods2));
			}
		});
		
		((Button) findViewById(R.id.button)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final String MainDistribution = ((EditText)findViewById(R.id.edit)).getText().toString();
				final String StreamOrder = ((EditText)findViewById(R.id.edit2)).getText().toString();
				if(MainDistribution.equals("") || StreamOrder.equals("")){
					Toast.makeText(getApplicationContext(), "请填写物流信息", Toast.LENGTH_SHORT).show();
				} else {
					pBar = new Dialog(RefundGoods2.this, R.style.dialog);
					pBar.setContentView(R.layout.progress);
					pBar.show();
					
					new Thread(){
						public void run(){
							JSONObject object = new JSONObject();
							try {
								object.put("Guid", getIntent().getStringExtra("ReturnGuid"));
								object.put("MainDistribution", MainDistribution);
								object.put("StreamOrder", StreamOrder);
								object.put("ReturnGoodsCause", getIntent().getStringExtra("ReturnGoodsCause"));
								object.put("OperateUserID", HttpConn.UserName);
								object.put("AgentID", MyApplication.agentId);
								object.put("AppSign", HttpConn.AppSign);
								StringBuffer result = httpget.postJSON("/api/updatereturngoodsinfo/", object.toString());
								Log.i("fly", result.toString());
								Message msg = Message.obtain();
								msg.obj = new JSONObject(result.toString()).getString("return");
								handler.sendMessage(msg);
							} catch (JSONException e) {
								e.printStackTrace();
							}	
						}
					}.start();
				}
			}
		});
		
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			pBar.dismiss();
			if(msg.obj.equals("202")){
				Toast.makeText(getApplicationContext(), "申请成功", Toast.LENGTH_SHORT).show();	
			} else {
				Toast.makeText(getApplicationContext(), "申请失败", Toast.LENGTH_SHORT).show();
			}
			setResult(1, getIntent());
			finish();
			super.handleMessage(msg);
		}
		
	};

}
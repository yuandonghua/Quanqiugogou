package com.shopnum1.distributionportal;
//登录
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
public class UserLoginPwd extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private Boolean showpassword = false; //是否显示明文密码
	private Boolean showpassword2 = false; //是否显示明文密码

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_login_pwd);
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
		//密码
		((ImageView) findViewById(R.id.showpassword)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!showpassword) {
					showpassword = true;
					((ImageView) findViewById(R.id.showpassword)).setBackgroundResource(R.drawable.password_show1);
					((EditText)findViewById(R.id.pwd)).setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else {
					showpassword = false;
					((ImageView) findViewById(R.id.showpassword)).setBackgroundResource(R.drawable.password_show);
					((EditText)findViewById(R.id.pwd)).setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				
			}
		});
		((ImageView) findViewById(R.id.showpassword2)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!showpassword2) {
					showpassword2 = true;
					((ImageView) findViewById(R.id.showpassword2)).setBackgroundResource(R.drawable.password_show1);
					((EditText)findViewById(R.id.pwd2)).setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else {
					showpassword2 = false;
					((ImageView) findViewById(R.id.showpassword2)).setBackgroundResource(R.drawable.password_show);
					((EditText)findViewById(R.id.pwd2)).setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				
			}
		});
		//旧密码
		((EditText)findViewById(R.id.pwd)).setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean isFocused) {
				if(isFocused)
					((ImageView)findViewById(R.id.password_icon)).setBackgroundResource(R.drawable.password1);
				else
					((ImageView)findViewById(R.id.password_icon)).setBackgroundResource(R.drawable.password);
			}
		});
		
		//新密码
		((EditText)findViewById(R.id.pwd2)).setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean isFocused) {
				if(isFocused)
					((ImageView)findViewById(R.id.password_icon2)).setBackgroundResource(R.drawable.password1);
				else
					((ImageView)findViewById(R.id.password_icon2)).setBackgroundResource(R.drawable.password);
			}
		});
		//修改
		((Button) findViewById(R.id.login)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final String pwd = ((EditText)findViewById(R.id.pwd)).getText().toString();
				final String pwd2 = ((EditText)findViewById(R.id.pwd2)).getText().toString();
					if(!pwd.equals("") && !pwd2.equals("")){
						new Thread(){
							@Override
							public void run(){	
								try {
									StringBuffer result = httpget.getArray("/api/updateloginpwd/?MemLoginID=" + HttpConn.username + "&oldPwd=" + pwd + "&newPwd=" + pwd2 + "&AppSign=" + HttpConn.AppSign);
									Message msg = Message.obtain();
									msg.obj = new JSONObject(result.toString()).getString("return");
									handler.sendMessage(msg);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}.start();
					} else {
						Toast.makeText(getApplicationContext(), "请输入完整密码", Toast.LENGTH_SHORT).show();
					}
				}
		});
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.obj.toString().equals("202")){
				
				Toast.makeText(UserLoginPwd.this, "修改成功", Toast.LENGTH_SHORT).show();
				setResult(1, getIntent());
				finish();
			} else {
				Toast.makeText(UserLoginPwd.this, "原密码错误", Toast.LENGTH_SHORT).show();
			}
			
			super.handleMessage(msg);
		}
		
	};

}
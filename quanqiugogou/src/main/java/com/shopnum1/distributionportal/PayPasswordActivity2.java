package com.shopnum1.distributionportal;
//登录
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class PayPasswordActivity2 extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private boolean showpassword = false; //是否显示明文密码
	private boolean showpassword2 = false; //是否显示明文密码
	private boolean showpassword3 = false; //是否显示明文密码
	public  boolean isFirstPaymentPwd;//有没有设置支付密码
	private String paymentPassword;
	private String pwd2;
	private String newpwd2;
	private String payPassword1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_pay_pwd2);
		getPayment();
	}

	//初始化
	public void initLayout() {
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		//密码
		((ImageView) findViewById(R.id.showpwd)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!showpassword) {
					showpassword = true;
					((ImageView) findViewById(R.id.showpwd)).setBackgroundResource(R.drawable.password_show1);
					((EditText)findViewById(R.id.oldpwd1)).setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else {
					showpassword = false;
					((ImageView) findViewById(R.id.showpwd)).setBackgroundResource(R.drawable.password_show);
					((EditText)findViewById(R.id.oldpwd1)).setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				
			}
		});
		((ImageView) findViewById(R.id.showpwd2)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!showpassword2) {
					showpassword2 = true;
					((ImageView) findViewById(R.id.showpwd2)).setBackgroundResource(R.drawable.password_show1);
					((EditText)findViewById(R.id.newpwd2)).setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else {
					showpassword2 = false;
					((ImageView) findViewById(R.id.showpwd2)).setBackgroundResource(R.drawable.password_show);
					((EditText)findViewById(R.id.newpwd2)).setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				
			}
		});
			((ImageView) findViewById(R.id.showpwd3)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(!showpassword3) {
						showpassword3 = true;
						((ImageView) findViewById(R.id.showpwd3)).setBackgroundResource(R.drawable.password_show1);
						((EditText)findViewById(R.id.confirm3)).setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					} else {
						showpassword3 = false;
						((ImageView) findViewById(R.id.showpwd3)).setBackgroundResource(R.drawable.password_show);
						((EditText)findViewById(R.id.confirm3)).setTransformationMethod(PasswordTransformationMethod.getInstance());
					}
					
				}
			});
			//确认密码
			((EditText)findViewById(R.id.confirm3)).setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View arg0, boolean isFocused) {
					if(isFocused)
						((ImageView)findViewById(R.id.password_icon2)).setBackgroundResource(R.drawable.password1);
					else
						((ImageView)findViewById(R.id.password_icon2)).setBackgroundResource(R.drawable.password);
				}
			});
		
		//旧密码
		((EditText)findViewById(R.id.oldpwd1)).setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean isFocused) {
				if(isFocused)
					((ImageView)findViewById(R.id.password_icon)).setBackgroundResource(R.drawable.password1);
				else
					((ImageView)findViewById(R.id.password_icon)).setBackgroundResource(R.drawable.password);
			}
		});
		
		//新密码
		((EditText)findViewById(R.id.newpwd2)).setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean isFocused) {
				if(isFocused)
					((ImageView)findViewById(R.id.password_icon2)).setBackgroundResource(R.drawable.password1);
				else
					((ImageView)findViewById(R.id.password_icon2)).setBackgroundResource(R.drawable.password);
			}
		});
		
		//修改支付密码
		((Button) findViewById(R.id.modification)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					final String pwd1 = ((EditText)findViewById(R.id.oldpwd1)).getText().toString();
					newpwd2 = ((EditText)findViewById(R.id.newpwd2)).getText().toString();
					final String pwd3 = ((EditText)findViewById(R.id.confirm3)).getText().toString();
					if(TextUtils.isEmpty(newpwd2)||TextUtils.isEmpty(pwd3)){
						Toast.makeText(getApplicationContext(), "密码不能为空", 0).show();
					}else if(!newpwd2.equals(pwd3)){
						Toast.makeText(getApplicationContext(), "两次输入密码不一致", 0).show();
					}if(!TextUtils.isEmpty(pwd1)&&!TextUtils.isEmpty(newpwd2)&&newpwd2.equals(pwd3)){
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									//检验原密码是否有误
									StringBuffer result = httpget.getArray("/api/checkequalpaypwd/?MemLoginID=" + HttpConn.username + "&PayPwd=" + pwd1 + "&AppSign=" + HttpConn.AppSign);
									Message msg = Message.obtain();
									if(new JSONObject(result.toString()).getString("return").equals("200")){
										StringBuffer result2 = httpget.getArray("/api/updatepaypwd/?MemLoginID=" + HttpConn.username + "&PayPwd=" + newpwd2 + "&AppSign=" + HttpConn.AppSign);
										msg.obj = new JSONObject(result2.toString()).getString("return");
										msg.what = 3;
									} else {
										msg.what = 0;
									}
									handler.sendMessage(msg);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}).start();
					}else{
						Toast.makeText(getApplicationContext(), "密码不能为空", 0).show();
					}
			}
		});
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(PayPasswordActivity2.this, "原密码错误", Toast.LENGTH_SHORT).show();
				break;
			case 1:
				if(msg.obj.toString().equals("200")){
					Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
					editor.putString("paymentPassword", payPassword1);
					editor.commit();
					Toast.makeText(PayPasswordActivity2.this, "设置成功", Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(PayPasswordActivity2.this, "设置失败", Toast.LENGTH_SHORT).show();
				}
				break;
			case 3:
				if(msg.obj.toString().equals("200")){
					Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
					editor.putString("paymentPassword", newpwd2);
					editor.commit();
					Toast.makeText(PayPasswordActivity2.this, "设置成功", Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(PayPasswordActivity2.this, "设置失败", Toast.LENGTH_SHORT).show();
				}
				break;
			case 4:
				isFirstPaymentPwd = true;
				Editor edit = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
				edit.putBoolean("isFirstPaymentPwd", true);
				edit.commit();
				initLayout();
				break;
			case 5:
				String paypwd = (String) msg.obj;
				Editor edit2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
				edit2.putBoolean("isFirstPaymentPwd", false);
				edit2.commit();
				initLayout();
				break;
			case 6:
				Toast.makeText(PayPasswordActivity2.this, "设置支付密码失败", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			};
			
			super.handleMessage(msg);
		}
		
	};
	//判断是否设置过支付密码
	public void getPayment(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				StringBuffer result = httpget.getArray("/api/getpaypwd/?memLoginID="+HttpConn.username+"&AppSign="+HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					if(new JSONObject(result.toString()).getString("Data").equals("")){
						msg.what = 4;
					}else {
						String paypwd = new JSONObject(result.toString()).getString("Data");
						msg.obj = paypwd;
						msg.what = 5;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}).start();
	}

}
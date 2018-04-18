package com.shopnum1.distributionportal;
//登录
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.ShareUtils;

public class UserLogin extends Activity implements PlatformActionListener {
	
	private HttpConn httpget = new HttpConn();
	private Boolean showpassword = false; //是否显示明文密码
	private JsonObjectRequest getBguanggao_task;//获取B类广告
    private int type=-1;//0-------QQ 1------WeiXin 2-------SinaWeiBo
    private RequestQueue quest;
	private Dialog mpBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_login);

		  ShareSDK.initSDK(this);
		  quest=Volley.newRequestQueue(this);
		initLayout();
	}
	//初始化
	public void initLayout() {
		((CheckBox)findViewById(R.id.check)).setChecked(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("autologin", false));
		//返回
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(httpget.isLogin){
					finish();
				}else{
					startActivity(new Intent(getApplicationContext(),MainActivity.class));
				}
			}
		});
((LinearLayout) findViewById(R.id.linear1)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Platform qq = ShareSDK.getPlatform(QQ.NAME);
				qq.SSOSetting(false);

				qq.setPlatformActionListener(UserLogin.this);
				qq.showUser(null);
				type=0;
			}
		});
((LinearLayout) findViewById(R.id.linear2)).setOnClickListener(new OnClickListener() {
	
	@Override
	public void onClick(View v) {
		Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
		wechat.SSOSetting(false);
		wechat.setPlatformActionListener(UserLogin.this);
		wechat.authorize();
		wechat.showUser(null);
		type=1;
	}
});
((LinearLayout) findViewById(R.id.linear3)).setOnClickListener(new OnClickListener() {
	
	@Override
	public void onClick(View v) {
		Platform sinaWeibo = ShareSDK.getPlatform(SinaWeibo.NAME);
		sinaWeibo.SSOSetting(true);

		sinaWeibo.setPlatformActionListener(UserLogin.this);
		sinaWeibo.showUser(null);
		type=2;
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
		//注册
		((LinearLayout) findViewById(R.id.register)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getBaseContext(), UserRegister.class), 0);
			}
		});
		
		//用户名
		((EditText)findViewById(R.id.name)).setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean isFocused) {
				if(isFocused)
					((ImageView)findViewById(R.id.username_icon)).setBackgroundResource(R.drawable.username1);
				else
					((ImageView)findViewById(R.id.username_icon)).setBackgroundResource(R.drawable.username);
			}
		});
		//密码
		((EditText)findViewById(R.id.pwd)).setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean isFocused) {
				if(isFocused)
					((ImageView)findViewById(R.id.password_icon)).setBackgroundResource(R.drawable.password1);
				else
					((ImageView)findViewById(R.id.password_icon)).setBackgroundResource(R.drawable.password);
			}
		});
		
		//登录
		((Button) findViewById(R.id.login)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final String name = ((EditText)findViewById(R.id.name)).getText().toString().trim().replaceAll(" ","");
				final String pwd = ((EditText)findViewById(R.id.pwd)).getText().toString().trim().replaceAll(" ","");
				if(!name.equals("") && !pwd.equals("")){
					new Thread(){
						@Override
						public void run(){
							Message message = Message.obtain();
							try {
								 
								String logingStr = "/api/accountlogin?MemLoginID=" + toUTF8(name) + "&Pwd=" + pwd + "&AppSign=" + HttpConn.AppSign+"&AgentID="+MyApplication.agentId;
								StringBuffer result = httpget.getArray(logingStr);
								if (result != null && !("").equals(result) && !("null").equals(result)) {
									if (("202").equals(new JSONObject(result.toString()).getString("return"))) {
										String shoppingcartgetUrl = "/api/shoppingcartget/?loginId="
												+ HttpConn.username + "&AppSign="
												+ HttpConn.AppSign + "&agentID="+MyApplication.agentId;
										String shoppingSartgetStr = "/api/shoppingcartget/?loginId=" + name + "&AppSign=" + HttpConn.AppSign +"&agentID="+MyApplication.agentId;
										
										StringBuffer result2 = httpget.getArray(shoppingSartgetStr);
										HttpConn.cartNum = new JSONObject(result2.toString()).getJSONArray("Data").length();
										login(name, pwd);
									} else {
										message.what = 0;
										handler.sendMessage(message);
									}
								} else {
									message.what = 1;
									handler.sendMessage(message);
								}
								
								
							} catch (JSONException e) {
								message.what = 1;
								handler.sendMessage(message);
								e.printStackTrace();
							}
						}
					}.start();
				} else {
					Toast.makeText(getApplicationContext(), "请输入用户名和密码", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	//登录
	public void login(String name, String pwd){
		Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
		HttpConn.isLogin = true;
		HttpConn.username = toUTF8(name);
		HttpConn.UserName = name;
		editor.putBoolean("islogin", true);
		editor.putString("name", name);
		editor.putString("pwd", pwd);
		if(((CheckBox)findViewById(R.id.check)).isChecked())
			editor.putBoolean("autologin", true);
		else
			editor.putBoolean("autologin", false);
		editor.commit();	
		
		if(getIntent().getStringExtra("cart") != null)
			startActivity(new Intent(getBaseContext(), CartActivity.class));
		if(getIntent().getStringExtra("person") != null)
			startActivity(new Intent(getBaseContext(), MemberActivity.class));
		finish();
	}
	
	public static String toUTF8(String name){
		String username = "";
		try {
			username = URLEncoder.encode(name, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return username;
	}
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(UserLogin.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
				break;
			case 1:
				Toast.makeText(UserLogin.this, "获取数据失败！", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(UserLogin.this, "取消授权！", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				mpBar = new Dialog(UserLogin.this, R.style.dialog);
				mpBar.setContentView(R.layout.progress);
				mpBar.show();
				
				break;
			case 4:
				Toast.makeText(UserLogin.this, "授权出错！", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	//注册成功跳转到个人中心
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1){
			startActivity(new Intent(getApplicationContext(), MemberActivity.class));
		}	
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	//返回主页
	@Override
	public void onBackPressed() {
		startActivity(new Intent(getBaseContext(), MainActivity.class));
		super.onBackPressed();
	}

	@Override
	public void onCancel(Platform arg0, int arg1) {
		// TODO Auto-generated method stub
		Log.e("onCancel", arg0.toString());
		Message message = Message.obtain();
		message.what = 2;
		handler.sendMessage(message);
	//	 Toast.makeText(getApplicationContext(), "onCancel：" + arg0.toString(), Toast.LENGTH_SHORT).show(); 
	}
	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
		// TODO Auto-generated method stub
		Log.e("onComplete", arg0.toString());
		Message message = Message.obtain();
		message.what = 3;
		handler.sendMessage(message);
		
	//	Toast.makeText(getApplicationContext(), "onComplete：" + arg0.toString(), Toast.LENGTH_SHORT).show(); 
		update( arg0.getDb().getUserId(), type);
		arg0.removeAccount();
	}
	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {
		// TODO Auto-generated method stub
		
		
		Log.e("onError", arg0.toString());
		arg2.printStackTrace();
		System.out.print(arg2);
		Message message = Message.obtain();
		message.what = 4;
		handler.sendMessage(message);
//		 Toast.makeText(getApplicationContext(), "onError：" + arg0.toString(), Toast.LENGTH_SHORT).show(); 
	}

	
	//获取B类活动
	public void update(String openId,int type){
		JSONObject object=new JSONObject();
		try {
			object.put("type", type);
			object.put("openId", openId);
			object.put("appSign", HttpConn.AppSign);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				getBguanggao_task = new JsonObjectRequest(Method.POST,HttpConn.hostName+"/api/ThirdLogin/", object,new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject mresponse) {		
						   login(mresponse.optJSONObject("Data").optString("MemLoginID"), mresponse.optJSONObject("Data").optString("Pwd"));
						   if(mpBar!=null){
							   mpBar.dismiss();
						   }
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						 if(mpBar!=null){
							   mpBar.dismiss();
						   }
						error.printStackTrace();
						
					}
				});
				quest.add(getBguanggao_task);// 发送登陆网络请求
			} 
		
	
//public class a implements PlatformActionListener{
//
//	@Override
//	public void onCancel(Platform arg0, int arg1) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onError(Platform arg0, int arg1, Throwable arg2) {
//		// TODO Auto-generated method stub
//		
//	}
//	
//}


}
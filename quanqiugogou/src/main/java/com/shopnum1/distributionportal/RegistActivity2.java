package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.ShareUtils;
import com.zxing.bean.Agent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public class RegistActivity2 extends Activity {
	private EditText et_pass;
	private EditText et_pass_again;
	private EditText et_user;
	private Button btn_ok;
	private String Phone;
//	final List<Agent> agenLists  = new ArrayList<Agent>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regist_activity2);
		initLayout();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
//	 getAgentList();
//		Log.i("test", "list length="+list.size());
		super.onResume();
		
	}
//	private void getAgentList() {
//		HttpUtils hu = new HttpUtils();
//		
//		String  urlStr = HttpConn.hostName
//				+ "/api/GetAgentList?AppSign=" + HttpConn.AppSign ;
//		hu.send(HttpMethod.GET, urlStr 
//		, new RequestCallBack<String>() {
//			@Override
//			public void onFailure(HttpException arg0,String arg1) {
//				Toast.makeText(getApplicationContext(),
//						"服务异常，注册失败", 1000).show();
//			}
//			@Override
//			public void onSuccess(ResponseInfo<String> data) {
//				try {
//					Log.i("test", "data:"+data.result);
//					String jsonStr = data.result;
//					 JSONTokener jsonParser = new JSONTokener(jsonStr);  
//					  JSONObject dataJson = (JSONObject) jsonParser.nextValue();  
//					  JSONArray dataArray = dataJson.getJSONArray("Data");  
//					  Agent agent = new Agent();
//					  for (int i = 0;i<dataArray.length();i++){
//						  JSONObject agentObj= (JSONObject) dataArray.get(i);
//						  String memLoginID = agentObj.getString("MemLoginID");
//						  String email = agentObj.getString("Email");
//						  agent.setMemLoginID(memLoginID);
//						  agent.setEmail(email);
//						  agenLists.add(agent);
//					  }
//					  List<Agent> agenLists2  =agenLists;  //删
//					  Log.i("test", "agenLists2:"+agenLists2.size());//删
//					  
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//			
//		});
//	
//	}

	private void initLayout() {
		Phone = getIntent().getStringExtra("phoneNum");
		btn_ok = (Button) this.findViewById(R.id.register);
		et_pass = (EditText) this.findViewById(R.id.pass);
		et_pass_again = (EditText) this.findViewById(R.id.pwd_again);
		et_user = (EditText) this.findViewById(R.id.user_name);
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				//判断是否是分销商
//				HttpUtils httpUtils = new HttpUtils();
//				String checkUrl = HttpConn.hostName
//						+ "/api/CheckIsAgent?AppSign=" + HttpConn.AppSign + "&AgentID="+et_user.getText().toString();
//				httpUtils.send(HttpMethod.GET,  HttpConn.hostName
//						+ "/api/CheckIsAgent?AppSign=" + HttpConn.AppSign + "&MemLoginID="+et_user.getText().toString()
//
//				, new RequestCallBack<String>() {
//					@Override
//					public void onFailure(HttpException arg0,String arg1) {
//						Toast.makeText(getApplicationContext(),
//								"服务异常，注册失败", 1000).show();
//					}
//					@Override
//					public void onSuccess(ResponseInfo<String> data) {
//						try {
//							String result = data.result;
//							//判断是否是分销商
//							gotoRegist();
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//					
//				});
//				
				gotoRegist();
				
			}
		});
		
//		et_user.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				Intent intent  = new Intent();
//				
//				intent.setClass(RegistActivity2.this, SelectAgentActivity.class);
//				startActivityForResult(intent, 100);
//				startActivity(intent);
//			}
//		});
	}
	private void gotoRegist() {
		if (ispass(et_pass.getText().toString())) {
			if (!TextUtils.isEmpty(et_pass.getText().toString())&& (!TextUtils.isEmpty(et_pass_again.getText().toString()))) {
				if (et_pass.getText().toString().equals(et_pass_again.getText().toString())) {
//					 String agentId = ShareUtils.getString(getApplicationContext(), HttpConn.AGENT_ID_KEY, "");
					 String agentId =MyApplication.agentId;
					String etTxt = et_user.getText().toString();
					if(etTxt!=null&&!"".equals(etTxt)){
						agentId=etTxt;
					}
//					String urlStr  = HttpConn.hostName
//							+ "/api/accountregist?MemLoginID=" + Phone 
//							+ "&Pwd=" + et_pass.getText().toString()
//							+ "&CommendPeople="
//							+ "&Email=0&Mobile="+ Phone + "&AppSign=" + HttpConn.AppSign
//							+ "&AgentID="+agentId;
					String urlStr  = HttpConn.hostName
							+ "/api/accountregist?MemLoginID=" + Phone
							+ "&Pwd=" + et_pass.getText().toString()
							+ "&Email=0&Mobile="+ Phone + "&AppSign=" + HttpConn.AppSign
							+ "&AgentID="+agentId;
					if (et_pass.getText().toString().length()>=6) {
					HttpUtils hu = new HttpUtils();
					
					hu.send(HttpMethod.GET,  urlStr
					, new RequestCallBack<String>() {
						@Override
						public void onFailure(HttpException arg0,String arg1) {
							Toast.makeText(getApplicationContext(),
									"服务异常，注册失败", 1000).show();
						}
						@Override
						public void onSuccess(ResponseInfo<String> data) {
							try {
								JSONObject object=new JSONObject(data.result);
								if(object.optString("return").equals("202")){
									HttpConn.isLogin = true;
									HttpConn.username = Phone;
									HttpConn.UserName = Phone;
									Toast.makeText(RegistActivity2.this,
											"注册成功", Toast.LENGTH_SHORT).show();
									Editor editor = PreferenceManager
											.getDefaultSharedPreferences(
													getApplicationContext())
											.edit();
									editor.putString("name", Phone);
									editor.putString("pwd", et_pass.getText()
											.toString());
									editor.putBoolean("islogin", true);
									editor.commit();
									Intent i = new Intent(RegistActivity2.this,
											MainActivity.class);
									setResult(1, getIntent());
									startActivity(i);
									RegistActivity2.this.finish();
								}else{
									Toast.makeText(getApplicationContext(),
											"注册失败", 1000).show();
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});	}else {
						Toast.makeText(getApplicationContext(), "密码至少6位",
								2000).show();
					}
				} else {
					Toast.makeText(getApplicationContext(), "两次密码不一致",
							2000).show();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"密码和确认密码不能为空", 2000).show();
			}
		} else {
			Toast.makeText(getApplicationContext(), "密码格式为 字母 数字和_", 2000)
					.show();
		}
		
	}
	// 电话号码
	public static boolean ispass(String str) {
		Pattern p = Pattern.compile("[A-Za-z0-9_]+");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TO DO Auto-generated method stub
		if(resultCode == 20){
			String agent = data.getExtras().getString("agent");
			et_user.setText(agent);
			Toast.makeText(getApplicationContext(), "agent:"+agent, 1).show();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
}

package com.shopnum1.distributionportal;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.ShareUtils;
import com.zxing.bean.Agent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

public class SelectAgentActivity extends Activity {
	
	private ImageView backIv ;
	private ListView listView;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_agent_id);
		
		initView();
		initData();
		Log.i("SelectAgentActivity" , "onCreate");
	}

	private void initView() {
		backIv = (ImageView) findViewById(R.id.back);
		listView = (ListView) findViewById(R.id.list_view);
		
	}
	private void initData() {
		getAgentList();
		
		backIv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SelectAgentActivity.this.finish();
			}
		});
	}
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				//显示列表
				ArrayList<String> lists = (ArrayList<String>) msg.obj;
				SelectAgentAdapter selectAdapter = new SelectAgentAdapter(SelectAgentActivity.this,lists);
				selectAdapter.setHandler(handler);
				listView.setAdapter(selectAdapter);
				break;

			case 2:
				//返回注册
//				
				//判断分销商
				final String agent = msg.obj.toString();
				HttpUtils httpUtils = new HttpUtils();
				String checkUrl = HttpConn.hostName
						+ "/api/CheckIsAgent?AppSign=" + HttpConn.AppSign + "&MemLoginID="+agent;
				httpUtils.send(HttpMethod.GET,  checkUrl, new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0,String arg1) {
						Toast.makeText(getApplicationContext(),
								"服务异常，注册失败", 1000).show();
					}
					@Override
					public void onSuccess(ResponseInfo<String> data) {
						try {
							String result = data.result;
							//判断是否是分销商
							JSONObject object=new JSONObject(data.result);
							if(object.optString("Data").equals("true")){
								//保存 agentId
								
								 ShareUtils.putString(getApplicationContext(),  MyApplication.AGENT_ID_KEY, agent);
								 MyApplication.agentId = agent;  //放入全局内存
								// 进入主页
								 startActivity(new Intent(getApplicationContext(),
											MainActivity.class)); 
//								 String agentId = ShareUtils.getString(getApplicationContext(), HttpConn.AGENT_ID_KEY, "");
						         SelectAgentActivity.this.finish();
							}else{
								Toast.makeText(getApplicationContext(), agent+"-不是有效的分销商ID", 1).show();
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Toast.makeText(getApplicationContext(), "判断分销商ID出错", 1).show();
							e.printStackTrace();
						}
					}
					
				});
				
				
				
//				//保存 agentId
//				String agent = msg.obj.toString();
//				 ShareUtils.putString(getApplicationContext(),  MyApplication.AGENT_ID_KEY, agent);
//				 MyApplication.agentId = agent;  //放入全局内存
//				// 进入主页
//				 startActivity(new Intent(getApplicationContext(),
//							MainActivity.class)); 
////				 String agentId = ShareUtils.getString(getApplicationContext(), HttpConn.AGENT_ID_KEY, "");
//		         SelectAgentActivity.this.finish();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	private void getAgentList() {
		HttpUtils hu = new HttpUtils();
		
		String  urlStr = HttpConn.hostName
				+ "/api/GetAgentList?AppSign=" + HttpConn.AppSign ;
		hu.send(HttpMethod.GET, urlStr 
		, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0,String arg1) {
				Toast.makeText(getApplicationContext(),
						"服务异常，注册失败", 1000).show();
			}
			@Override
			public void onSuccess(ResponseInfo<String> data) {
				try {
					Log.i("test", "data:"+data.result);
					String jsonStr = data.result;
					 JSONTokener jsonParser = new JSONTokener(jsonStr);  
					  JSONObject dataJson = (JSONObject) jsonParser.nextValue();  
					  JSONArray dataArray = dataJson.getJSONArray("Data");  
					  ArrayList<Agent> agenLists  = new ArrayList<Agent>();
					  ArrayList<String> agenLists2  = new ArrayList<String>();
					  Agent agent = new Agent();
					  for (int i = 0;i<dataArray.length();i++){
						  JSONObject agentObj= (JSONObject) dataArray.get(i);
						  String memLoginID = agentObj.getString("MemLoginID");
						  String email = agentObj.getString("Email");
						  agent.setMemLoginID(memLoginID);
						  agent.setEmail(email);
						  agenLists.add(agent);
						  agenLists2.add(memLoginID);
					  }
					 
					  
					  Message msg  = new Message();
					  msg.obj  = agenLists2;
					  msg.what = 1;
					  handler.sendMessage(msg);
					  
					  
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
	
	}

}

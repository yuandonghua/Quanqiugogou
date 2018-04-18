package com.shopnum1.distributionportal;
//分类浏览
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;


public class CityList extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private List<Map<String, String>> list;
	private Dialog pBar;
	private int index = 0;
	private String OrderID = "0";
	private String cityName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_list);
        initLayout();
        addData();
    }
    
	public void initLayout(){
		//返回
		((LinearLayout)findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
	//加载数据
	public void addData(){
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		
		new Thread() {
			@Override
			public void run(){		
				StringBuffer result = httpget.getArray("/api/region/?parentId=" + OrderID + "&AppSign=" + HttpConn.AppSign);
				try {
					JSONArray sortList = new JSONObject(result.toString()).getJSONArray("Data");
					list = new ArrayList<Map<String, String>>();
					for (int i = 0; i < sortList.length(); i++) {
						Map<String, String> map = new HashMap<String, String>();
						map.put("name", sortList.getJSONObject(i).getString("Name"));
						map.put("id", sortList.getJSONObject(i).getString("OrderID"));
						map.put("code", sortList.getJSONObject(i).getString("Code"));
						list.add(map);
					}
					Message msg = new Message();
					handler.sendMessage(msg);	
				} catch (JSONException e) {
					e.printStackTrace();
				}		
			}
		}.start();
	}
	
	public void addList(){
		ListView listview = (ListView)findViewById(R.id.listview);
		SimpleAdapter gridadapter = new SimpleAdapter(getApplicationContext(), list, R.layout.city_item, new String[]{"name"}, new int[]{R.id.name});	
		listview.setAdapter(gridadapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int i, long arg3) {
				String code = list.get(i).get("code");
				cityName += list.get(i).get("name");
				if(index < 2) {
					OrderID = list.get(i).get("id");
					addData();
					index++;
				} else {
					HttpConn.cityName = cityName;
					Intent intent = getIntent();
					intent.putExtra("code", code);
					setResult(1, intent);
					finish();
				}
			}
		});
	}
	
	Handler handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			pBar.dismiss();
			addList();
			super.handleMessage(msg);
		}
		
	};
    
}
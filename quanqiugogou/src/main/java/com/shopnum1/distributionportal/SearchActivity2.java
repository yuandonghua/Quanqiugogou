package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity2 extends Activity {
	
	private JSONArray CatagoryList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.city_list);
		
		((TextView) findViewById(R.id.title)).setText(getIntent().getStringExtra("title"));
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		try {
			CatagoryList = new JSONArray(getIntent().getStringExtra("CatagoryList3"));
			JSONObject object = new JSONObject();
			object.put("Name", "其它商品");
			object.put("ID", getIntent().getStringExtra("typeid"));
			CatagoryList.put(object);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		addList();
	}

	//一级分类列表
	public void addList(){
		ListView listview = (ListView)findViewById(R.id.listview);
		listview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		listview.setAdapter(new CatagoryAdapter());
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				try {
					String name = CatagoryList.getJSONObject(position).getString("Name");
					String id = CatagoryList.getJSONObject(position).getString("ID");
					
					Intent intent = new Intent(getApplicationContext(), SearchResult.class);
					intent.putExtra("title", name);
					intent.putExtra("type", "list");
					intent.putExtra("typeid", Integer.parseInt(id));
					intent.putExtra("searchstr", "");
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
		});
	}
	//列表适配器
	class CatagoryAdapter extends BaseAdapter{

		@Override
		public int getCount() {

			return CatagoryList.length();
		}

		@Override
		public Object getItem(int arg0) {

			return arg0;
		}

		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.city_item, null);
				convertView.setBackgroundResource(R.drawable.searchbg2);
			}
			try {
				((TextView)convertView.findViewById(R.id.name)).setText(CatagoryList.getJSONObject(position).getString("Name"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return convertView;
		}
		
	}

}
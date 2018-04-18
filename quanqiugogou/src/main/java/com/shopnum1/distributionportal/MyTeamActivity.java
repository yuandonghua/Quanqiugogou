package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.zxing.view.CircleImageView;

public class MyTeamActivity extends Activity {
	private HttpConn httpget = new HttpConn();
	private ListView lv; 
	private ArrayList<JSONObject> lv1 = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> lv2 = new ArrayList<JSONObject>();
	private ArrayList<JSONObject> lv3 = new ArrayList<JSONObject>();
	private TextView tv_earnings;
	private TextView tv_generalize;
	private TextView yiji;
	private TextView erji;
	private TextView sanji;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_team);
		initLayout();
		getSanJiDistribution();
	}
	
	private void initLayout() {
		lv= (ListView) findViewById(R.id.lv);
		tv_earnings = (TextView) findViewById(R.id.tv_earnings);
		tv_generalize = (TextView) findViewById(R.id.tv_generalize);
		yiji = (TextView) findViewById(R.id.yiji);
		erji = (TextView) findViewById(R.id.erji);
		sanji = (TextView) findViewById(R.id.sanji);
		findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		findViewById(R.id.develop).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), DevelopVipActivity.class));
			}
		});
		findViewById(R.id.category1).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				findViewById(R.id.cursor).setVisibility(View.VISIBLE);
				findViewById(R.id.cursor2).setVisibility(View.INVISIBLE);
				findViewById(R.id.cursor3).setVisibility(View.INVISIBLE);
				lv.setAdapter(new MyAdapter(lv1));
			}
		});
		findViewById(R.id.category2).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				findViewById(R.id.cursor2).setVisibility(View.VISIBLE);
				findViewById(R.id.cursor).setVisibility(View.INVISIBLE);
				findViewById(R.id.cursor3).setVisibility(View.INVISIBLE);
				lv.setAdapter(new MyAdapter(lv2));
			}
		});
		findViewById(R.id.category3).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				findViewById(R.id.cursor3).setVisibility(View.VISIBLE);
				findViewById(R.id.cursor2).setVisibility(View.INVISIBLE);
				findViewById(R.id.cursor).setVisibility(View.INVISIBLE);
				lv.setAdapter(new MyAdapter(lv3));
			}
		});
	}
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				try {
					JSONObject jsonObject = (JSONObject) msg.obj;
					JSONArray jsonArray = jsonObject.getJSONArray("Distributor");
					//收益
					double earnings = jsonObject.getJSONObject("Profit").getDouble("0");
					//推广
					double generalize = jsonObject.getJSONObject("Profit").getDouble("1");
					tv_earnings.setText(earnings+"");
					tv_generalize.setText(generalize+"");
					for (int i = 0; i < jsonArray.length(); i++) {
						if (((JSONObject)jsonArray.get(i)).getInt("lvl")==1) {
							lv1.add(((JSONObject)jsonArray.get(i)));
						}else if (((JSONObject)jsonArray.get(i)).getInt("lvl")==2) {
							lv2.add(((JSONObject)jsonArray.get(i)));
						}else {
							lv3.add(((JSONObject)jsonArray.get(i)));
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(lv1 != null){
					lv.setAdapter(new MyAdapter(lv1));
				}
				yiji.setText(lv1.size()+"");
				erji.setText(lv2.size()+"");
				sanji.setText(lv3.size()+"");
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
    private class MyAdapter extends BaseAdapter{
	
    	private ArrayList<JSONObject> list_data;
    	public MyAdapter(ArrayList<JSONObject> list_data) {
    		this.list_data = list_data;
		}
    	
    	@Override
		public int getCount() {
			return list_data.size();
		}

		@Override
		public Object getItem(int position) {
			return list_data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			View view = null;
			if(view == null){
				view = View.inflate(getApplicationContext(), R.layout.categary_item, null);
				view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.categary_item, null);
				holder = new ViewHolder();
				holder.iv = (CircleImageView) view.findViewById(R.id.head);
				holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
				holder.tv_price = (TextView) view.findViewById(R.id.tv_price);
				view.setTag(holder);
			}else{
				holder = (ViewHolder) view.getTag();
			}
			try {
				JSONObject jsonObject = list_data.get(position);
				holder.tv_name.setText(jsonObject.getString("MemLoginID"));
				holder.tv_price.setText("总盈利￥"+jsonObject.getDouble("Profit"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return view;
		}
		
    }
    static class ViewHolder{
    	CircleImageView iv;
    	TextView tv_name;
    	TextView tv_price;
    } 
    /**
     * 获取三级分销的列表
     */
    public void getSanJiDistribution(){
    	new Thread(){
			public void run() {
				Message msg = Message.obtain();
    			try {
    				StringBuffer result = httpget.getArray("/api/getdistributor?MemLoginID="+HttpConn.username+"&AppSign="+HttpConn.AppSign);
    				JSONObject jsonObject = new JSONObject(result.toString()).getJSONObject("Data");
    				msg.obj = jsonObject;
    				msg.what = 1;
				} catch (JSONException e) {
					e.printStackTrace();
				}
    			handler.sendMessage(msg);
    		};
    	}.start();
    }
}


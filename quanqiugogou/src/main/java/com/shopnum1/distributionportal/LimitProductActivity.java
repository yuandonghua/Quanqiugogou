package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.CustomDigitalClock;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class LimitProductActivity extends Activity {
	
	private HttpConn httpget = new HttpConn();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_limit_product_list);
		initLayout();
	}
	@Override
	protected void onResume() {
		ListView lv = (ListView) findViewById(R.id.listview);
		adapter2 = new GridViewAdapter2();
		lv.setAdapter(adapter2);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {
					Intent intent = new Intent(getApplicationContext(), ProductDetails.class);
					intent.putExtra("guid", adapter2.getInfo(position).getString("ProductGuid"));
					intent.putExtra("EndTime", adapter2.getInfo(position).getString("EndTime"));
					intent.putExtra("RestrictCount", adapter2.getInfo(position).getInt("RestrictCount"));
					intent.putExtra("ShopPrice", adapter2.getInfo(position).getDouble("PanicBuyingPrice"));
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		super.onResume();
	}
	private void initLayout() {
		
	findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			finish();
		}
	});	
		
	}
		Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 10:
					adapter2.notifyDataSetChanged();
					break;

				default:
					break;
				}
			};
		};
		private GridViewAdapter2 adapter2;
	//商品适配器
		class GridViewAdapter2 extends BaseAdapter {
			
			private JSONArray companyList = new JSONArray();
			
			public JSONObject getInfo(int position) {
				JSONObject result = null;
				try {
					result = companyList.getJSONObject(position);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return result;
			}
			
			public GridViewAdapter2() {
				new Thread() {
					@Override
					public void run() {
						StringBuffer result = httpget.getArray("/api/panicbuyinglist/?pageIndex=1&pageSize=100" + "&AppSign=" + HttpConn.AppSign);
						Log.i("TAG", result.toString());
						try {
							companyList = new JSONObject(result.toString()).getJSONArray("Data");
							Message message = Message.obtain();
							message.what = 10;
							handler.sendMessage(message);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
			
			@Override
			public int getCount() {
				return companyList.length();
			}
			
			@Override
			public Object getItem(int arg0) {
				return arg0;
			}
			
			@Override
			public long getItemId(int arg1) {
				return arg1;
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if(convertView == null) {
					convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.limit_product_item, parent, false);
				}
				try {
					TextView text1 = (TextView) convertView.findViewById(R.id.text1);	
					TextView text2 = (TextView) convertView.findViewById(R.id.text2);	
					CustomDigitalClock text3 = (CustomDigitalClock) convertView.findViewById(R.id.text3);
					text1.setText(companyList.getJSONObject(position).getString("Name"));		
					text2.setText("￥" + new DecimalFormat("0.00").format(companyList.getJSONObject(position).getDouble("PanicBuyingPrice")));		
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date d2 = sdf.parse(companyList.getJSONObject(position).getString("EndTime"));
					text3.setEndTime(d2.getTime());
					ImageView imageview = (ImageView) convertView.findViewById(R.id.img);
					if(HttpConn.showImage)
						ImageLoader.getInstance().displayImage(companyList.getJSONObject(position).getString("OriginalImge"), imageview, MyApplication.options);
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				return convertView;
			}		
			
		}
}
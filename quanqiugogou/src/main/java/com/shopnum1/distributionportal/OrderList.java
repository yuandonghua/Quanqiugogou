package com.shopnum1.distributionportal;

//订单详情
import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
public class OrderList extends Activity {
	private HttpConn httpget = new HttpConn();
	private JSONArray ProductList;
	private ListAdapter adapter;
	private Dialog pBar; // 加载进度
	private int times;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_list);
		initLayout();
		getData();
	}

	// 初始化
	public void initLayout() {
		// 返回
		((LinearLayout) findViewById(R.id.back))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
	}

	public void getData() {
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();

		new Thread() {
			public void run() {
				String orderListUrl = "/api/order/member/OrderList/?pageIndex=1&pageCount=100&memLoginID="
						+ HttpConn.username
						+ "&t=0&AppSign="
						+ HttpConn.AppSign+"&agentID="+MyApplication.agentId;
				StringBuffer result = httpget
						.getArray(orderListUrl);
				Message msg = Message.obtain();
				try {
					JSONArray OrderList = new JSONObject(result.toString())
							.getJSONArray("Data");
					ProductList = new JSONArray();
					for (int i = 0; i < OrderList.length(); i++) {
						JSONObject object = OrderList.getJSONObject(i);
						if ((object.getInt("ShipmentStatus") != 4 && object
								.getInt("OderStatus") == 5)
								|| object.getInt("ShipmentStatus") == 2) {
							JSONArray array = object
									.getJSONArray("ProductList");
							for (int j = 0; j < array.length(); j++) {
								JSONObject object2 = array.getJSONObject(j);
								StringBuffer result2 = httpget
										.getArray("/api/getbaskorderlogs/?pageIndex=1&pageSize=1&ProductGuid="
												+ array.getJSONObject(j)
														.getString(
																"ProductGuid")
												+ "&OrderNumber="
												+ object.getString("OrderNumber")
												+ "&AppSign="
												+ HttpConn.AppSign);
								if (new JSONObject(result2.toString())
										.getInt("count") == 0)
									object2.put("isShown", false);
								else
									object2.put("isShown", true);
								object2.put("OrderNumber",
										object.getString("OrderNumber"));
								ProductList.put(object2);
								times++;
							}
						}
					}
					msg.what = 1;
				} catch (JSONException e) {
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				pBar.dismiss();
				break;
			case 1:
				addList();
				pBar.dismiss();
				if (times == 0)
					((TextView) findViewById(R.id.nocontent))
							.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	public void addList() {
		ListView listview = (ListView) findViewById(R.id.listview);
		listview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ListAdapter();
		listview.setAdapter(adapter);
		// 商品详情
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int i,
					long arg3) {
				try {
					Intent intent = new Intent(getApplicationContext(),
							ProductDetails.class);
					intent.putExtra("guid", ProductList.getJSONObject(i)
							.getString("ProductGuid"));
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {

			return ProductList.length();
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
		public View getView(final int position, View convertView, ViewGroup arg2) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.order_item3, null);
			}
			try {
				((TextView) convertView.findViewById(R.id.text1))
						.setText(ProductList.getJSONObject(position).getString(
								"NAME"));
				Log.i("test", ProductList.getJSONObject(position) + "");
				((TextView) convertView.findViewById(R.id.text2))
						.setText(ProductList.getJSONObject(position).getString(
								"Attributes"));
				((TextView) convertView.findViewById(R.id.text3))
						.setText("￥"
								+ new DecimalFormat("0.00").format(ProductList
										.getJSONObject(position).getDouble(
												"BuyPrice")));
				Boolean isShown = ProductList.getJSONObject(position)
						.getBoolean("isShown");
				Button btn = (Button) convertView.findViewById(R.id.text4);
				btn.setVisibility(View.VISIBLE);
				if (isShown) {
					btn.setText("已晒单");
					btn.setOnClickListener(null);
				} else {
					btn.setText("去晒单");
					btn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							try {
								Intent intent = new Intent(getBaseContext(),
										OrderShow.class);
								intent.putExtra("ProductList", ProductList
										.getJSONObject(position).toString());
								startActivityForResult(intent, 0);
							} catch (JSONException e) {
								e.printStackTrace();
							}

						}
					});
				}

				ImageView imageview = (ImageView) convertView
						.findViewById(R.id.imageView1);
				if (HttpConn.showImage)
					ImageLoader.getInstance().displayImage(
							ProductList.getJSONObject(position).getString(
									"OriginalImge"), imageview,
							MyApplication.options);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return convertView;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1) {
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
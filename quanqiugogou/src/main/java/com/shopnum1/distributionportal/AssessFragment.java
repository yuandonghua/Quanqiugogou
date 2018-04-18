package com.shopnum1.distributionportal;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.ShareUtils;

import java.text.DecimalFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AssessFragment extends Fragment {
	private HttpConn httpget = new HttpConn();
	private JSONArray ProductList;
	private ListAdapter adapter;
	private Dialog pBar; // 加载进度
	private int times;
	private View v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.activity_assess_list, null);
		return v;
	}

	@Override
	public void onResume() {
		getData();
		super.onResume();
	}

	public void getData() {
		pBar = new Dialog(getActivity(), R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();

		new Thread() {
			public void run() {
				String orderListUrl  = "/api/order/member/OrderList/?pageIndex=1&pageCount=100&memLoginID="
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
										.getArray("/api/countProductComment/?&productGuid="
												+ array.getJSONObject(j)
														.getString(
																"ProductGuid")
												+ "&orderNumber="
												+ object.getString("OrderNumber")
												+ "&AppSign="
												+ HttpConn.AppSign);
								if (new JSONObject(result2.toString())
										.getInt("count") == 0)
									object2.put("isAssess", false);
								else
									object2.put("isAssess", true);
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
					msg.what = 0;
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
					((TextView) v.findViewById(R.id.nocontent))
							.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	public void addList() {
		ListView listview = (ListView) v.findViewById(R.id.listview);
		listview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ListAdapter();
		listview.setAdapter(adapter);
		// 商品详情
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int i,
					long arg3) {
				try {
					Intent intent = new Intent(getActivity(),
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
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.order_item3, null);
			}
			try {
				((TextView) convertView.findViewById(R.id.text1))
						.setText(ProductList.getJSONObject(position).getString(
								"NAME"));
				((TextView) convertView.findViewById(R.id.text2))
						.setText(ProductList.getJSONObject(position).getString(
								"Attributes"));
				((TextView) convertView.findViewById(R.id.text3))
						.setText("￥"
								+ new DecimalFormat("0.00").format(ProductList
										.getJSONObject(position).getDouble(
												"BuyPrice")));
				Boolean isAssess = ProductList.getJSONObject(position)
						.getBoolean("isAssess");
				Button btn = (Button) convertView.findViewById(R.id.text4);
				btn.setVisibility(View.VISIBLE);
				if (isAssess) {
					btn.setText("已评价");
					btn.setOnClickListener(null);
				} else {
					btn.setText("去评价");
					btn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							try {
								Intent intent = new Intent(getActivity()
										.getBaseContext(),
										AssessShowActivity.class);
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

}

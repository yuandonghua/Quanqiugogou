package com.shopnum1.distributionportal;
//我的收藏
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
public class MemberCollect extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private ListAdapter adapter;
	private int requestTime = 1;
	private Dialog pBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_collect);
		
		initLayout();
		addList();
	}
	//初始化
	public void initLayout() {
		//返回
        ((LinearLayout)findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
	
	public void addList(){
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		
		ListView listview = (ListView)findViewById(R.id.listview);
		adapter = new ListAdapter(this);
		listview.setAdapter(adapter);
		registerForContextMenu(listview);
		//商品详情
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int i, long arg3) {
				try {
					Intent intent = new Intent(getApplicationContext(), ProductDetails.class);
					intent.putExtra("guid", adapter.getInfo(i).getString("ProductGuid"));
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}	
			}
		});
		
		listview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				return false;
			}
		});
		
		listview.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(final AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 判断是否滚动到底部
					if (view.getLastVisiblePosition() == view.getCount() - 1)
						adapter.upData("/api/collectlist/?pageIndex=" + requestTime + "?pageCount=5&MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign, false);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(v==((ListView)findViewById(R.id.listview))){
			menu.setHeaderTitle("确定删除吗?");
			menu.add(0, 0, 0, "确定");
			menu.add(0, 1, 0, "取消");
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int selectedPosition=((AdapterContextMenuInfo)item.getMenuInfo()).position;
		if(item.getItemId()==0){
			try {
				delCollection(adapter.getInfo(selectedPosition).getString("Guid"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return super.onContextItemSelected(item);
	}
	
	public void delCollection(final String guid){
		new Thread(){
			@Override
			public void run(){
				httpget.getArray("/api/collectdelete?CollectId=" + guid + "&MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
				Message message = Message.obtain();
				message.what = 4;
				handler.sendMessage(message);
			}
		}.start();
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if(!adapter.isdown){
					adapter.isdown = true;
					adapter.notifyDataSetChanged();
					adapter.isdown = false;
				}
				break;
			case 2:
				if(msg.obj.equals("202"))
					Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getApplicationContext(), "删除失败", Toast.LENGTH_SHORT).show();
				finish();
				startActivity(new Intent(getApplicationContext(), MemberCollect.class));
				break;
			case 3:
				((TextView)findViewById(R.id.nocontent)).setVisibility(View.VISIBLE);
				break;
			case 4:
				addList();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	class ListAdapter extends BaseAdapter{
		private ArrayList<Bitmap> bm = new ArrayList<Bitmap>();
		private JSONArray collectList = new JSONArray();
		private boolean isend = false; //已发送
		private boolean isdown = false; //在下拉
		int count = 0;
		
		public JSONObject getInfo(int position) {
			JSONObject result = null;
			try {
				result = collectList.getJSONObject(position);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}
		
		//翻页
		public void upData(final String port, final boolean isRebuilt) {
			if (isRebuilt)
				isend = false;
			if (!isend) {
				if (!isdown) {
					isdown = true;
					new Thread() {
						@Override
						public void run() {
							StringBuffer temp = httpget.getArray(port);
							if (temp.length() == 0) {
								isend = true;
								if (requestTime == 1) {
									collectList = new JSONArray();
									bm.clear();
									isdown = false;
									Message message = Message.obtain();
									message.what = 1;
									handler.sendMessage(message);
								} else
									isdown = false;
							} else {
					            requestTime++;
								try {
									StringBuffer data = new StringBuffer();								
									if (isRebuilt) {
										bm.clear();
										data.append(temp);
									} else {
										data.append(collectList.toString());
										data.setCharAt(data.length() - 1, ',');
										data.append(new JSONObject(temp.toString()).getJSONArray("Data").toString().substring(1));
									}
									collectList = new JSONArray(data.toString());
									int start = bm.size();
									int end = collectList.length();
									if(end <= count) {
										for (int i = start; i < end; i++)
											bm.add(null);
									}
									isdown = false;
									Message message = Message.obtain();
									message.what = 1;
									handler.sendMessage(message);
								} catch (JSONException e) {
									e.printStackTrace();
									isdown = false;
									isend = true;
								}
							}
						}
					}.start();
				}
			}
		}
		
		public ListAdapter(Context c) {
			isdown = true;
			new Thread() {
				@Override
				public void run() {
					try {
						StringBuffer result = httpget.getArray("/api/collectlist/?pageIndex=1&pageCount=50&MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
						pBar.dismiss();
						collectList = new JSONObject(result.toString()).getJSONArray("Data");
						count = Integer.parseInt(new JSONObject(result.toString()).get("Count").toString());
						if(count == 0){
							Message message = Message.obtain();
							message.what = 3;
							handler.sendMessage(message);
						} else {
							requestTime++;
						}
						for (int i = 0; i < collectList.length(); i++)
							bm.add(null);
						isdown = false;
						
						Message message = Message.obtain();
						message.what = 1;
						handler.sendMessage(message);
					} catch (JSONException e) {
						isdown = false;
						isend = true;
						e.printStackTrace();
					}
				}
			}.start();
		}

		@Override
		public int getCount() {
			
			return bm.size();
		}

		@Override
		public Object getItem(int arg0) {
			
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.product_item, parent, false);
				holder.ProductName = (TextView)convertView.findViewById(R.id.textView1);
				holder.ShopPrice = (TextView)convertView.findViewById(R.id.textView2);
				holder.MarketPrice = (TextView)convertView.findViewById(R.id.textView3);
				holder.ProductImage = (ImageView)convertView.findViewById(R.id.imageView1);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			try {
				holder.ProductName.setText(collectList.getJSONObject(position).getString("Name"));
				holder.ShopPrice.setText("￥" + collectList.getJSONObject(position).getString("ShopPrice"));
				holder.MarketPrice.setHint("￥" + collectList.getJSONObject(position).getString("MarketPrice"));
				if(HttpConn.showImage)
					ImageLoader.getInstance().displayImage(collectList.getJSONObject(position).getString("OriginalImge"), holder.ProductImage, MyApplication.options);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return convertView;
		}
		
	}
	
	class ViewHolder {
		ImageView ProductImage;
		TextView ProductName;
		TextView ShopPrice;
		TextView MarketPrice;
	}
	
	public void delMessage(final String msgid) {
		new Thread(){
			@Override
			public void run(){	
				try {
					StringBuffer result = httpget.getArray("/api/membermessage/delete/?msgId=" + msgid + "&MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
					Message msg = Message.obtain();
					msg.obj = new JSONObject(result.toString()).getString("return");
					msg.what = 2;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

}
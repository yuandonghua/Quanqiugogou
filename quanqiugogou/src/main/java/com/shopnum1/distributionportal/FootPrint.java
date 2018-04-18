package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class FootPrint extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private JSONArray footList;
	private Dialog pBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_collect);
		
		initLayout();
		getData();
	}
	//初始化
	public void initLayout() {
		((TextView)findViewById(R.id.title)).setText("我的足迹");
        ((LinearLayout)findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}
	
	public void getData(){
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		
		new Thread(){
			public void run(){
				StringBuffer result = httpget.getArray("/api/footprintget/?MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					footList = new JSONObject(result.toString()).getJSONArray("data");		
					msg.what = 1;	
				} catch (JSONException e) {
					msg.what = 0;	
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	public void addList(){
		ListView listview = (ListView)findViewById(R.id.listview);
		listview.setAdapter(new ListAdapter());
		registerForContextMenu(listview);
		//商品详情
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int i, long arg3) {
				try {
					Intent intent = new Intent(getApplicationContext(), ProductDetails.class);
					intent.putExtra("guid", footList.getJSONObject(i).getString("ProductGuid"));
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
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if(v==((ListView)findViewById(R.id.listview))){
			menu.add(0, 0, 0, "删除");
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		((ListView)findViewById(R.id.listview)).setAdapter(null);
		int selectedPosition=((AdapterContextMenuInfo)item.getMenuInfo()).position;
		if(item.getItemId()==0){
			try {
				delFootList(footList.getJSONObject(selectedPosition).getString("id"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return super.onContextItemSelected(item);
	}
	
	public void delFootList(final String guid){
		new Thread(){
			@Override
			public void run(){
				httpget.getArray("/api/footprintremove/?id=" + guid + "&AppSign=" + HttpConn.AppSign);
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
			case 0:
				pBar.dismiss();
				((TextView)findViewById(R.id.nocontent)).setText("暂无足迹");
				((TextView)findViewById(R.id.nocontent)).setVisibility(View.VISIBLE);
				break;
			case 1:
				pBar.dismiss();
				addList();
				break;
			case 2:
				if(msg.obj.equals("202"))
					Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getApplicationContext(), "删除失败", Toast.LENGTH_SHORT).show();
				finish();
				startActivity(new Intent(getApplicationContext(), FootPrint.class));
				break;
			case 3:
				((TextView)findViewById(R.id.nocontent)).setVisibility(View.VISIBLE);
				break;
			case 4:
				getData();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			
			return footList.length();
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
				holder.ProductName.setText(footList.getJSONObject(position).getString("ProductName"));
				holder.ShopPrice.setText("￥" + footList.getJSONObject(position).getString("ProductShopPrice"));
				holder.MarketPrice.setHint("￥" + footList.getJSONObject(position).getString("ProductMarketPrice"));
				if(HttpConn.showImage)
					ImageLoader.getInstance().displayImage(footList.getJSONObject(position).getString("ProductOriginalImge"), holder.ProductImage, MyApplication.options);
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

}
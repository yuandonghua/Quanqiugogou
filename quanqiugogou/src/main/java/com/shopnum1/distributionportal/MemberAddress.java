package com.shopnum1.distributionportal;
//我的消息
import com.shopnum1.distributionportal.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
public class MemberAddress extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private ListAdapter adapter;
	private Dialog pBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_address);
		
		initLayout();
		addList();
	}
	//初始化
	public void initLayout() {
		//返回
        ((LinearLayout)findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				setResult(2, getIntent());
				finish();
			}
		});
        
        //新增
        ((LinearLayout)findViewById(R.id.add)).setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View arg0) {
        		startActivityForResult(new Intent(getBaseContext(), MemberAddressEdit.class), 0);
        	}
        });
	}
	
	public void addList(){
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		
		ListView listview = (ListView)findViewById(R.id.listview);
		listview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ListAdapter(this);
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
				String name = ((TextView)v.findViewById(R.id.textView1)).getText().toString();
				String mobile = ((TextView)v.findViewById(R.id.textView2)).getText().toString();
				String address = ((TextView)v.findViewById(R.id.textView3)).getText().toString();
				String code = ((TextView)v.findViewById(R.id.textView4)).getText().toString();
				String guid = ((TextView)v.findViewById(R.id.textView5)).getText().toString();
				Intent intent = getIntent();
				intent.putExtra("name", name);
				intent.putExtra("mobile", mobile);
				intent.putExtra("address", address);
				intent.putExtra("code", code);
				intent.putExtra("guid", guid);
				setResult(1, intent);
				finish();
			}
		});
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				((TextView)findViewById(R.id.nocontent)).setVisibility(View.VISIBLE);
				break;
			case 1:
				((TextView)findViewById(R.id.nocontent)).setVisibility(View.GONE);
				adapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	class ListAdapter extends BaseAdapter{

		private JSONArray addressList = new JSONArray();
		int count = 0;
		
		public JSONObject getInfo(int position) {
			JSONObject result = null;
			try {
				result = addressList.getJSONObject(position);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}
		
		public ListAdapter(Context c) {
			new Thread() {
				@Override
				public void run() {
					Message message = Message.obtain();
					try {
						StringBuffer result = httpget.getArray("/api/address/?MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
						addressList = new JSONObject(result.toString()).getJSONArray("Data");
						if(addressList.length() == 0)
							message.what = 0;
						else
							message.what = 1;	
					} catch (JSONException e) {
						message.what = 0;
						e.printStackTrace();
					}
					handler.sendMessage(message);
					pBar.dismiss();
				}
			}.start();
		}

		@Override
		public int getCount() {
			
			return addressList.length();
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
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_address, parent, false);
				holder.name = (TextView)convertView.findViewById(R.id.textView1);
				holder.mobile = (TextView)convertView.findViewById(R.id.textView2);
				holder.address = (TextView)convertView.findViewById(R.id.textView3);
				holder.code = (TextView)convertView.findViewById(R.id.textView4);
				holder.guid = (TextView)convertView.findViewById(R.id.textView5);
				holder.edit = (ImageView)convertView.findViewById(R.id.edit);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			try {
				holder.name.setText(addressList.getJSONObject(position).getString("NAME"));
				holder.mobile.setText(addressList.getJSONObject(position).getString("Mobile"));

				holder.address.setText(addressList.getJSONObject(position).getString("Address"));
				holder.code.setText(addressList.getJSONObject(position).getString("Code"));
				holder.guid.setText(addressList.getJSONObject(position).getString("Guid"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			holder.edit.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					try {
						Intent intent = new Intent(getBaseContext(), MemberAddressEdit.class);
						intent.putExtra("addressList", addressList.getJSONObject(position).toString());
						startActivityForResult(intent, 0);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			
			return convertView;
		}
		
	}
	
	class ViewHolder {
		TextView name;
		TextView mobile;
		TextView address;
		TextView code;
		TextView guid;
		ImageView edit;
	}
	
	
	
	@Override
	public void onBackPressed() {
		setResult(2, getIntent());
		finish();
		super.onBackPressed();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1){
			addList();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
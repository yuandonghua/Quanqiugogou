package com.shopnum1.distributionportal;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
public class MemberMessage extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private ListAdapter adapter;
	private int requestTime = 1;
	private Dialog pBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_message);
		
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
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, final int arg2, long arg3) {
				Intent intent = new Intent(getBaseContext(), MyMessageDetail.class);
				intent.putExtra("title", ((TextView)v.findViewById(R.id.title)).getText().toString());
				intent.putExtra("time", ((TextView)v.findViewById(R.id.time)).getText().toString());
				intent.putExtra("content", ((TextView)v.findViewById(R.id.content)).getText().toString());
				startActivityForResult(intent, 0);
				
				
				new Thread(){
					public void run(){
						try {
							String guid = adapter.getInfo(arg2).getString("Guid");
							httpget.getArray("/api/membermessageisread/?id=" + guid + "&memLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		});
		
		listview.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(final AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 判断是否滚动到底部
					if (view.getLastVisiblePosition() == view.getCount() - 1)
						adapter.upData("/api/membermessagelist/?pageIndex=" + requestTime + "&pageCount=5&receiveMemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign, false);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

			}
		});
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
				startActivity(new Intent(getApplicationContext(), MemberMessage.class));
				break;
			case 3:
				((TextView)findViewById(R.id.nocontent)).setVisibility(View.VISIBLE);
				break;
			case 12:
				if(msg.obj.equals("202"))
					Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(getApplicationContext(), "删除失败", Toast.LENGTH_SHORT).show();
				finish();
				startActivity(new Intent(getApplicationContext(), MemberMessage.class));
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	class ListAdapter extends BaseAdapter{
		private ArrayList<Bitmap> bm = new ArrayList<Bitmap>();
		private JSONArray msgList = new JSONArray();
		private boolean isend = false; //已发送
		private boolean isdown = false; //在下拉
		int count = 0;
		
		public JSONObject getInfo(int position) {
			JSONObject result = null;
			try {
				result = msgList.getJSONObject(position);
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
									msgList = new JSONArray();
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
										data.append(msgList.toString());
										data.setCharAt(data.length() - 1, ',');
										data.append(new JSONObject(temp.toString()).getJSONArray("Data").toString().substring(1));
									}
									msgList = new JSONArray(data.toString());
									int start = bm.size();
									int end = msgList.length();
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
						StringBuffer result = httpget.getArray("/api/membermessagelist/?pageIndex=1&pageCount=50&receiveMemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
						pBar.dismiss();
						msgList = new JSONObject(result.toString()).getJSONArray("Data");
						count = Integer.parseInt(new JSONObject(result.toString()).get("Count").toString());
						if(count == 0){
							Message message = Message.obtain();
							message.what = 3;
							handler.sendMessage(message);
						} else {
							requestTime++;
						}
						for (int i = 0; i < msgList.length(); i++)
							bm.add(null);
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
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_message, null);
				holder.title = (TextView)convertView.findViewById(R.id.title);
				holder.content = (TextView)convertView.findViewById(R.id.content);
				holder.time = (TextView)convertView.findViewById(R.id.time);
				holder.point = (ImageView)convertView.findViewById(R.id.point);
				holder.del = (Button)convertView.findViewById(R.id.del);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			try {
				holder.title.setText(msgList.getJSONObject(position).getString("Title"));
				holder.content.setText(msgList.getJSONObject(position).getString("Content"));
				holder.time.setText(msgList.getJSONObject(position).getString("CreateTime").replace("/", "-"));
				
				if(msgList.getJSONObject(position).getInt("IsRead") == 0){
					holder.point.setVisibility(View.VISIBLE);
				} else {
					holder.point.setVisibility(View.GONE);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			holder.del.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					final Dialog dialog = new Dialog(MemberMessage.this, R.style.MyDialog);
					View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog, null);
					((TextView)view.findViewById(R.id.dialog_text)).setText("是否删除消息");
					dialog.setContentView(view);
					dialog.show();
					
					((Button)view.findViewById(R.id.no)).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							dialog.dismiss();
						}
					});
					
					((Button)view.findViewById(R.id.yes)).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							try {
								delMessage(msgList.getJSONObject(position).getString("Guid"));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				}
			});
			
			return convertView;
		}
		
		class ViewHolder {
			TextView title;
			TextView content;
			TextView time;
			ImageView point;
			Button del;
		}
		
	}
	
	public void delMessage(final String msgid) {
		new Thread(){
			@Override
			public void run(){	
				try {
					StringBuffer result = httpget.getArray("/api/membermessagedelete/?msgId=" + msgid + "&MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
					Message msg = Message.obtain();
					msg.obj = new JSONObject(result.toString()).getString("return");
					msg.what = 12;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	//返回结果
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1)
			addList();
		super.onActivityResult(requestCode, resultCode, data);
	}

}
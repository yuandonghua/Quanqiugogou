package com.shopnum1.distributionportal;
//积分明细
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
public class MemberScoreDetail extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private int Score;
	private JSONArray ScoreList;
	private ListAdapter adapter; //商品适配器
	private Dialog pBar; //加载进度

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_score_detail);
		initLayout();
	}
	//初始化
	public void initLayout() {
		//返回
		((ImageView) findViewById(R.id.iv_back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
//		//快捷方式
//		((LinearLayout)findViewById(R.id.more)).setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				httpget.showMenu(MemberScoreDetail.this, findViewById(R.id.member_score_detail));
//			}
//		});
		//获取数据
		getData();
		getList();
	}
	
	//获取用户信息
	public void getData(){	
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		
		new Thread(){
			public void run(){
				try {
					StringBuffer result = httpget.getArray("/api/accountget/?MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
					Score = new JSONObject(result.toString()).getJSONObject("AccoutInfo").getInt("Score");
					Message msg = Message.obtain();
					msg.what = 1;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				pBar.dismiss();
				break;
			case 1:
				((TextView)findViewById(R.id.score)).setText("" + Score);
				break;
			case 2:
				ListView listview = (ListView)findViewById(R.id.listview);
				adapter = new ListAdapter();
				listview.setAdapter(adapter);
				pBar.dismiss();
				break;
			default:
				break;
			};
			
			super.handleMessage(msg);
		}
		
	};
	
	//获取积分明细
	public void getList(){
		new Thread(){
			public void run(){
				Message msg = Message.obtain();
				try {
					Log.i("fly", HttpConn.urlName + "/api/getscoremodifylog/?MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
					StringBuffer result = httpget.getArray("/api/getscoremodifylog/?MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
					ScoreList = new JSONObject(result.toString()).getJSONArray("Data");
					msg.what = 2;	
				} catch (JSONException e) {
					msg.what = 0;
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	//适配器
	public class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return ScoreList.length();
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_score, null);
				holder = new ViewHolder();
				holder.text1 = (TextView) convertView.findViewById(R.id.text1);
				holder.text2 = (TextView) convertView.findViewById(R.id.text2);
				holder.text3 = (TextView) convertView.findViewById(R.id.text3);	
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			try {	
				String Date = ScoreList.getJSONObject(position).getString("Date");
				String Memo = ScoreList.getJSONObject(position).getString("Memo");
				int OperateType = ScoreList.getJSONObject(position).getInt("OperateType");
				
				holder.text1.setText(Date.replace("/", "-"));	
				
				if(Memo.equals("")){
					if(OperateType == 0){
						holder.text2.setText("后台提取");
					} else {
						holder.text2.setText("后台充值");
					}
				} else {
					holder.text2.setText(Memo);	
				}	
				
				if(OperateType == 0) {
					holder.text3.setText("-" + ScoreList.getJSONObject(position).getString("OperateScore"));
				} else {
					holder.text3.setText("+" + ScoreList.getJSONObject(position).getString("OperateScore"));
				}
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
			
			return convertView;
		}	
		
	}
	
	static class ViewHolder {
		TextView text1, text2, text3;
	}

}
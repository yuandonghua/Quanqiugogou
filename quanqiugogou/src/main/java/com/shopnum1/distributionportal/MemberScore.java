package com.shopnum1.distributionportal;
//关于
import java.io.IOException;
import java.text.DecimalFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
public class MemberScore extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private int Score, Score2;
	private String Photo;  // 用户头像地址
	private ImageView userImg;
	private MediaPlayer mediaPlayer;
	private JSONArray ProductList;
	private ListAdapter adapter; //商品适配器
	private Dialog pBar; //加载进度
	private int IsNowDay = -1; // 当天是否已签到  0未签到，1已签到

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_score);
		initLayout();
	}
	//初始化
	public void initLayout() {
		//返回
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}
	@Override
	protected void onResume() {
		// 用户头像
				userImg = (ImageView) findViewById(R.id.user_head);
				
				if(HttpConn.getScore){
					((Button) findViewById(R.id.sign)).setBackgroundResource(R.drawable.sign1);
				} else {
					((Button) findViewById(R.id.sign)).setBackgroundResource(R.drawable.sign);
				}
				
				//获取数据
				getData();
				getList();
				// 判断用户今日是否已签到
				getMemSign();
		super.onResume();
	}
	//获取用户信息
	public void getData(){
		((TextView)findViewById(R.id.username)).setText(HttpConn.UserName);
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		
		new Thread(){
			public void run(){
				Message msg = Message.obtain();
				msg.what = 1;
				try {
					StringBuffer result = httpget.getArray("/api/accountget/?MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
					Photo = new JSONObject(result.toString()).getJSONObject("AccoutInfo").getString("Photo");
					Score = new JSONObject(result.toString()).getJSONObject("AccoutInfo").getInt("Score");
					msg.obj = "1";
				} catch (JSONException e) {
					msg.obj = "0";
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	//获取用户信息
	public void getMemSign(){
		
		new Thread(){
			public void run(){
				Message msg = Message.obtain();
				msg.what = 0;
				try {
					StringBuffer result = httpget.getArray1("/Api/AppAPI/AppPublic.ashx?Method=SearchMemSign&MemLoginID=" + HttpConn.username);
					if (result != null && !("").equals(result) && !("null").equals(result)) {
						boolean IsSuccess = new JSONObject(result.toString()).getBoolean("IsSuccess");
						if (IsSuccess) {
							IsNowDay = new JSONObject(result.toString()).getJSONArray("Data").getJSONObject(0).getInt("IsNowDay");
							if (IsNowDay != -1 && IsNowDay == 1) {
								// 已签到
								msg.obj = "1";
							} else {
								msg.obj = "0";
							}
						} else {
							msg.obj = "0";
						}
					} else {
						msg.obj = "0";
					}
				} catch (JSONException e) {
					msg.obj = "0";
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	//签到
	public void scoreSign(){
		((Button) findViewById(R.id.sign)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!HttpConn.getScore){
					new Thread(){
						public void run(){
							try {
								httpget.getArray("/api/memberscoreupdate/?Score=10&MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
								StringBuffer result = httpget.getArray("/api/accountget/?MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
								Score2 = new JSONObject(result.toString()).getJSONObject("AccoutInfo").getInt("Score");
								Message msg = Message.obtain();
								msg.what = 2;
								handler.sendMessage(msg);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}.start();
				}
			}
		});
			
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				String strObj = (String) msg.obj;
				if (strObj != null && !("").equals(strObj)) {
					if (strObj.equals("0")) {
						HttpConn.getScore = false;
						((Button) findViewById(R.id.sign)).setBackgroundResource(R.drawable.sign);
					} else if (strObj.equals("1")) {
						HttpConn.getScore = true;
						((Button) findViewById(R.id.sign)).setBackgroundResource(R.drawable.sign1);
					}
				} else {
					HttpConn.getScore = false;
					((Button) findViewById(R.id.sign)).setBackgroundResource(R.drawable.sign);
				}
				pBar.dismiss();
				break;
			case 1:
				String str = (String) msg.obj;
				if (str != null && !("").equals(str) && !("null").equals(str)) {
					((TextView)findViewById(R.id.score)).setText("积分" + Score);
					// 设置用户头像
					if (Photo == null || Photo.equals("") || Photo.equals("null")) {
						userImg.setImageResource(R.drawable.user_head);
					} else {
						if (HttpConn.showImage) {
							String userImgUrl;
							if (Photo.startsWith("~")) {
								userImgUrl = HttpConn.urlName + Photo.substring(1);
								ImageLoader.getInstance().displayImage(userImgUrl, userImg, MyApplication.options);
							} else {
								ImageLoader.getInstance().displayImage(HttpConn.urlName + Photo, userImg, MyApplication.options);
							}
						} else {
							userImg.setImageResource(R.drawable.user_head);
						}
					}
				} else {
					Toast.makeText(getApplicationContext(), "获取用户信息失败！", Toast.LENGTH_SHORT).show();
				}
				scoreSign();
				
				break;
			case 2:
				if(Score2 > Score){
					HttpConn.getScore = true;
					((TextView)findViewById(R.id.score)).setText("积分" + Score2);
					((Button) findViewById(R.id.sign)).setBackgroundResource(R.drawable.sign1);
					startAnim();
				} else {
					Toast.makeText(MemberScore.this, "今天已签到，明天再来吧！", Toast.LENGTH_SHORT).show();
					HttpConn.getScore = true;
				}
				break;
			case 3:
				GridView gridview = (GridView)findViewById(R.id.gridview);
				gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
				adapter = new ListAdapter();
				gridview.setAdapter(adapter);
				pBar.dismiss();
				
				gridview.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {		
						try {
							Intent intent = new Intent(getBaseContext(), ProductDetails.class);
							intent.putExtra("guid", ProductList.getJSONObject(arg2).getString("Guid"));
							startActivity(intent);
						} catch (JSONException e) {
							e.printStackTrace();
						}	
					}
				});
				break;
			default:
				break;
			};
			
			super.handleMessage(msg);
		}
		
	};
	
	private void startAnim(){
		final ImageView imgv = (ImageView)findViewById(R.id.imgv);
		final AnimationDrawable draw = (AnimationDrawable) imgv.getBackground();
		initBeepSound();
		mediaPlayer.start();
		draw.start();
		int duration = 0; 
        for(int i = 0; i < draw.getNumberOfFrames(); i++){ 
            duration += draw.getDuration(i); 
        } 
        new Handler().postDelayed(new Runnable() { 

            public void run() { 
            	draw.stop();
            	imgv.setVisibility(View.GONE);
            } 

        }, duration);
	}
	
	private void initBeepSound() {
		if (mediaPlayer == null) {
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(0.10f, 0.10f);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}
	
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	
	//获取积分商品
	public void getList(){
		new Thread(){
			public void run(){
				try {
					//StringBuffer result = httpget.getArray("/api/productbysocre/?pageIndex=1&pageSize=10&MemLoginID=" + HttpConn.username + "&AppSign=" + HttpConn.AppSign);
					StringBuffer result = httpget.getArray("/api/GetScoreProductList?AppSign=" + HttpConn.AppSign);
					ProductList = new JSONObject(result.toString()).getJSONArray("Data");
					Message msg = Message.obtain();
					msg.what = 3;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	//商品适配器
	public class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return ProductList.length();
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
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.product_item2, null);
				holder = new ViewHolder();
				holder.text1 = (TextView) convertView.findViewById(R.id.textView1);
				holder.text2 = (TextView) convertView.findViewById(R.id.textView2);
				holder.text3 = (TextView) convertView.findViewById(R.id.textView3);	
				holder.text4 = (TextView) convertView.findViewById(R.id.textView4);
				holder.imageview = (ImageView) convertView.findViewById(R.id.imageView1);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			try {	
				holder.text1.setText(ProductList.getJSONObject(position).getString("Name"));	
				holder.text2.setText("￥" + new DecimalFormat("0.00").format(ProductList.getJSONObject(position).getDouble("ShopPrice")));			
				holder.text3.setText("￥" + new DecimalFormat("0.00").format(ProductList.getJSONObject(position).getDouble("MarketPrice")));
				holder.text4.setVisibility(View.VISIBLE);
				holder.text4.setText("积分可抵" + new DecimalFormat("0.00").format(ProductList.getJSONObject(position).getDouble("SocrePrice")) + "元");
				if(HttpConn.showImage)
					ImageLoader.getInstance().displayImage(ProductList.getJSONObject(position).getString("OriginalImge"), holder.imageview, MyApplication.options);
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
			
			return convertView;
		}	
		
	}
	
	static class ViewHolder {
		ImageView imageview;
		TextView text1, text2, text3, text4;
	}
}
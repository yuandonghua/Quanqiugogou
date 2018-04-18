package com.shopnum1.distributionportal;
//商品评价
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ProductComment extends Activity {
	
	private View view1, view2; //评价和晒单页面
	private TextView textview1, textview2; //评价和晒单按钮
	private int cursor_width= 0; //游标宽度
	private ImageView cursor = null; //游标图片
	private ViewPager viewPager = null; //滑动视图
	private int currentIndex = 0; //滑动视图位置
	
	private HttpConn httpget = new HttpConn();
	private JSONArray commentList1, commentList2;
	private Dialog pBar; //加载进度
	private GridAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.product_comment);
		initLayout();
		initViewpager();
		getData();
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
	
	//初始化ViewPager
	public void initViewpager() { 
		cursor = (ImageView) this.findViewById(R.id.cursor);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		cursor_width = metric.widthPixels / 2;
		LayoutParams params = (LayoutParams)cursor.getLayoutParams();
		params.width = cursor_width;
		cursor.setLayoutParams(params);
		
		LayoutInflater inflater = getLayoutInflater();
		List<View> pager = new ArrayList<View>();
		view1 = inflater.inflate(R.layout.comment_pager, null);
		view2 = inflater.inflate(R.layout.comment_pager, null);
		pager.add(view1);
		pager.add(view2);
		viewPager = (ViewPager) this.findViewById(R.id.viewPager);
		viewPager.setAdapter(new ViewPagerAdapter(pager));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnClickPagerChange());
		textview1 = (TextView)findViewById(R.id.textview1);
		textview2 = (TextView)findViewById(R.id.textview2);
		textview1.setOnClickListener(new Myclick(0));
		textview2.setOnClickListener(new Myclick(1));
	}
	//按钮切换
	class Myclick implements View.OnClickListener {
		int mark;

		public Myclick(int dex) {
			mark = dex;
		}

		@Override
		public void onClick(View arg0) {
			viewPager.setCurrentItem(mark);
		}

	}
	//滑动页面切换
	class MyOnClickPagerChange implements android.support.v4.view.ViewPager.OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int arg0) {
			TranslateAnimation animation = null;
			switch (arg0) {
			case 0:
				textview1.setTextColor(Color.RED);
				textview2.setTextColor(Color.BLACK);
				if (currentIndex == 1) {
					animation = new TranslateAnimation(cursor_width, 0, 0, 0);
				}
				break;
			case 1:
				textview1.setTextColor(Color.BLACK);
				textview2.setTextColor(Color.RED);
				if (currentIndex == 0) {
					animation = new TranslateAnimation(0, cursor_width, 0, 0);
				}
				break;
			}
			currentIndex = arg0;
			animation.setFillAfter(true);
			animation.setDuration(300);
			cursor.startAnimation(animation);
		}

	}
	
	public void getData(){
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		
		new Thread(){
			public void run(){
				StringBuffer result = httpget.getArray("/api/getProductCommentByProductGuid/?productGuid="+getIntent().getStringExtra("guid") +"&AppSign="+HttpConn.AppSign+"&memLoginId="+HttpConn.username);
				StringBuffer result2 = httpget.getArray("/api/getBaskOrderLogByProductGuid/?memLoginId="+HttpConn.username+"&productGuid=" + getIntent().getStringExtra("guid")+"&AppSign="+HttpConn.AppSign);
				Log.i("fly", result.toString());
				Message msg = Message.obtain();
				try {
					if(new JSONObject(result.toString()).getString("Data") == "null"){
						commentList1 = new JSONArray();
					}else{
						commentList1 = new JSONObject(result.toString()).getJSONArray("Data");
					}
					if(new JSONObject(result2.toString()).getString("Data") == "null"){
						commentList2 = new JSONArray();
					}else{
						commentList2 = new JSONObject(result2.toString()).getJSONArray("Data");
					}
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
		ListView listview1 = (ListView)view1.findViewById(R.id.listview);
		listview1.setSelector(new ColorDrawable(Color.TRANSPARENT));;
		listview1.setAdapter(new MyAdapter(commentList1, 1));
		
		ListView listview2 = (ListView)view2.findViewById(R.id.listview);
		listview2.setSelector(new ColorDrawable(Color.TRANSPARENT));;
		listview2.setAdapter(new MyAdapter(commentList2, 2));
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				pBar.dismiss();
				break;
			case 1:
				addList();
				pBar.dismiss();
				break;
			case 2:
				adapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
		
	
	//适配器
	class MyAdapter extends BaseAdapter{
		
		JSONArray commentList;
		int id;
		
		public MyAdapter(JSONArray commentList, int id){
			this.commentList = commentList;
			this.id = id;
		}

		@Override
		public int getCount() {
			
			return commentList.length();
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
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_comment, parent, false); 
				holder.userid = (TextView)convertView.findViewById(R.id.userid);
				holder.content = (TextView)convertView.findViewById(R.id.comment);
				holder.title = (TextView)convertView.findViewById(R.id.title);
				holder.time = (TextView)convertView.findViewById(R.id.time);
				holder.gridview = (GridView)convertView.findViewById(R.id.gridview);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			try {
				holder.userid.setText(commentList.getJSONObject(position).getString("MemLoginID"));
				holder.content.setText(commentList.getJSONObject(position).getString("Content"));
				
				if(id == 1){
					holder.time.setText(commentList.getJSONObject(position).getString("SendTime"));
				} else {
					holder.title.setVisibility(View.VISIBLE);
					holder.title.setText(commentList.getJSONObject(position).getString("Title"));
					holder.time.setText(commentList.getJSONObject(position).getString("CreateTime"));
					String Image = commentList.getJSONObject(position).getString("Image").replace("|", ",");
					if(!Image.equals("")){
						holder.gridview.setVisibility(View.VISIBLE);
						adapter = new GridAdapter(Image.split(","));
						holder.gridview.setAdapter(adapter);
					} else {
						holder.gridview.setVisibility(View.GONE);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return convertView;
		}
		
		class ViewHolder {
			TextView userid;
			TextView title;
			TextView content;
			TextView time;
			GridView gridview;
		}
	}
	
	class GridAdapter extends BaseAdapter{
		
		String[] url;
		public GridAdapter(final String[] url){
			this.url = url;
		}

		@Override
		public int getCount() {
			
			return url.length;
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
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			if(convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_show, null);
			}
			ImageView imageview = (ImageView)convertView.findViewById(R.id.img);
			ImageLoader.getInstance().displayImage(HttpConn.urlName + url[arg0], imageview, MyApplication.options);
			
			return convertView;
		}
		
	}
	
}
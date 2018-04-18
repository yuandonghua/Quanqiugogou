package com.shopnum1.distributionportal;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.ViewPagerAdapter;
//商品评价
import java.util.ArrayList;
import java.util.List;
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
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BrandCenter extends Activity {

	private View view1, view2; 
	private TextView textview1, textview2; 
	private int cursor_width = 0; // 游标宽度
	private ImageView cursor = null; // 游标图片
	private ViewPager viewPager = null; // 滑动视图
	private int currentIndex = 0; // 滑动视图位置

	private HttpConn httpget = new HttpConn();
	private JSONArray AllList, BigList;
	private GridViewAdapter adapter1, adapter2;
	private Dialog pBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.brand_center);
		initLayout();
		initViewpager();
		getBrandAll();
		getBrandBig();
	}

	// 初始化
	public void initLayout() {
		// 返回
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	// 初始化ViewPager
	public void initViewpager() {
		cursor = (ImageView) this.findViewById(R.id.cursor);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		cursor_width = metric.widthPixels / 2;
		LayoutParams params = (LayoutParams) cursor.getLayoutParams();
		params.width = cursor_width;
		cursor.setLayoutParams(params);

		LayoutInflater inflater = getLayoutInflater();
		List<View> pager = new ArrayList<View>();
		view1 = inflater.inflate(R.layout.gridview, null);
		view2 = inflater.inflate(R.layout.gridview, null);
		pager.add(view1);
		pager.add(view2);
		viewPager = (ViewPager) this.findViewById(R.id.viewPager);
		viewPager.setAdapter(new ViewPagerAdapter(pager));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnClickPagerChange());
		textview1 = (TextView) findViewById(R.id.textview1);
		textview2 = (TextView) findViewById(R.id.textview2);
		textview1.setOnClickListener(new Myclick(0));
		textview2.setOnClickListener(new Myclick(1));
	}

	// 按钮切换
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

	// 滑动页面切换
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

	public void getBrandAll() {
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		new Thread(){
			public void run(){
				StringBuffer result = httpget.getArray("/api/productbrandlistisrecommend/?IsRecommend=1&AppSign=" + HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					AllList = new JSONObject(result.toString()).getJSONArray("data");
					msg.what = 1;
				} catch (JSONException e) {
					msg.what = 0;
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	public void getBrandBig() {
		new Thread(){
			public void run(){
				StringBuffer result = httpget.getArray("/api/productbrandlist/?AppSign=" + HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					BigList = new JSONObject(result.toString()).getJSONArray("Data");		
					msg.what = 2;
				} catch (JSONException e) {
					msg.what = 0;
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
				GridView gridview1 = ((GridView) view1.findViewById(R.id.gridview));
				gridview1.setSelector(new ColorDrawable(Color.TRANSPARENT));
				adapter1 = new GridViewAdapter(AllList);
				gridview1.setAdapter(adapter1);
				pBar.dismiss();
				
				gridview1.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
						TextView text1 = (TextView) v.findViewById(R.id.text1);
						TextView text2 = (TextView) v.findViewById(R.id.text2);
						Intent intent = new Intent(getBaseContext(), BrandDetail.class);
						intent.putExtra("Name", text1.getText().toString());
						intent.putExtra("Guid", text2.getText().toString());
						startActivity(intent);
					}
				});
				break;
			case 2:
				GridView gridview2 = ((GridView) view2.findViewById(R.id.gridview));
				gridview2.setSelector(new ColorDrawable(Color.TRANSPARENT));
				adapter2 = new GridViewAdapter(BigList);
				gridview2.setAdapter(adapter2);
				
				gridview2.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
						TextView text1 = (TextView) v.findViewById(R.id.text1);
						TextView text2 = (TextView) v.findViewById(R.id.text2);
						TextView text3 = (TextView) v.findViewById(R.id.text3);
						Intent intent = new Intent(getBaseContext(), BrandDetail.class);
						intent.putExtra("Name", text1.getText().toString());
						intent.putExtra("Guid", text2.getText().toString());
						intent.putExtra("Logo", text3.getText().toString());
						startActivity(intent);
					}
				});
				break;
			}
			super.handleMessage(msg);
		}

	};

	class GridViewAdapter extends BaseAdapter {
		
		JSONArray dataList;
		public GridViewAdapter(JSONArray dataList){
			this.dataList = dataList;
		} 

		@Override
		public int getCount() {
			return dataList.length();
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
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_brand, parent, false);
			}
			try {
				TextView text1 = (TextView) convertView.findViewById(R.id.text1);
				TextView text2 = (TextView) convertView.findViewById(R.id.text2);
				TextView text3 = (TextView) convertView.findViewById(R.id.text3);
				
				text1.setTextColor(Color.BLACK);
				text2.setVisibility(View.GONE);
				text1.setText(dataList.getJSONObject(position).getString("Name"));
				text2.setText(dataList.getJSONObject(position).getString("Guid"));
				
				String imgurl = dataList.getJSONObject(position).getString("Logo");
				text3.setText(imgurl);
				Log.i("fly", imgurl);
				ImageView imageview = (ImageView) convertView.findViewById(R.id.img);
				if (HttpConn.showImage)
					ImageLoader.getInstance().displayImage(imgurl, imageview, MyApplication.options);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return convertView;
		}

	}

}
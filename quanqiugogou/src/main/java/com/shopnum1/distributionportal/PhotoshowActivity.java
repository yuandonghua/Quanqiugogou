package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PhotoshowActivity extends Activity {
    ViewPager viewPager;
    ViewPagerAdapter adapter;
    List<Map<String, Object>> data;
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private String[] detailicon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoshow);
        init();
    }

    public void init() {
        viewPager = (ViewPager) findViewById(R.id.mypager);
        data = getData();
        imageLoader=ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.notype) // 在ImageView加载过程中显示图片
                .showImageForEmptyUri(R.drawable.notype) // image连接地址为空时
                .showImageOnFail(R.drawable.notype) // image加载失败
                .cacheInMemory(true) // 加载图片时会在内存中加载缓存
                .cacheOnDisc(true) // 加载图片时会在磁盘中加载
                .build();
        adapter = new ViewPagerAdapter(data);
        viewPager.setAdapter(adapter);
    	((LinearLayout) findViewById(R.id.back))
		.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}

		});
    }

//这个方法长度是动态的，可以改成你从服务器获取的图片，这样数量就不确定啦
    public List<Map<String, Object>> getData() {
    	detailicon=this.getIntent().getExtras().getStringArray("icon");
    	 List<Map<String, Object>> mdata = new ArrayList<Map<String, Object>>();
    	for(int i=0;i<detailicon.length;i++){
    		 
    	        Map<String, Object> map = new HashMap<String, Object>();
    	        map.put("url", HttpConn.urlName+detailicon[i]);
    	        map.put("view", new ImageView(this));
    	        mdata.add(map);
    	}
      

       
         return  mdata;
    }

    public class ViewPagerAdapter extends PagerAdapter {

        List<Map<String,Object>> viewLists;

        public ViewPagerAdapter(List<Map<String,Object>> lists)
        {
            viewLists = lists;
        }

        @Override
        public int getCount() {                                                                 //获得size
            // TODO Auto-generated method stub
            return viewLists.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            // TODO Auto-generated method stub
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(View view, int position, Object object)                       //销毁Item
        {
            ImageView x=(ImageView)viewLists.get(position).get("view");
            x.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ((ViewPager) view).removeView(x);
        }

        @Override
        public Object instantiateItem(View view, int position)                                //实例化Item
        {
            ImageView x=(ImageView)viewLists.get(position).get("view");
            x.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageLoader.displayImage(viewLists.get(position).get("url").toString(), x,options);
            ((ViewPager) view).addView(x, 0);

            return viewLists.get(position).get("view");
        }

    }
}
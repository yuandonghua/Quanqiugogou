package com.shopnum1.distributionportal.util;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.Application;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class MyApplication extends Application {
	
	public static DisplayImageOptions options;
	
	public static String agentId = "";
	
	 //缓存agentid的键值
    public static final String AGENT_ID_KEY = "agent_id";
	
	private HttpConn httpget = new HttpConn();
	//public static ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	public void onCreate() {
		super.onCreate();
				ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs().build();
				ImageLoader.getInstance().init(config);
				getSign();
				agentId = ShareUtils.getString(getApplicationContext(), AGENT_ID_KEY, "");
	}
	public void getSign() {
		new Thread() {
			public void run() {
				StringBuffer result = httpget.getArray("/api/SignSet/?isSet=1");
				try {
					HttpConn.AppSign = new JSONObject(result.toString())
							.getString("AppSign");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}

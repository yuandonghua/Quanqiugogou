package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.ImplementDao;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.ShareUtils;
//启动
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class InitActivity extends Activity {

	private HttpConn httpget = new HttpConn();
	private ImplementDao database = new ImplementDao(this);
	private Boolean exit = false; // 是否退出
	private ImageView[] tips, imgViews; // 引导页图片

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init);
		getSign();
	}

	public void getSign() {
		new Thread() {
			public void run() {
				Message msg = Message.obtain();
				StringBuffer result = httpget.getArray("/api/SignSet/?isSet=1");
				try {
					HttpConn.AppSign = new JSONObject(result.toString())
							.getString("AppSign");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				handler.sendEmptyMessage(0);
			}
		}.start();
	}

	public void getNumber() {
		SharedPreferences mPerferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		// 是否登录
		boolean islogin = mPerferences.getBoolean("islogin", false);
		String username = mPerferences.getString("name", "");
		if (islogin) {
			HttpConn.isLogin = true;
			HttpConn.username = toUTF8(username);
			HttpConn.UserName = username;
			new Thread() {
				public void run() {
					String shoppingcartgetUrl = "/api/shoppingcartget/?loginId="
							+ HttpConn.username + "&AppSign="
							+ HttpConn.AppSign + "&agentID="+MyApplication.agentId;
					StringBuffer result = httpget
							.getArray(shoppingcartgetUrl);
					try {
						HttpConn.cartNum = new JSONObject(result.toString())
								.getJSONArray("Data").length();
						Log.i("fly", HttpConn.AppSign);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		// 是否显示导航
		boolean showImage = mPerferences.getBoolean("showImage", true);
		if (showImage) {
			initLayout();
			Editor mEditor = mPerferences.edit();
			mEditor.putBoolean("showImage", false);
			mEditor.commit();
		} else {
			((ImageView) findViewById(R.id.img))
					.setBackgroundResource(R.drawable.guid);
			// ((ImageView)findViewById(R.id.img)).setBackgroundResource(R.drawable.startview);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (exit){
						
					
						finish();
					}
					else{
						SelectAgent();
						
					}
					finish();
				}
			}, 1000);
		}
	}
	
	 private void SelectAgent() {
			// TODO Auto-generated method stub
//			 String agentId = ShareUtils.getString(getApplicationContext(), HttpConn.AGENT_ID_KEY, "");
		 String agentId = MyApplication.agentId;
		 if(agentId==null || "".equals(agentId)){
				//还没有选择分销商,前去选择分销商
				startActivity(new Intent(getApplicationContext(),SelectAgentActivity.class));
			}else{
				startActivity(new Intent(getApplicationContext(),
						MainActivity.class)); // 进入主页
			}
		}
	public String toUTF8(String name) {
		String username = "";
		try {
			username = URLEncoder.encode(name, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return username;
	}
	// 显示引导图
	public void initLayout() {
		ViewGroup group = (ViewGroup) findViewById(R.id.viewGroup);
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
		// int[] img = new int[]{R.drawable.startview1, R.drawable.startview2,
		// R.drawable.startview3};
		int[] img = new int[] { R.drawable.guid1, R.drawable.guid2,
				R.drawable.guid3 };

		tips = new ImageView[img.length];
		for (int i = 0; i < tips.length; i++) {
			ImageView imageView = new ImageView(this);
			imageView.setLayoutParams(new LayoutParams(10, 10));
			tips[i] = imageView;
			if (i == 0) {
				tips[i].setBackgroundResource(R.drawable.point_white1);
			} else {
				tips[i].setBackgroundResource(R.drawable.point_white);
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 5;
			layoutParams.rightMargin = 5;
			layoutParams.topMargin = 5;
			layoutParams.bottomMargin = 10;
			group.addView(imageView, layoutParams);
		}

		imgViews = new ImageView[img.length];
		for (int i = 0; i < imgViews.length; i++) {
			ImageView imageView = new ImageView(this);
			imgViews[i] = imageView;
			imageView.setBackgroundResource(img[i]);
		}
		viewPager.setAdapter(new MyAdapter());
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				for (int i = 0; i < tips.length; i++) {
					if (i == arg0) {
						tips[i].setBackgroundResource(R.drawable.point_white1);
						if (i == (imgViews.length - 1)) {
							imgViews[i]
									.setOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View arg0) {
//											startActivity(new Intent(
//													getApplicationContext(),
//													MainActivity.class));
												SelectAgent();
											finish();
										}
									});
						}
					} else {
						tips[i].setBackgroundResource(R.drawable.point_white);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	// 引导图适配器
	class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return imgViews.length;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(imgViews[position]);
		}

		@Override
		public Object instantiateItem(View container, int position) {
			((ViewPager) container).addView(imgViews[position], 0);
			return imgViews[position];
		}

	}

	public void login() {
		new Thread() {
			public void run() {
				String name = PreferenceManager.getDefaultSharedPreferences(
						getApplicationContext()).getString("name", "");
				String pwd = PreferenceManager.getDefaultSharedPreferences(
						getApplicationContext()).getString("pwd", "");

				try {
					StringBuffer result = httpget
							.getArray("/api/accountlogin/?MemLoginID="
									+ URLEncoder.encode(name, "utf-8")
									+ "&Pwd=" + pwd + "&AppSign="
									+ HttpConn.AppSign);
					if (!new JSONObject(result.toString()).getString("return")
							.equals("202")) {
						HttpConn.isLogin = false;
						Editor editor = PreferenceManager
								.getDefaultSharedPreferences(
										getApplicationContext()).edit();
						editor.putBoolean("islogin", false);
						editor.commit();
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	// 获取背景图片
	public void getBitmap() {
		new Thread() {
			@Override
			@SuppressWarnings("deprecation")
			public void run() {
				StringBuffer ImageList = httpget.getArray("/api/welcome/?"
						+ "AppSign=" + HttpConn.AppSign);
				String path = "";
				try {
					path = new JSONObject(ImageList.toString())
							.getJSONArray("ImageList").getJSONObject(0)
							.getString("Value");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Bitmap photo = database.imgQuery("picimage", path);
				if (photo != null) {
					Message msg = new Message();
					msg.obj = photo;
					handler.sendMessage(msg);
				} else {
					Bitmap temp = httpget.getImage(path);
					if (temp != null) {
						float width = temp.getWidth();
						float height = temp.getHeight();
						float scaleX = getWindowManager().getDefaultDisplay()
								.getWidth() / width;
						float scaleY = getWindowManager().getDefaultDisplay()
								.getHeight() / height;
						Matrix matrix = new Matrix();
						matrix.postScale(scaleX, scaleY);
						Bitmap bitmap = Bitmap.createBitmap(temp, 0, 0,
								(int) width, (int) height, matrix, true);
						database.imgInsert("picimage", path, bitmap);
						Message msg = new Message();
						msg.obj = bitmap;
						handler.sendMessage(msg);
						if (!temp.isRecycled()) {
							temp.recycle();
							temp = null;
							System.gc();
						}
					}
				}
			}
		}.start();
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			((ImageView) findViewById(R.id.img))
					.setImageBitmap((Bitmap) msg.obj); // 设置背景图片
			super.handleMessage(msg);
			switch (0) {
			case 0:
				getNumber();
				break;

			default:
				break;
			}
		}

	};

	// 退出程序
	@Override
	public void onBackPressed() {
		exit = true;
		super.onBackPressed();
	}

}
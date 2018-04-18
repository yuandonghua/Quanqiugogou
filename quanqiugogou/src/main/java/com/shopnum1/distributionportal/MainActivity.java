package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.adater.GGPagerAdapter;
import com.shopnum1.distributionportal.adater.ImageAdapter;
import com.shopnum1.distributionportal.adater.MatchAdapter;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.MyScrollView;
import com.shopnum1.distributionportal.util.NoScrollGridView;
import com.shopnum1.distributionportal.util.ShareUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract.Constants;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zxing.activity.CaptureActivity;
import com.zxing.bean.Announcement;

public class MainActivity extends Activity implements OnClickListener {
	private LinearLayout tv_erwei;// 二维码
	private EditText rl_search;
	private LinearLayout tv_xiaoxi;// 消息
	private ViewPager view_banner;// 轮播广告
	private ViewFlipper details;
	private TextView tv_hot;// 热卖
	private TextView tv_big;// 大牌
	private TextView tv_new;// 新品
	private TextView tv_group;// 团购
	private ImageView banner1;// 广告1
	private ImageView banner2;// 广告2
	private ImageView iv_xianshi;// 限时抢购
	private ImageView iv_jinri1;// 今日活动1
	private ImageView iv_jinri2;// 今日活动2
	private ImageView iv_jinri3;// 今日活动3
	private NoScrollGridView product_grid;// 推荐商品列表
	private NoScrollGridView type_grid;// 分类列表
	private RelativeLayout relativeLayout1;
	private ArrayList<View> bannerList = new ArrayList<View>();// 轮播广告View
	private ArrayList<JSONObject> TypeList = new ArrayList<JSONObject>();// 分类View
	private GGPagerAdapter ggadapter;
	private LinearLayout point_group;
	private int verCode, newCode;
	private ImageAdapter typeAdapter;
	private MatchAdapter matchAdapter;
	private ArrayList<JSONObject> product_list = new ArrayList<JSONObject>();
	private int width, fileSize, sumSize;
	public static MainActivity instance;
	private ProgressDialog pBar;
	private HttpConn httpGet = new HttpConn();
	private InputStream is;
	private FileOutputStream fos;
	private ArrayList<View> point_list = new ArrayList<View>();
	private Drawable mActionBarBackgroundDrawable;
	List<Announcement> announcements = new ArrayList<Announcement>();
    private FrameLayout actionbarbg;
	private Runnable r = new Runnable() {
		@Override
		public void run() {
			Message msg = handler.obtainMessage();
			msg.what = 1;
			handler.sendMessageDelayed(msg, 5000);
		}
	};
	private JSONArray qqgArray;// 全球购

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		instance = this;
		 mActionBarBackgroundDrawable = getResources().getDrawable(R.drawable.title);
		    //    mActionBarBackgroundDrawable1= getResources().getDrawable(R.drawable.title_bg);
		        mActionBarBackgroundDrawable.setAlpha(0);
		        getActionBar().setBackgroundDrawable(mActionBarBackgroundDrawable);        
		        ((MyScrollView) findViewById(R.id.scrollview)).setOnScrollChangedListener(mOnScrollChangedListener);
		        if (Build.VERSION.SDK_INT <Build.VERSION_CODES.JELLY_BEAN_MR1) {
		            mActionBarBackgroundDrawable.setCallback(mDrawableCallback);
		        }
		        
		initLayout();
		
		getData();
		update();
	}

//	 private void SelectAgent() {
//		// TODO Auto-generated method stub
//		 String agentId = ShareUtils.getString(getApplicationContext(), HttpConn.AGENT_ID_KEY, "");
//		if(agentId==null || "".equals(agentId)){
//			//还没有选择分销商,前去选择分销商
//			startActivity(new Intent(MainActivity.this,SelectAgentActivity.class));
//		}
//	}

	private MyScrollView.OnScrollChangedListener mOnScrollChangedListener = new MyScrollView.OnScrollChangedListener() {
		    
			@Override
			public void onScrollChanged(ScrollView who, int l, int t, int oldl,int oldt) {
				// TODO Auto-generated method stub
				 final int headerHeight = findViewById(R.id.scor1).getHeight() - getActionBar().getHeight();
				  int newAlpha = (t * 255)/(headerHeight/2);
				  if(newAlpha>255){
					  newAlpha=255;
				  }
		            mActionBarBackgroundDrawable.setAlpha(newAlpha);
		        //    mActionBarBackgroundDrawable1.setAlpha(255-newAlpha);
			}
	    };

	    private Drawable.Callback mDrawableCallback = new Drawable.Callback() {
	        @Override
	        public void invalidateDrawable(Drawable who) {
	            getActionBar().setBackgroundDrawable(who);
	        }
	     
	        @Override
	        public void scheduleDrawable(Drawable who, Runnable what, long when) {
	        }
	     
	        @Override
	        public void unscheduleDrawable(Drawable who, Runnable what) {
	        }
	    };

	  
	@Override
	protected void onResume() {
		super.onResume();
		getNet();
//		SelectAgent();
	}

	private void getNet() {
		httpGet.getNetwork(this); // 判断网络
		if (HttpConn.isNetwork) {
			if (HttpConn.isLogin)
				if (HttpConn.cartNum > 0) {
					((TextView) findViewById(R.id.num))
							.setVisibility(View.VISIBLE);
					((TextView) findViewById(R.id.num))
							.setText(HttpConn.cartNum + "");
				} else {
					((TextView) findViewById(R.id.num))
							.setVisibility(View.GONE);
				}
		} else {
			httpGet.setNetwork(MainActivity.this); // 设置网络
		}
	}
	/**
	 * 自动更新
	 */
	public void update() {
		HttpUtils hu = new HttpUtils();
		hu.send(HttpMethod.GET, HttpConn.urlName+"/DownLoad/main/VersionCode.xml",
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
					
					}
					@Override
					public void onSuccess(ResponseInfo<String> data) {
						if (data.result.toString() != "") {
							newCode = parser(data.result.toString());
							try {
								verCode = getPackageManager().getPackageInfo(
										getPackageName(), 0).versionCode;
								if (newCode > verCode)
									doNewVersionUpdate();

							} catch (NameNotFoundException e) {
								e.printStackTrace();
							}
						}
					}
				});
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:

				if (bannerList.size() > 3) {
					view_banner.setCurrentItem(view_banner.getCurrentItem() + 1);
				} else {
					if (view_banner.getCurrentItem() == bannerList.size() - 1) {
						view_banner.setCurrentItem(0);
					} else {
						view_banner.setCurrentItem(view_banner.getCurrentItem() + 1);
					}
				}
				handler.postDelayed(r, 5000);
				break;
			case 2:
				pBar.setProgress(sumSize * 100 / fileSize);
				break;
			case 3:
				pBar.cancel();
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(Environment
						.getExternalStorageDirectory() + "/download/",
						"P8686.apk")),
						"application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
				break;

			}
		}
	};

	private void doNewVersionUpdate() {
		Dialog dialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle("软件更新")
				.setMessage("发现有新版本")
				.setPositiveButton("立即更新",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								pBar = new ProgressDialog(MainActivity.this);
								// 设置点击其他地方消失掉的问题
								pBar.setCanceledOnTouchOutside(false);
								pBar.setTitle("正在下载...");
								pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
								pBar.setButton("取消",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												try {
													if (is != null) {
														is.close();
													}
													if (fos != null) {
														fos.close();
													}

													File file = new File(
															Environment
																	.getExternalStorageDirectory()
																	+ "/download/",
															"P8686fxs.apk");
													if (file.exists()) {
														file.delete();
													}
													dialog.dismiss();
												} catch (IOException e) {
													e.printStackTrace();
												}
											}
										});
								downFile(HttpConn.urlName
										+ "/DownLoad/main/fxmh_v3.3.apk");

							}
						})
				.setNegativeButton("暂不更新",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						}).create();
		dialog.show();
	}

	public void downFile(final String path) {
		pBar.show();
		new Thread() {
			@Override
			public void run() {
				try {
					URL url = new URL(path);
					URLConnection conn = url.openConnection();
					fileSize = conn.getContentLength();
					is = conn.getInputStream();
					File file = new File(
							Environment.getExternalStorageDirectory()
									+ "/download/", "P8686.apk");
					if (!file.exists()) {
						file.createNewFile();
					} else {
						file.delete();
						file.createNewFile();
					}
					fos = new FileOutputStream(file);
					byte[] b = new byte[1024];
					while (sumSize < fileSize) {
						int len = is.read(b);
						sumSize += len;
						fos.write(b, 0, len);
						fos.flush();
						Message msg = new Message();
						msg.what = 2;
						handler.sendMessage(msg);
					}
					is.close();
					fos.close();
					Message msg = new Message();
					msg.what = 3;
					handler.sendMessage(msg);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public int parser(String content) {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			StringReader in = new StringReader(content);
			parser.setInput(in);
			int result = parser.getEventType();
			while (result != XmlPullParser.END_DOCUMENT) {
				switch (result) {
				case XmlPullParser.START_TAG:
					if ("versionCode".equals(parser.getName()))
						return Integer.parseInt(parser.nextText().toString());
					break;
				}
				result = parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	/**
	* 设置ActionBar的布局
	* @param layoutId 布局Id
	* 
	* */
	public void setActionBarLayout( int layoutId ){
		   ActionBar actionBar = getActionBar( );
		    if( null != actionBar ){
		    	 actionBar.setTitle(""); 
		        actionBar.setDisplayShowHomeEnabled( false );
		        actionBar.setDisplayShowCustomEnabled(true);
		      
		        LayoutInflater inflator = (LayoutInflater)   this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		        View v = inflator.inflate(layoutId, null);
		        ActionBar.LayoutParams layout = new     ActionBar.LayoutParams(android.app.ActionBar.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		        actionBar.setCustomView(v,layout);
		        tv_erwei = (LinearLayout) v.findViewById(R.id.tv_erwei);
				rl_search = (EditText) v.findViewById(R.id.sousuo);
				tv_xiaoxi = (LinearLayout) v.findViewById(R.id.tv_xiaoxi);
				actionbarbg=(FrameLayout) v.findViewById(R.id.actionbarbg);
			//	actionbarbg.setBackgroundDrawable(mActionBarBackgroundDrawable1);
				//二维码
				tv_erwei.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						startActivityForResult(new Intent(getBaseContext(),
								CaptureActivity.class), 0);
					}
				});
				//消息
				tv_xiaoxi.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (HttpConn.isLogin) {
							startActivity(new Intent(getBaseContext(), MemberMessage.class));
						}else{
							Toast.makeText(MainActivity.this, "您未登录！", 1).show();
						}
					}
				});
				//搜索框
				rl_search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
				rl_search.setOnEditorActionListener(new OnEditorActionListener() {
					
					@Override
					public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
						// TODO Auto-generated method stub
						if (arg1==EditorInfo.IME_ACTION_SEARCH ||(arg2!=null&&arg2.getKeyCode()== KeyEvent.KEYCODE_ENTER)) {                
							// 先隐藏键盘
							((InputMethodManager) rl_search.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
							Intent intent = new Intent(getApplicationContext(), SearchResult.class);
							intent.putExtra("title", "搜索结果");
							intent.putExtra("type", "search");
							intent.putExtra("typeid", "0");
							intent.putExtra("searchstr", rl_search.getText().toString().trim());
							startActivity(intent);			
							return true;             
						}  
							return false;
					}
				});
				
				
		    }
	}
	/**
	 * 初始化接口
	 */
	@SuppressLint("NewApi")
	private void initLayout() {
	
		setActionBarLayout(R.layout.mianactionbar );
		details = (ViewFlipper) this.findViewById(R.id.details);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;// 宽度
		
		view_banner = (ViewPager) this.findViewById(R.id.view_banner);
		tv_hot = (TextView) this.findViewById(R.id.tv_hot);
		tv_big = (TextView) this.findViewById(R.id.tv_big);
		tv_new = (TextView) this.findViewById(R.id.tv_new);
		tv_group = (TextView) this.findViewById(R.id.tv_group);
		banner1 = (ImageView) this.findViewById(R.id.banner1);
		banner2 = (ImageView) this.findViewById(R.id.banner2);
		iv_xianshi = (ImageView) findViewById(R.id.iv_xianshi);
		iv_jinri1 = (ImageView) this.findViewById(R.id.iv_jinri1);
		iv_jinri2 = (ImageView) this.findViewById(R.id.iv_jinri2);
		iv_jinri3 = (ImageView) this.findViewById(R.id.iv_jinri3);
		product_grid = (NoScrollGridView) this.findViewById(R.id.product_grid);
		type_grid = (NoScrollGridView) this.findViewById(R.id.type_grid);
		point_group = (LinearLayout) this.findViewById(R.id.point_group);
		relativeLayout1 = (RelativeLayout) this.findViewById(R.id.relativeLayout1);
//		tv_erwei.setOnClickListener(this);
//		tv_xiaoxi.setOnClickListener(this);
//		rl_search.setOnClickListener(this);
		iv_xianshi.setOnClickListener(this);
		iv_jinri1.setOnClickListener(this);
		iv_jinri2.setOnClickListener(this);
		iv_jinri3.setOnClickListener(this);
		tv_big.setOnClickListener(this);
		tv_new.setOnClickListener(this);
		tv_group.setOnClickListener(this);
		tv_hot.setOnClickListener(this);
		type_grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {
					
					
//					Intent intent = new Intent(getBaseContext(), BrandDetail.class);
//					JSONObject obj= (JSONObject) typeAdapter.getItem(position);
//					JSONArray descriptionArray = obj.getJSONArray("Description");
//					JSONObject descriptionArrayObj1  = descriptionArray.getJSONObject(0);
//					String name  = obj.getString("Name");
//					String productCategoryID  = descriptionArrayObj1.getString("FatherID");
//					
//					intent.putExtra("Name", name);
//					intent.putExtra("ProductCategoryID",productCategoryID);
////					intent.putExtra("Logo", text3.getText().toString());
//					startActivity(intent);
					
					
					JSONObject obj= (JSONObject) typeAdapter.getItem(position);
					JSONArray descriptionArray = obj.getJSONArray("Description");
					JSONObject descriptionArrayObj1  = descriptionArray.getJSONObject(0);
					String name  = obj.getString("Name");
					String productCategoryID  = descriptionArrayObj1.getString("FatherID");
					String ID = obj.getInt("ID")+"";
					
					Intent intent = new Intent(MainActivity.this,SearchResult.class);
//					Intent intent = new Intent(MainActivity.this,BrandDetail.class);
					intent.putExtra("title",name);
					intent.putExtra("ProductCategoryID",productCategoryID);
					intent.putExtra("type", "list");
					intent.putExtra("typeid",Integer.parseInt(productCategoryID));
					intent.putExtra("searchstr", "");
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		view_banner.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				for (int i = 0; i < point_list.size(); i++) {
					if (i == position % point_list.size()) {
						point_list.get(i).setBackgroundResource(
								R.drawable.point1);
					} else {
						point_list.get(i).setBackgroundResource(
								R.drawable.point);
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

		product_grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {
					Intent intent = new Intent(MainActivity.this,
							ProductDetails.class);
					intent.putExtra("guid", ((JSONObject) matchAdapter
							.getItem(position)).getString("Guid"));
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		});
		// 搜索
		((RelativeLayout) findViewById(R.id.imageButton2))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								EntityshopActivity.class));
						overridePendingTransition(android.R.anim.fade_in,
								android.R.anim.fade_out);
						if (!HttpConn.isNetwork)
							finish();
					}

				});
		
//		// 发现
//		((RelativeLayout) findViewById(R.id.find_image))
//		.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (HttpConn.isLogin) {
//					startActivity(new Intent(getBaseContext(),
//							FindActivity.class));
//					overridePendingTransition(android.R.anim.fade_in,
//							android.R.anim.fade_out);
//				} else {
//					Intent intent = new Intent(MainActivity.this,UserLogin.class);
//					intent.putExtra("score", "");
//					startActivity(intent);
//				}
//				if (!HttpConn.isNetwork)
//					finish();
//			}
//			
//		});
		// 购物车
		((RelativeLayout) findViewById(R.id.imageButton3))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (HttpConn.isLogin) {
							startActivity(new Intent(getBaseContext(),
									CartActivity.class));
							overridePendingTransition(android.R.anim.fade_in,
									android.R.anim.fade_out);
						} else {
							Intent intent = new Intent(MainActivity.this,
									UserLogin.class);
							intent.putExtra("cart", "");
							startActivity(intent);
						}
						if (!HttpConn.isNetwork)
							finish();
					}

				});
		// 个人中心
		((RelativeLayout) findViewById(R.id.imageButton4))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (HttpConn.isLogin) {
							startActivity(new Intent(getBaseContext(),
									MemberActivity.class));
							overridePendingTransition(android.R.anim.fade_in,
									android.R.anim.fade_out);
						} else {
							Intent intent = new Intent(MainActivity.this,
									UserLogin.class);
							intent.putExtra("person", "");
							startActivity(intent);
						}
						if (!HttpConn.isNetwork)
							finish();
					}

				});
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.tv_erwei:
			startActivityForResult(new Intent(getBaseContext(),
					CaptureActivity.class), 0);
			break;
		case R.id.tv_xiaoxi:
			if (HttpConn.isLogin) {
				startActivity(new Intent(getBaseContext(), MemberMessage.class));
			}
			break;
		case R.id.sousuo:
			startActivity(new Intent(getBaseContext(), SearchMain.class));
			break;
		case R.id.iv_xianshi:
			intent = new Intent(MainActivity.this,
					LimitProductActivity.class);
			intent.putExtra("type", 8);
			startActivity(intent);
			break;
		case R.id.tv_big:
			startActivity(new Intent(MainActivity.this, BrandCenter.class));
			break;
		case R.id.tv_group:
			intent = new Intent(MainActivity.this, ProductList.class);
			intent.putExtra("type", 4);
			startActivity(intent);
			break;
		case R.id.tv_new:
			intent = new Intent(MainActivity.this, ProductList.class);
			intent.putExtra("type", 1);
			startActivity(intent);
			break;
		case R.id.tv_hot:
			intent = new Intent(MainActivity.this, ProductList.class);
			intent.putExtra("type", 2);
			startActivity(intent);
			break;
		}
	}

	/**
	 * 获取数据
	 */
	private void getData() {
		// 获取广告
		HttpUtils hu = new HttpUtils();

		String url =HttpConn.hostName
				+ "/api/ShopGGlist/?shopid="+MyApplication.agentId+"&Type=1&AppSign="
				+ HttpConn.AppSign;

		hu.send(HttpMethod.GET,
				HttpConn.hostName
						+ "/api/ShopGGlist/?shopid="+MyApplication.agentId+"&Type=1&AppSign="
						+ HttpConn.AppSign, new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(MainActivity.this, "连接网络失败", 1000)
								.show();
					}

					@Override
					public void onSuccess(ResponseInfo<String> data) {
						final JSONArray ShopGGlist;
						try {
							ShopGGlist = new JSONObject(data.result.toString())
									.getJSONArray("ImageList");
							for (int i = 0; i < ShopGGlist.length(); i++) {
								String path = ShopGGlist.getJSONObject(i)
										.getString("Value");
								ImageView iv = new ImageView(
										MainActivity.this);
								iv.setScaleType(ScaleType.FIT_XY);
								ImageLoader.getInstance()
										.displayImage(path, iv);
								bannerList.add(iv);
							}
							if (ShopGGlist.length() > 0) {
								ggadapter = new GGPagerAdapter(bannerList,
										ShopGGlist, MainActivity.this);
								view_banner.setAdapter(ggadapter);
							}
							initPoint();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

		// 获取广告1
		String getDefaultAdUrl = HttpConn.hostName
				+ "/api/GetDefaultAd?ShopID="+MyApplication.agentId+"&BanerPostion=0&W_Type=0"
				+ "&AppSign=" + HttpConn.AppSign;
		hu.send(HttpMethod.GET,
				getDefaultAdUrl,
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(MainActivity.this, "连接网络失败", 1000)
								.show();
					}

					@Override
					public void onSuccess(ResponseInfo<String> data) {
						try {
							JSONObject jo = new JSONObject(data.result
									.toString());
							JSONArray ja = jo.getJSONArray("Data");
							final JSONObject banner = (JSONObject) ja.get(0);
							ImageLoader.getInstance().displayImage(
									banner.getString("Value"), banner1);
							banner1.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(
											MainActivity.this, BannerWeb.class);
									try {
										intent.putExtra("Url",
												banner.getString("Url"));
									} catch (JSONException e) {
										e.printStackTrace();
									}
									startActivity(intent);

								}
							});

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
		// 获取广告2
		String getDefaultAdUrl2 =HttpConn.hostName
				+ "/api/GetDefaultAd?ShopID="+MyApplication.agentId+"&BanerPostion=0&W_Type=0"
				+ "&AppSign=" + HttpConn.AppSign;
		hu.send(HttpMethod.GET,
				HttpConn.hostName
						+ "/api/GetDefaultAd?ShopID="+MyApplication.agentId+"&BanerPostion=0&W_Type=0"
						+ "&AppSign=" + HttpConn.AppSign,
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(MainActivity.this, "连接网络失败", 1000)
								.show();
					}

					@Override
					public void onSuccess(ResponseInfo<String> data) {
						try {
							JSONObject jo = new JSONObject(data.result
									.toString());
							JSONArray ja = jo.getJSONArray("Data");
							final JSONObject banner = (JSONObject) ja.get(0);
							ImageLoader.getInstance().displayImage(
									banner.getString("Value"), banner2);
							banner2.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(
											MainActivity.this, BannerWeb.class);
									try {
										intent.putExtra("Url",
												banner.getString("Url"));
									} catch (JSONException e) {
										e.printStackTrace();
									}
									startActivity(intent);
								}
							});
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

		// 获取分类
		String productCatagoryUrl = HttpConn.hostName + "/api/productcatagory/?id="
				+ 0 + "&AppSign=" + HttpConn.AppSign+"&AgentID="+MyApplication.agentId +"&sbool=true";
		hu.send(HttpMethod.GET,productCatagoryUrl ,
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(MainActivity.this, "连接网络失败", 1000)
								.show();
					}

					@Override
					public void onSuccess(ResponseInfo<String> data) {
						try {
							JSONObject jo = new JSONObject(data.result
									.toString());

							JSONArray ja = jo.getJSONArray("Data");
							
							if(ja.length()<8){
								for (int i = 0; i < ja.length(); i++) {
									TypeList.add(ja.getJSONObject(i));
								}
							}else{
								for (int i = 0; i < 8; i++) {
									TypeList.add(ja.getJSONObject(i));
								}
							}
						
							if (typeAdapter == null) {
								typeAdapter = new ImageAdapter(TypeList,
										MainActivity.this);
								type_grid.setAdapter(typeAdapter);
							} else {
								typeAdapter.notifyDataSetChanged();
							}
						} catch (JSONException e) {
							e.printStackTrace();

						}
					}
				});
		// 获取推荐
		 String agentId = MyApplication.agentId;
		 String urlStr = HttpConn.hostName + "/api/product2/type/?type="
					+ 2 + "&sorts=ModifyTime&isASC=false&pageIndex=1&pageCount="
					+ 16 + "&AppSign=" + HttpConn.AppSign + "&MemLoginID="
					+ HttpConn.username +"&AgentId="+agentId+"&Sbool=true";
		 Log.i("MainActivity", "Url--product2 :"+urlStr);
		hu.send(HttpMethod.GET, urlStr, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				Toast.makeText(MainActivity.this, "获取推荐商品失败", 1)
						.show();
			}

			@Override
			public void onSuccess(ResponseInfo<String> data) {
				try {
					JSONObject jo = new JSONObject(data.result.toString());
					JSONArray ja = jo.getJSONArray("Data");
					for (int i = 0; i < ja.length(); i++) {
						product_list.add(ja.getJSONObject(i));
					}
					if (matchAdapter == null) {
						matchAdapter = new MatchAdapter(
								MainActivity.this, product_list, width);
						product_grid.setAdapter(matchAdapter);
					} else {
						matchAdapter.notifyDataSetChanged();
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		// 获取全球购
		String getAnnouncementListUr =  HttpConn.hostName+ "/api/GetAnnouncementList/?AppSign=" + HttpConn.AppSign+"&AgentID="+MyApplication.agentId;
		hu.send(HttpMethod.GET,getAnnouncementListUr,
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
					}

					@Override
					public void onSuccess(ResponseInfo<String> data) {
						try {
							qqgArray = new JSONObject(data.result.toString()).getJSONArray("Data");
							
							for (int i = 0; i < qqgArray.length(); i++) {
								JSONObject obj = qqgArray.getJSONObject(i);
								Announcement announcement = new Announcement();
								announcement.setGuid(obj.getString("Guid"));
								announcement.setTitle(obj.getString("Title"));
								announcement.setRemark(obj.getString("Remark"));
								announcement.setCreateUser(obj.getString("CreateUser"));
								announcement.setCreateTime(obj.getString("CreateTime"));
								announcement.setModifyUser(obj.getString("ModifyUser"));
								announcement.setModifyTime(obj.getString("ModifyTime"));
								announcement.setIsDeleted(obj.getInt("IsDeleted"));
								announcement.setAgentID(obj.getString("AgentID"));
								announcements.add(announcement);
								
								TextView tv = new TextView(MainActivity.this);
								tv.setText(obj.getString("Title"));
								tv.setTextColor(Color.parseColor("#666666"));
								tv.setGravity(Gravity.CENTER_VERTICAL);
								tv.setTextSize(12);
								tv.setSingleLine(true);
								tv.setEllipsize(TruncateAt.MARQUEE);
								tv.setFocusable(true);
								tv.setFocusableInTouchMode(true);
								tv.setTag(i);
								tv.setOnClickListener(new OnTextViewClickListener());
								details.addView(tv);
							}
							details.setAutoStart(true);
							details.startFlipping();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
		
		
		
		// 获取今日活动
		String productListUrl = HttpConn.hostName
				+ "/api/product2/list?pageIndex=1&pageCount=3&sorts=ModifyTime&isASC=false&AppSign="
				+ HttpConn.AppSign + "&ProductCategoryID=0"+"&AgentId="+MyApplication.agentId+"&Sbool=true";
		hu.send(HttpMethod.GET,productListUrl,
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(MainActivity.this, "获取推荐商品失败",
								1000).show();
					}

					@Override
					public void onSuccess(ResponseInfo<String> data) {
						try {
							JSONObject jo = new JSONObject(data.result
									.toString());
							final JSONArray ja = jo.getJSONArray("Data");

							ImageLoader.getInstance().displayImage(
									ja.getJSONObject(0).getString(
											"OriginalImge"), iv_jinri1);
							ImageLoader.getInstance().displayImage(
									ja.getJSONObject(1).getString(
											"OriginalImge"), iv_jinri2);
							ImageLoader.getInstance().displayImage(
									ja.getJSONObject(2).getString(
											"OriginalImge"), iv_jinri3);
							iv_jinri1.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(
											MainActivity.this,
											ProductDetails.class);
									try {
										intent.putExtra(
												"guid",
												ja.getJSONObject(0).getString(
														"Guid"));
									} catch (JSONException e) {

										e.printStackTrace();
									}
									startActivity(intent);

								}
							});
							iv_jinri2.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(
											MainActivity.this,
											ProductDetails.class);
									try {
										intent.putExtra(
												"guid",
												ja.getJSONObject(1).getString(
														"Guid"));
									} catch (JSONException e) {

										e.printStackTrace();
									}
									startActivity(intent);

								}
							});
							iv_jinri3.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Intent intent = new Intent(
											MainActivity.this,
											ProductDetails.class);
									try {
										intent.putExtra(
												"guid",
												ja.getJSONObject(2).getString(
														"Guid"));
									} catch (JSONException e) {
										e.printStackTrace();
									}
									startActivity(intent);
								}
							});
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}
	
	public class OnTextViewClickListener implements View.OnClickListener{

		@Override
		public void onClick(View view) {
			int position= (Integer) view.getTag();
			Announcement ann = announcements.get(position);
			Intent intent = new Intent();
			Bundle  bundle  = new Bundle();
			bundle.putSerializable("announcement", ann);
			intent.putExtras(bundle);
			
			
			intent.setClass(MainActivity.this, AnnouncementActivity.class);
			startActivity(intent);
			
		}
		
	}
	
	/**
	 * 初始化 广告的点
	 */
	private void initPoint() {
		for (int i = 0; i < bannerList.size(); i++) {
			ImageView iv = new ImageView(MainActivity.this);
			if (i == 0) {
				iv.setBackgroundResource(R.drawable.point1);
			} else {
				iv.setBackgroundResource(R.drawable.point);
			}
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			lp.setMargins(10, 0, 10, 0);
			iv.setLayoutParams(lp);
			point_list.add(iv);
			point_group.addView(iv);
		}
		handler.postDelayed(r, 5000);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1) {
			String mresult = data.getExtras().getString("mresult");
			if (mresult.contains("ProductDetail")) {
				String str[] = mresult.split("ProductDetail");
				if (str.length > 1) {
					Intent intent = new Intent(MainActivity.this,
							ProductDetails.class);
					intent.putExtra("guid",
							mresult.split("ProductDetail")[1].substring(1));
					startActivity(intent);
				} else {

				}
			} else if (mresult.length() == 13 && isNumeric(mresult)) {
				// 跳到扫码结果界面
				Intent intent = new Intent(MainActivity.this,
						BarcodeResultList.class);
				intent.putExtra("ProNum", mresult);
				startActivity(intent);
			} else {
				Toast.makeText(MainActivity.this, "没有找到该商品",
						Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher m = pattern.matcher(str);
		return m.matches();
	}
}

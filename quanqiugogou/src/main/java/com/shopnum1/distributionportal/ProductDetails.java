package com.shopnum1.distributionportal;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.adater.BaseImageAdapter;
import com.shopnum1.distributionportal.adater.CommentAdapter;
import com.shopnum1.distributionportal.adater.MyPagerAdapter;
import com.shopnum1.distributionportal.util.CustomDigitalClock;
import com.shopnum1.distributionportal.util.CustomViewPager;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.NoScrollListView;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

@SuppressWarnings("deprecation")
public class ProductDetails extends Activity implements OnClickListener {
	private HttpConn httpget = new HttpConn();
	private JSONObject jsonObject; // 商品信息
	private Dialog pBar; // 加载进度
	private LocationClient mLocationClient = null;
	private List<String> bannerList = new ArrayList<String>();
	private MyAdapter adapter; // 广告图适配器
	private String Name, ShopPrice, MarketPrice, ShopPrice2, OriginalImge; // 商品名称和价格
	private int RepertoryCount, BuyNumber, LimitBuyCount; // 库存数购买数和限制购买数
	private String SpecificationName = "";
	private String guid; // 商品guid
	private SelectPicPopupWindow menuWindow; // 弹出框
	private View menuView; // 弹出框视图
	private EditText editText; // 购买数量
	private JSONArray ProductList, specArray;
	private GridAdapter adapter1, adapter2, adapter3, adapter4;
	private int selectedPosition = -1;// 选中的位置
	private Boolean isCollected = true;
	private Boolean isSaled = true;
	private Boolean toBuy = true;
	private Boolean getValue = false;
	private Boolean overTime = false;
	private String EndTime;
	private DisplayMetrics metric;
	private TextView textview1;
	private TextView textview2;
	private int picWidth;
	private DisplayImageOptions options;// imageLoader配置对象
	private ImageView cursor;// 下标
	private int cursor_width;// 下标 宽度
	private CustomViewPager viewpager;// viewpager视图
	private View v1;// 评论
	private View v2;// 商品详细
	private NoScrollListView comment_list;// 评论listview
	private ArrayList<View> list_view = new ArrayList<View>();
	private MyPagerAdapter pagerAda;
	private WebView webview = null;
	private CommentAdapter commentAdapter;
	private ArrayList<JSONObject> comment_data = new ArrayList<JSONObject>();
	private ArrayList<BaseAdapter> list_adapter = new ArrayList<BaseAdapter>();
	private int width;
	private String pcproductdetailurl="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.product_details);
	
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.banner) // 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.banner) // 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.banner) // 设置图片加载或解码过程中发生错误显示的图片
				.cacheInMemory(true) // default 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true) // default 设置下载的图片是否缓存在SD卡中
				.considerExifParams(false) // default
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) // default
				.bitmapConfig(Bitmap.Config.ARGB_8888) // default 设置图片的解码类型
				.handler(new Handler()) // default
				.build();
		metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		width = metric.widthPixels;
		picWidth = (int) ((metric.widthPixels - 6 * 10 * metric.density) / 3);
		initLayout();
		PanDaniscollect();
		getProduct();
	
	}
	void PanDaniscollect(){
		HttpUtils hu = new HttpUtils();
		hu.configCurrentHttpCacheExpiry(2000); 
		String checkIsCollectedUrl = HttpConn.hostName+ "/api/CheckIsCollected/?MemLoginID="+HttpConn.username+"&AppSign="
		+ HttpConn.AppSign+"&productGuid="+guid +"&AgentID="+MyApplication.agentId;
		hu.send(HttpMethod.GET,
				checkIsCollectedUrl, new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(getApplicationContext(), "连接网络失败", 1000)
								.show();
						isCollected = false;
						((ImageView) findViewById(R.id.collectimg)).setBackgroundResource(R.drawable.collect);
					}
					@Override
					public void onSuccess(ResponseInfo<String> data) {
						final JSONObject ShopGGlist;
						try {
							ShopGGlist = new JSONObject(data.result.toString());
							if(ShopGGlist.optBoolean("Data")){
								isCollected = true;
								((ImageView) findViewById(R.id.collectimg)).setBackgroundResource(R.drawable.collect1);
							//	Toast.makeText(getApplicationContext(), "已收藏过", 2).show();
							}else{
								isCollected = false;
								((ImageView) findViewById(R.id.collectimg)).setBackgroundResource(R.drawable.collect);
								//Toast.makeText(getApplicationContext(), "未收藏", 2).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}
	// 初始化商品详情
	private void ininView2() {
		webview = (WebView) v2.findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return super.shouldOverrideUrlLoading(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
		});
		try {
			if (jsonObject.getString("MobileDetail") == null
					|| ("").equals(jsonObject.getString("MobileDetail"))
					|| jsonObject.getString("MobileDetail").equals("null")) {
				pBar.dismiss();
				webview.setVisibility(View.GONE);
				((TextView) v2.findViewById(R.id.nocontent))
						.setVisibility(View.VISIBLE);
			} else {
				webview.loadDataWithBaseURL("",
						jsonObject.getString("MobileDetail"), "text/html",
						"utf-8", "");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	private void showShare() {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		oks.setTitle(getString(R.string.app_name));
		if(pcproductdetailurl==null||"".equals(pcproductdetailurl)){
		    	//Toast.makeText(ProductDetails.this, "pcproductdetailurl为null", 3).show();
		    	pcproductdetailurl=HttpConn.shareProductDetialURL+"/#page/ProductDetail?"+guid;
		}
		if(OriginalImge==null&"".equals(OriginalImge)){
		 //Toast.makeText(ProductDetails.this, "OriginalImge为null", 3).show();
		 OriginalImge=HttpConn.urlName+"/ImgUpload/20151201115440931.jpg";    	
		    }
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl(pcproductdetailurl);
		// text是分享文本，所有平台都需要这个字段
		oks.setText(Name);	   
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		oks.setImageUrl(OriginalImge);// 确保SDcard下面存在此张图片
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(pcproductdetailurl);
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("全球gogo是一个优秀的应用，里面有各式各样的商品,真的太棒啦");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用

		oks.setSiteUrl(pcproductdetailurl);
		// 启动分享GUI
		oks.show(this);
	}

	// 初始化
	public void initLayout() {
		// 添加购物车
		((ImageView) findViewById(R.id.share))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						showShare();
				
					}
				});
		v1 = View.inflate(this, R.layout.comment_list, null);
		v2 = View.inflate(this, R.layout.product_more, null);
		comment_list = (NoScrollListView) v1.findViewById(R.id.comment_list);
		textview1 = (TextView) this.findViewById(R.id.textview1);
		textview2 = (TextView) this.findViewById(R.id.textview2);
		textview1.setOnClickListener(this);
		textview2.setOnClickListener(this);
		viewpager = (CustomViewPager) this.findViewById(R.id.viewPager);
		cursor = (ImageView) this.findViewById(R.id.cursor);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		cursor_width = metric.widthPixels / 2;
		LayoutParams params = (LayoutParams) cursor.getLayoutParams();
		params.width = cursor_width;
		cursor.setLayoutParams(params);
		guid = getIntent().getStringExtra("guid");
		menuView = LayoutInflater.from(this).inflate(R.layout.product_spec,
				null);
		EndTime = getIntent().getStringExtra("EndTime");
		if (EndTime != null) {
			CustomDigitalClock text = ((CustomDigitalClock) findViewById(R.id.text));
			((LinearLayout) findViewById(R.id.linearLayout))
					.setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.add)).setVisibility(View.GONE);
			((Button) findViewById(R.id.buy))
					.setBackgroundResource(R.drawable.red_buttonbg);
			((TextView) menuView.findViewById(R.id.text4))
					.setVisibility(View.INVISIBLE);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			try {
				Date d2 = sdf.parse(EndTime);
				text.setEndTime(d2.getTime());
				if (d2.getTime() < System.currentTimeMillis()) {
					overTime = true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			((LinearLayout) findViewById(R.id.linearLayout2))
					.setVisibility(View.VISIBLE);
		}
		if (HttpConn.cartNum > 0) {
			((TextView) findViewById(R.id.num)).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.num)).setText(HttpConn.cartNum + "");
		} else {
			((TextView) findViewById(R.id.num)).setVisibility(View.GONE);
		}
		// 返回
		((LinearLayout) findViewById(R.id.back))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
		viewpager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				TranslateAnimation animation = null;
				switch (arg0) {
				case 0://商品详细
					textview1.setTextColor(Color.BLACK);
					textview2.setTextColor(Color.RED);
					animation = new TranslateAnimation(cursor_width, 0, 0, 0);
					break;
				case 1://商品评价
					textview1.setTextColor(Color.RED);
					textview2.setTextColor(Color.BLACK);
					animation = new TranslateAnimation(0, cursor_width, 0, 0);
					break;
				}
				animation.setFillAfter(true);
				animation.setDuration(300);
				cursor.startAnimation(animation);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		getComment_list();
	}

	// 广告适配器
	class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		@Override
		public Object getItem(int position) {
			return bannerList.get(position % bannerList.size());
		}

		@Override
		public long getItemId(int position) {
			return position % bannerList.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.img_zoom, parent, false);
			}
			ImageView imageview = (ImageView) convertView
					.findViewById(R.id.img);
			LayoutParams para = imageview.getLayoutParams();
			para.height = getWindowManager().getDefaultDisplay().getWidth();
			para.width = getWindowManager().getDefaultDisplay().getWidth();
			imageview.setLayoutParams(para);
			String imageurl  = bannerList.get(position % bannerList.size());
			ImageLoader.getInstance().displayImage(imageurl, imageview,options);
			return convertView;
		}
	}

	public void getProduct() {
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		new Thread() {
			public void run() {
				
				String productUrl = "/api/product/?id="
						+ guid + "&AppSign=" + HttpConn.AppSign
						+ "&MemLoginID=" + HttpConn.username + "&AgentID="+MyApplication.agentId + "&Sbool=true";
				StringBuffer result = httpget.getArray(productUrl);
				Message msg = Message.obtain();
				try {
					jsonObject = new JSONObject(result.toString()).getJSONObject("ProductInfo");
					pcproductdetailurl=jsonObject.optString("PCUrl");
					if (HttpConn.showImage) {
						OriginalImge = jsonObject.getString("OriginalImge");
						String Image = jsonObject.getString("Images");
						ArrayList<String> Images = new ArrayList<String>();
						String str = Image.substring(1, Image.length() - 1).trim();
						if (!str.equals("")) {
							String[] ImageString = Image.substring(1, Image.length() - 1).trim()
									.split(",");
							for (int i = 0; i < ImageString.length; i++) {
								Images.add(ImageString[i]);
							}
						}
						for (int i = 0; i <= Images.size(); i++) {
							if (i == 0)
								bannerList.add(OriginalImge.replace("180x180","300x300"));
							else
								bannerList.add(Images.get(i - 1).replace("\"", "").replace("\\", ""));
						}
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

	// 显示图片
	public void addGallery() {
		Gallery gallery = (Gallery) findViewById(R.id.gallery);
		adapter = new MyAdapter();
		gallery.setAdapter(adapter);
	}

	public void getSpecificationList() {
		new Thread() {
			public void run() {
				StringBuffer result = httpget
						.getArray("/api/SpecificationList/?id=" + guid
								+ "&AppSign=" + HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					specArray = new JSONObject(result.toString()).getJSONArray("SpecificationProudct");
					msg.what = 3;
				} catch (JSONException e) {
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
				Toast.makeText(getBaseContext(), "该商品已下架", Toast.LENGTH_SHORT)
						.show();
				break;
			case 1:
				pBar.dismiss();
				initData();
				addWindow();
				addGallery();
				ininView2();
				
				list_view.add(v2);//商品详细
				list_view.add(v1);//商品评价
				pagerAda = new MyPagerAdapter(list_view);
				viewpager.setAdapter(pagerAda);
				break;
			case 2:
				((TextView) findViewById(R.id.city)).setHint(msg.obj.toString());
				((TextView) findViewById(R.id.goods)).setText("");
				break;
			case 3:
				if (specArray.length() > 0 && EndTime == null) {
					((ScrollView) menuView.findViewById(R.id.specLayout))
							.setVisibility(View.VISIBLE);
					getSpec();
				} else {
					getSpecCount();
				}
				break;
			case 4:
				((TextView) menuView.findViewById(R.id.text2)).setText("￥"
						+ ShopPrice2);
				((TextView) menuView.findViewById(R.id.text4)).setText("库存："
						+ RepertoryCount);
				getValue = true;
				break;
			case 5:
				pBar.dismiss();
				menuWindow.dismiss();
				if (msg.obj.toString().equals("202")) {
					((TextView) findViewById(R.id.num))
							.setText(HttpConn.cartNum + "");
					if (toBuy) { // 立即购买
						if (isSaled) {
							Intent intent = new Intent(getBaseContext(),
									OrderPost.class);
							Double BuyPrice = Double.parseDouble(ShopPrice2);
							intent.putExtra("ProductList",
									ProductList.toString());
							intent.putExtra("BuyNumber", BuyNumber);
							intent.putExtra("ProductPrice","￥"+ new DecimalFormat("0.00")
													.format(BuyPrice* BuyNumber));
							startActivity(intent);
						} else {
							Toast.makeText(getBaseContext(), "区域无货",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						if (HttpConn.cartNum > 0) {
							((TextView) findViewById(R.id.num))
									.setVisibility(View.VISIBLE);
						} else {
							((TextView) findViewById(R.id.num))
									.setText(HttpConn.cartNum + "");
							Toast.makeText(getBaseContext(), "添加成功",
									Toast.LENGTH_SHORT).show();
						}
					}
				}
				break;
			case 6:
				if (msg.obj.toString().equals("202")){
					isCollected = true;
					((ImageView) findViewById(R.id.collectimg))
					.setBackgroundResource(R.drawable.collect1);
					
					Toast.makeText(getBaseContext(), "收藏成功", Toast.LENGTH_SHORT)
							.show();}
				else{
					isCollected = false;
					((ImageView) findViewById(R.id.collectimg))
					.setBackgroundResource(R.drawable.collect);
					Toast.makeText(getBaseContext(), "收藏失败",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case 7:
				adapter.notifyDataSetChanged();
				break;
			case 8:
				if (msg.obj.toString().equals("202")) {
					isSaled = true;
					((TextView) findViewById(R.id.goods)).setText("有货");
				} else {
					isSaled = false;
					((TextView) findViewById(R.id.goods)).setText("无货");
				}
				break;
			case 9:
				if (HttpConn.cartNum > 0) {
					((TextView) findViewById(R.id.num)).setVisibility(View.VISIBLE);
					((TextView) findViewById(R.id.num)).setText(HttpConn.cartNum + "");
				} else {
					((TextView) findViewById(R.id.num)).setVisibility(View.GONE);
				}
				if (pBar != null && pBar.isShowing()) {
					pBar.dismiss();
				}
				break;
			case 10:
				
				if (msg.obj.toString().equals("202")){
					isCollected = false;
					((ImageView) findViewById(R.id.collectimg))
					.setBackgroundResource(R.drawable.collect);
					Toast.makeText(getBaseContext(), "取消收藏成功", Toast.LENGTH_SHORT)
							.show();}
				else{
					isCollected = true;
					((ImageView) findViewById(R.id.collectimg))
					.setBackgroundResource(R.drawable.collect1);
					Toast.makeText(getBaseContext(), "取消失败",
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
			
			super.handleMessage(msg);
		}
	};

	public void initData() {
		try {
			Name = jsonObject.getString("Name");
			if (EndTime != null)
				ShopPrice = new DecimalFormat("0.00").format(getIntent()
						.getDoubleExtra("ShopPrice", 0.00));
			else
				ShopPrice = new DecimalFormat("0.00").format(jsonObject
						.getDouble("ShopPrice"));
			ShopPrice2 = ShopPrice;
			MarketPrice = new DecimalFormat("0.00").format(jsonObject
					.getDouble("MarketPrice"));
			RepertoryCount = jsonObject.getInt("RepertoryCount");
			LimitBuyCount = jsonObject.getInt("LimitBuyCount");
			if (LimitBuyCount == 0) {
				// TODO 限购 9999件
				LimitBuyCount = HttpConn.LIMIT_BUY_COUNT;
			}

			((TextView) findViewById(R.id.product_name)).setText(Name);
			((TextView) findViewById(R.id.ShopPrice)).setText("￥" + ShopPrice);
			((TextView) findViewById(R.id.MarketPrice)).setHint("￥"
					+ MarketPrice);
			((TextView) findViewById(R.id.text1)).setHint("最近售出："
					+ jsonObject.getString("SaleNumber"));

			String CollectCount = jsonObject.getString("CollectCount");
		

			if (jsonObject.getInt("PresentScore") == Integer.parseInt(ShopPrice
					.replace(".", "")))
				((TextView) findViewById(R.id.text2)).setHint("赠送消费积分："
						+ jsonObject.getInt("ShopPrice"));
			else
				((TextView) findViewById(R.id.text2)).setHint("赠送消费积分："
						+ jsonObject.getInt("PresentScore"));

			getMore();

			new Thread() {
				public void run() {
					JSONObject object = new JSONObject();
					try {
						object.put("ProductGuid", guid);
						object.put("ProductName", Name);
						object.put("ProductOriginalImge", OriginalImge);
						object.put("ProductMarketPrice", MarketPrice);
						object.put("ProductShopPrice", ShopPrice);
						object.put("MemLoginID", HttpConn.UserName);
						object.put("AppSign", HttpConn.AppSign);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if (HttpConn.isLogin) {
						httpget.postJSON("/api/footprintappend/",object.toString());
					}
				}
			}.start();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 商品详细
	public void getMore() {
		// 商品收藏
		((LinearLayout) findViewById(R.id.collect))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
				
						if (!isCollected) {//如果没有收藏
							if (HttpConn.isLogin) {
							
								new Thread() {
									public void run() {
										StringBuffer result = httpget
												.getArray("/api/collectadd/?productGuid="
														+ guid
														+ "&MemLoginID="
														+ HttpConn.username
														+ "&AppSign="
														+ HttpConn.AppSign);
										Message msg = Message.obtain();
										msg.what = 6;
										try {
											msg.obj = new JSONObject(result
													.toString())
													.getString("return");
										} catch (JSONException e) {
											e.printStackTrace();
										}
										handler.sendMessage(msg);

									}
								}.start();
							} else {
								startActivity(new Intent(getBaseContext(),
										UserLogin.class));
							}
						}else{
							
							
							if (HttpConn.isLogin) {
							
								new Thread() {
									public void run() {
										StringBuffer result = httpget
												.getArray("/api/collectdeleteByProduct/?productGuid="
														+ guid
														+ "&MemLoginID="
														+ HttpConn.username
														+ "&AppSign="
														+ HttpConn.AppSign);
										Message msg = Message.obtain();
										msg.what = 10;
										try {
											msg.obj = new JSONObject(result
													.toString())
													.getString("return");
										} catch (JSONException e) {
											e.printStackTrace();
										}
										handler.sendMessage(msg);

									}
								}.start();
							} else {
								startActivity(new Intent(getBaseContext(),
										UserLogin.class));
							}
							
							
							
						}
					}
				});
		
		// 添加购物车
		((Button) findViewById(R.id.add))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						menuWindow = new SelectPicPopupWindow(
								ProductDetails.this, null);
						menuWindow.showAtLocation(
								findViewById(R.id.product_details),
								Gravity.BOTTOM, 0, 0);
						toBuy = false;
					}
				});
		// 立即购买
		((Button) findViewById(R.id.buy))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						menuWindow = new SelectPicPopupWindow(
								ProductDetails.this, null);
						menuWindow.showAtLocation(
								findViewById(R.id.product_details),
								Gravity.BOTTOM, 0, 0);
						toBuy = true;
					}
				});
		// 查看购物车
		((TextView) findViewById(R.id.cart))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (HttpConn.isLogin) {
							Intent intent = new Intent(getBaseContext(),
									CartActivity.class);
							intent.putExtra("RepertoryCount", RepertoryCount);
							startActivity(intent);
						} else {
							startActivity(new Intent(getBaseContext(),
									UserLogin.class));
						}
					}
				});

		((TextView) findViewById(R.id.num))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (HttpConn.isLogin)
							startActivity(new Intent(getBaseContext(),
									CartActivity.class));
						else
							startActivity(new Intent(getBaseContext(),
									UserLogin.class));
					}
				});
	}

	// 加载弹窗
	public void addWindow() {
		((TextView) menuView.findViewById(R.id.text1)).setText(Name);
		((TextView) menuView.findViewById(R.id.text2)).setText("￥" + ShopPrice);
		if (RepertoryCount > 0)
			((TextView) menuView.findViewById(R.id.text4)).setHint("库存："
					+ RepertoryCount);
		else
			((TextView) menuView.findViewById(R.id.text4)).setHint("库存：0");

		ImageView imageview = (ImageView) menuView
				.findViewById(R.id.imageView1);
		if (HttpConn.showImage)
			ImageLoader.getInstance().displayImage(OriginalImge, imageview,
					MyApplication.options);

		// 减少数量
		final TextView BuyNum = (TextView) menuView.findViewById(R.id.text6);
		BuyNum.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final Dialog dialog = new Dialog(ProductDetails.this,
						R.style.MyDialog);
				final View view = LayoutInflater.from(getBaseContext())
						.inflate(R.layout.dialog4, null);
				dialog.setContentView(view);
				dialog.show();

				editText = (EditText) view.findViewById(R.id.edit);
				String num = BuyNum.getText().toString();
				editText.setText(num);
				editText.setSelection(num.length());
				editText.addTextChangedListener(mEtWatcher);

				((Button) view.findViewById(R.id.no))
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								dialog.dismiss();
							}
						});

				((Button) view.findViewById(R.id.yes))
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								if (!editText.getText().toString().equals("")
										&& !editText.getText().toString()
												.equals("0")) {
									int i = Integer.parseInt(editText.getText()
											.toString());
									BuyNum.setText(i + "");
									dialog.dismiss();
								} else {
									Toast.makeText(getApplicationContext(),
											"商品购买数量不得小于1", Toast.LENGTH_SHORT)
											.show();
								}
							}
						});
			}
		});

		((Button) menuView.findViewById(R.id.text5)).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int i = Integer.parseInt(BuyNum.getText().toString());
						if (i > 1) {
							BuyNum.setText((i - 1) + "");
						} else {
							Toast.makeText(getApplicationContext(), "最少购买1件商品",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
		// 增加数量
		((Button) menuView.findViewById(R.id.text7))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						int i = Integer.parseInt(BuyNum.getText().toString());
						if (i < HttpConn.LIMIT_BUY_COUNT) {
							BuyNum.setText((i + 1) + "");
						} else {
							Toast.makeText(getApplicationContext(),
									"最多只能购买" + HttpConn.LIMIT_BUY_COUNT + "件",
									Toast.LENGTH_SHORT).show();
						}
					}
				});

		// 关闭
		((Button) menuView.findViewById(R.id.cancel))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						menuWindow.dismiss();
					}
				});
		// 确定
		((Button) menuView.findViewById(R.id.okbtn))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (specArray == null || specArray.length() == 0) {
							getValue = true;
							addCart();
						} else {
							int count = specArray.length();
							String value1 = ((TextView) menuView
									.findViewById(R.id.value1)).getText()
									.toString();
							String value2 = ((TextView) menuView
									.findViewById(R.id.value2)).getText()
									.toString();
							String value3 = ((TextView) menuView
									.findViewById(R.id.value3)).getText()
									.toString();
							String value4 = ((TextView) menuView
									.findViewById(R.id.value4)).getText()
									.toString();

							if (EndTime != null) {
								count = 0;
							}

							if (count > 0) {
								if (count == 1) {
									if (value1.length() == 0) {
										Toast.makeText(getBaseContext(),
												"请选择商品规格", Toast.LENGTH_SHORT)
												.show();
									} else {
										SpecificationName = value1;
										addCart();
									}
								} else if (count == 2) {
									if (value1.length() == 0
											|| value2.length() == 0) {
										Toast.makeText(getBaseContext(),
												"请选择商品规格", Toast.LENGTH_SHORT)
												.show();
									} else {
										SpecificationName = value1 + ";"
												+ value2;
										addCart();
									}
								} else if (count == 3) {
									if (value1.length() == 0
											|| value2.length() == 0
											|| value3.length() == 0) {
										Toast.makeText(getBaseContext(),
												"请选择商品规格", Toast.LENGTH_SHORT)
												.show();
									} else {
										SpecificationName = value1 + ";"
												+ value2 + ";" + value3;
										addCart();
									}
								} else if (count == 4) {
									if (value1.length() == 0
											|| value2.length() == 0
											|| value3.length() == 0
											|| value4.length() == 0) {
										Toast.makeText(getBaseContext(),
												"请选择商品规格", Toast.LENGTH_SHORT)
												.show();
									} else {
										SpecificationName = value1 + ";"
												+ value2 + ";" + value3 + ";"
												+ value4;
										addCart();
									}
								}
							} else {
								getValue = true;
								addCart();
							}
						}
					}
				});
	}

	TextWatcher mEtWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			if (s.toString().trim().startsWith("0")
					&& s.toString().trim().length() == 1) {
				editText.setText("1");
				editText.setSelection(1);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	public class SelectPicPopupWindow extends PopupWindow {

		public SelectPicPopupWindow(Activity context,
				OnClickListener itemsOnClick) {
			super(context);
			this.setContentView(menuView);
			this.setWidth(LayoutParams.MATCH_PARENT);
			this.setHeight(LayoutParams.WRAP_CONTENT);
			this.setFocusable(true);
			this.setBackgroundDrawable(new ColorDrawable(0xb0000000));
		}

	}

	public void addCart() {
		BuyNumber = Integer.parseInt(((TextView) menuView
				.findViewById(R.id.text6)).getText().toString());
		if (EndTime != null) {
			int RestrictCount = getIntent().getIntExtra("RestrictCount", 0);
			if (BuyNumber > RestrictCount) {
				Toast.makeText(this, "限购" + RestrictCount + "件",
						Toast.LENGTH_SHORT).show();
			} else if (overTime) {
				Toast.makeText(this, "超过截至时间", Toast.LENGTH_SHORT).show();
			} else if (getValue) {
				postData();
			}
		} else {
			if (BuyNumber > LimitBuyCount) {
				Toast.makeText(this, "限购" + LimitBuyCount + "件",
						Toast.LENGTH_SHORT).show();
			} else if (BuyNumber > RepertoryCount) {
				Toast.makeText(this, "库存不足", Toast.LENGTH_SHORT).show();
			} else if (getValue) {
				postData();
			}
		}
	}

	// 添加到购物车
	public void postData() {
		if (HttpConn.isLogin) {
			pBar.show();
			new Thread() {
				public void run() {
					Message msg = Message.obtain();
					try {
						int LimitCount;
						if (RepertoryCount <= LimitBuyCount)
							LimitCount = RepertoryCount;
						else
							LimitCount = LimitBuyCount;
						JSONObject object2 = new JSONObject();
						object2.put("MemLoginID", HttpConn.UserName);
						object2.put("ProductGuid", guid);
						object2.put("BuyNumber", BuyNumber);
						object2.put("BuyPrice", ShopPrice2);
						object2.put("Attributes", SpecificationName);
						object2.put("DetailedSpecifications", getValue());
						object2.put("AppSign", HttpConn.AppSign);
						object2.put("AgentID", MyApplication.agentId);
						object2.put("ExtensionAttriutes", LimitCount);
						StringBuffer result = httpget.postJSON(
								"/api/shoppingcartadd/", object2.toString());
						String shoppingcartgetUrl = "/api/shoppingcartget/?loginId="
								+ HttpConn.username + "&AppSign="
								+ HttpConn.AppSign + "&agentID="+MyApplication.agentId;
						StringBuffer result2 = httpget
								.getArray(shoppingcartgetUrl);
						HttpConn.cartNum = new JSONObject(result2.toString())
								.getJSONArray("Data").length();
						if (toBuy) {
							JSONArray cartList = new JSONObject(
									result2.toString()).getJSONArray("Data");
							ProductList = new JSONArray();
							for (int i = 0; i < cartList.length(); i++) {
								JSONObject object = cartList.getJSONObject(i);
								if (guid.equals(object.getString("ProductGuid"))) {
									httpget.getArray("/api/shoppingcartdelete/?Guid="
											+ object.getString("Guid")
											+ "&MemLoginID="
											+ HttpConn.username
											+ "&AppSign="
											+ HttpConn.AppSign);
									object.put("BuyNumber", BuyNumber);
									ProductList.put(object);
									break;
								}
							}
						}
						msg.obj = new JSONObject(result.toString())
								.getString("return");
						msg.what = 5;
					} catch (JSONException e) {
						msg.what = 0;
						e.printStackTrace();
					}
					handler.sendMessage(msg);
				}
			}.start();
		} else {
			startActivity(new Intent(this, UserLogin.class));
		}
	}

	public void getCity() {
		mLocationClient = new LocationClient(this);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
		option.setScanSpan(3000);// 设置发起定位请求的间隔时间为3000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocationClient.setLocOption(option);
		mLocationClient.start();
		mLocationClient.requestLocation();
		mLocationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceiveLocation(BDLocation location) {
				if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
					String cityName = location.getProvince()
							+ location.getCity() + location.getDistrict();
					HttpConn.cityName = cityName;
					Message msg = Message.obtain();
					msg.obj = cityName;
					msg.what = 2;
					handler.sendMessage(msg);
				}
			}

			@Override
			public void onReceivePoi(BDLocation arg0) {

			}
		});
	}

	@Override
	public void onPause() {
		if (mLocationClient != null && mLocationClient.isStarted()) {
			mLocationClient.stop();
			mLocationClient = null;
		}
		super.onDestroy();
	}

	// 获取规格
	public void getSpec() {
		try {
			switch (specArray.length()) {
			case 1:
				((TextView) menuView.findViewById(R.id.name1))
						.setVisibility(View.VISIBLE);
				((GridView) menuView.findViewById(R.id.grid1))
						.setVisibility(View.VISIBLE);
				((TextView) menuView.findViewById(R.id.name1))
						.setText(specArray.getJSONObject(0).getString(
								"SpecValueName"));
				adapter1 = new GridAdapter(specArray.getJSONObject(0).getJSONArray("Specification"), 0);
				((GridView) menuView.findViewById(R.id.grid1))
						.setAdapter(adapter1);
				break;
			case 2:
				((TextView) menuView.findViewById(R.id.name1))
						.setVisibility(View.VISIBLE);
				((TextView) menuView.findViewById(R.id.name2))
						.setVisibility(View.VISIBLE);
				((GridView) menuView.findViewById(R.id.grid1))
						.setVisibility(View.VISIBLE);
				((GridView) menuView.findViewById(R.id.grid2))
						.setVisibility(View.VISIBLE);
				((TextView) menuView.findViewById(R.id.name1))
						.setText(specArray.getJSONObject(0).getString(
								"SpecValueName"));
				((TextView) menuView.findViewById(R.id.name2))
						.setText(specArray.getJSONObject(1).getString(
								"SpecValueName"));
				adapter1 = new GridAdapter(specArray.getJSONObject(0)
						.getJSONArray("Specification"), 0);
				adapter2 = new GridAdapter(specArray.getJSONObject(1)
						.getJSONArray("Specification"), 1);
				((GridView) menuView.findViewById(R.id.grid1))
						.setAdapter(adapter1);
				((GridView) menuView.findViewById(R.id.grid2))
						.setAdapter(adapter2);
				break;
			case 3:
				((TextView) menuView.findViewById(R.id.name1))
						.setVisibility(View.VISIBLE);
				((TextView) menuView.findViewById(R.id.name2))
						.setVisibility(View.VISIBLE);
				((TextView) menuView.findViewById(R.id.name3))
						.setVisibility(View.VISIBLE);
				((GridView) menuView.findViewById(R.id.grid1))
						.setVisibility(View.VISIBLE);
				((GridView) menuView.findViewById(R.id.grid2))
						.setVisibility(View.VISIBLE);
				((GridView) menuView.findViewById(R.id.grid3))
						.setVisibility(View.VISIBLE);
				((TextView) menuView.findViewById(R.id.name1))
						.setText(specArray.getJSONObject(0).getString(
								"SpecValueName"));
				((TextView) menuView.findViewById(R.id.name2))
						.setText(specArray.getJSONObject(1).getString(
								"SpecValueName"));
				((TextView) menuView.findViewById(R.id.name3))
						.setText(specArray.getJSONObject(2).getString(
								"SpecValueName"));
				adapter1 = new GridAdapter(specArray.getJSONObject(0)
						.getJSONArray("Specification"), 0);
				adapter2 = new GridAdapter(specArray.getJSONObject(1)
						.getJSONArray("Specification"), 1);
				adapter3 = new GridAdapter(specArray.getJSONObject(2)
						.getJSONArray("Specification"), 2);
				((GridView) menuView.findViewById(R.id.grid1))
						.setAdapter(adapter1);
				((GridView) menuView.findViewById(R.id.grid2))
						.setAdapter(adapter2);
				((GridView) menuView.findViewById(R.id.grid3))
						.setAdapter(adapter3);
				break;
			case 4:
				((TextView) menuView.findViewById(R.id.name1))
						.setVisibility(View.VISIBLE);
				((TextView) menuView.findViewById(R.id.name2))
						.setVisibility(View.VISIBLE);
				((TextView) menuView.findViewById(R.id.name3))
						.setVisibility(View.VISIBLE);
				((TextView) menuView.findViewById(R.id.name4))
						.setVisibility(View.VISIBLE);
				((GridView) menuView.findViewById(R.id.grid1))
						.setVisibility(View.VISIBLE);
				((GridView) menuView.findViewById(R.id.grid2))
						.setVisibility(View.VISIBLE);
				((GridView) menuView.findViewById(R.id.grid3))
						.setVisibility(View.VISIBLE);
				((GridView) menuView.findViewById(R.id.grid4))
						.setVisibility(View.VISIBLE);
				((TextView) menuView.findViewById(R.id.name1))
						.setText(specArray.getJSONObject(0).getString(
								"SpecValueName"));
				((TextView) menuView.findViewById(R.id.name2))
						.setText(specArray.getJSONObject(1).getString(
								"SpecValueName"));
				((TextView) menuView.findViewById(R.id.name3))
						.setText(specArray.getJSONObject(2).getString(
								"SpecValueName"));
				((TextView) menuView.findViewById(R.id.name4))
						.setText(specArray.getJSONObject(3).getString(
								"SpecValueName"));
				adapter1 = new GridAdapter(specArray.getJSONObject(0)
						.getJSONArray("Specification"), 0);
				adapter2 = new GridAdapter(specArray.getJSONObject(1)
						.getJSONArray("Specification"), 1);
				adapter3 = new GridAdapter(specArray.getJSONObject(2)
						.getJSONArray("Specification"), 2);
				adapter4 = new GridAdapter(specArray.getJSONObject(3)
						.getJSONArray("Specification"), 3);
				((GridView) menuView.findViewById(R.id.grid1))
						.setAdapter(adapter1);
				((GridView) menuView.findViewById(R.id.grid2))
						.setAdapter(adapter2);
				((GridView) menuView.findViewById(R.id.grid3))
						.setAdapter(adapter3);
				((GridView) menuView.findViewById(R.id.grid4))
						.setAdapter(adapter4);
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	class GridAdapter extends BaseAdapter {

		JSONArray specArray;
		int position;

		public GridAdapter(JSONArray specArray, int position) {
			this.specArray = specArray;
			this.position = position;
		}

		@Override
		public int getCount() {

			return specArray.length();
		}

		@Override
		public Object getItem(int position) {

			return position;
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public View getView(final int arg0, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.spec_grid, parent, false);
			try {
				final String SpecName = specArray.getJSONObject(arg0).getString("SpecName");
				final String SpecValueName = specArray.getJSONObject(arg0).getString("SpecValueName");
				final String Specid = specArray.getJSONObject(arg0).getString("Specid");
				final String SpecValueid = specArray.getJSONObject(arg0).getString("SpecValueid");
				TextView name = (TextView) convertView.findViewById(R.id.name);
				name.setText(SpecValueName);

				if (selectedPosition == arg0) {
					name.setBackgroundResource(R.drawable.attrselect);
					name.setTextColor(Color.parseColor("#ffffff"));
				} else {
					name.setBackgroundResource(R.drawable.attrunselect);
					name.setTextColor(Color.parseColor("#666666"));
				}
				name.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						switch (position) {
						case 0:
							selectedPosition = arg0;
							adapter1.notifyDataSetChanged();
							((TextView) menuView.findViewById(R.id.value1)).setText(SpecName + ":" + SpecValueName);
							((TextView) menuView.findViewById(R.id.value11)).setText(Specid + ":" + SpecValueid);
							break;
						case 1:
							selectedPosition = arg0;
							adapter2.notifyDataSetChanged();
							((TextView) menuView.findViewById(R.id.value2))
									.setText(SpecName + ":" + SpecValueName);
							((TextView) menuView.findViewById(R.id.value22))
									.setText(Specid + ":" + SpecValueid);
							break;
						case 2:
							selectedPosition = arg0;
							adapter3.notifyDataSetChanged();
							((TextView) menuView.findViewById(R.id.value3)).setText(SpecName + ":" + SpecValueName);
							((TextView) menuView.findViewById(R.id.value33))
									.setText(Specid + ":" + SpecValueid);
							break;
						case 3:
							selectedPosition = arg0;
							adapter4.notifyDataSetChanged();
							((TextView) menuView.findViewById(R.id.value4)).setText(SpecName + ":" + SpecValueName);
							((TextView) menuView.findViewById(R.id.value44)).setText(Specid + ":" + SpecValueid);
							break;
						default:
							break;
						}
						;
						getSpecCount();
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return convertView;
		}

	}

	// 获取规格库存
	public void getSpecCount() {
		if (!getValue().equals("")) {
			new Thread() {
				public void run() {
					String specificationUrl = "/api/Specification/?productGuid=" + guid
							+ "&Detail=" + getValue() + "&AppSign="
							+ HttpConn.AppSign + "&MemLoginID="
							+ HttpConn.username + "&agentID="+MyApplication.agentId +"&sbool=true";
					StringBuffer result = httpget.getArray(specificationUrl);
					try {
						JSONObject Specification = new JSONObject(result.toString())
							.getJSONArray("Specification").getJSONObject(0);
						ShopPrice2 = new DecimalFormat("0.00").format(Specification.getDouble("Price"));
						RepertoryCount = Specification.getInt("RepertoryCount");
						Message msg = Message.obtain();
						msg.what = 4;
						handler.sendMessage(msg);
					} catch (JSONException e) {
						getValue = false;
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	// 规格值
	public String getValue() {
		if (specArray == null) {
			return "";
		}
		int count = specArray.length();
		String value1 = ((TextView) menuView.findViewById(R.id.value1))
				.getText().toString();
		String value2 = ((TextView) menuView.findViewById(R.id.value2))
				.getText().toString();
		String value3 = ((TextView) menuView.findViewById(R.id.value3))
				.getText().toString();
		String value4 = ((TextView) menuView.findViewById(R.id.value4))
				.getText().toString();
		String value11 = ((TextView) menuView.findViewById(R.id.value11))
				.getText().toString();
		String value22 = ((TextView) menuView.findViewById(R.id.value22))
				.getText().toString();
		String value33 = ((TextView) menuView.findViewById(R.id.value33))
				.getText().toString();
		String value44 = ((TextView) menuView.findViewById(R.id.value44))
				.getText().toString();

		if (count > 0) {
			if (count == 1) {
				if (value1.length() > 0) {
					return value11;
				}
			} else if (count == 2) {
				if (value1.length() > 0 && value2.length() > 0) {
					return value11 + ";" + value22;
				}
			} else if (count == 3) {
				if (value1.length() > 0 && value2.length() > 0
						&& value3.length() > 0) {
					return value11 + ";" + value22 + ";" + value33;
				}
			} else if (count == 4) {
				if (value1.length() > 0 && value2.length() > 0
						&& value3.length() > 0 && value4.length() > 0) {
					return value11 + ";" + value22 + ";" + value33 + ";"
							+ value44;
				}
			}
		}
		return "";
	}

	@Override
	protected void onResume() {
		// 重新获取购物车数量
		new Thread() {
			public void run() {
				String shoppingcartgetUrl = "/api/shoppingcartget/?loginId="
						+ HttpConn.username + "&AppSign="
						+ HttpConn.AppSign + "&agentID="+MyApplication.agentId;
				StringBuffer result = httpget.getArray(shoppingcartgetUrl);
				try {
					HttpConn.cartNum = new JSONObject(result.toString())
							.getJSONArray("Data").length();
				} catch (JSONException e) {
					HttpConn.cartNum = 0;
					e.printStackTrace();
				}
				Message msg = Message.obtain();
				msg.what = 9;
				handler.sendMessage(msg);
				getSpecificationList();
			}
		}.start();
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		if (resultCode == 1) {
			((TextView) findViewById(R.id.city)).setHint(HttpConn.cityName);
			new Thread() {
				public void run() {
					String code = data.getStringExtra("code");
					StringBuffer result = httpget
							.getArray("/api/productstockbyarea/?ProducGuid="
									+ guid + "&province="
									+ code.substring(0, 3) + "&city="
									+ code.substring(3, 6) + "&region="
									+ code.substring(6, 9) + "&AppSign="
									+ HttpConn.AppSign);
					try {
						Message msg = Message.obtain();
						msg.obj = new JSONObject(result.toString())
								.getString("return");
						msg.what = 8;
						handler.sendMessage(msg);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.textview1:
			viewpager.setCurrentItem(1);
			break;
		case R.id.textview2:
			viewpager.setCurrentItem(0);
		
			break;
		}
	}

	public void getComment_list() {
		HttpUtils hu = new HttpUtils();
		hu.send(HttpMethod.GET, HttpConn.hostName
				+ "/api/getproductassess/?productID=" + guid + "&AppSign="
				+ HttpConn.AppSign + "&startPage=1&pageSize=50",
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(getApplicationContext(), "获取评论失败", 1000)
								.show();
					}

					@Override
					public void onSuccess(ResponseInfo<String> data) {
						try {
							JSONObject result = new JSONObject(data.result
									.toString());
							JSONArray ja = result.getJSONArray("Data");
							for (int i = 0; i < ja.length(); i++) {
								JSONObject item = ja.getJSONObject(i);
								comment_data.add(item);
								String[] pathImage = {};
								if (!item.getString("baskiamge").equals("")) {
									pathImage = item.getString("baskiamge")
											.split("\\|");
								}
								BaseImageAdapter adapter = new BaseImageAdapter(pathImage, ProductDetails.this,width);
								list_adapter.add(adapter);
							}
							commentAdapter = new CommentAdapter(comment_data,
									getApplicationContext(), list_adapter);
							comment_list.setAdapter(commentAdapter);
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
			}

	}
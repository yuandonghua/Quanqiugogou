package com.shopnum1.distributionportal.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.shopnum1.distributionportal.CartActivity;
import com.shopnum1.distributionportal.MainActivity;
import com.shopnum1.distributionportal.MemberActivity;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.SetupActivity;

public class HttpConn {
	public static Boolean isLogin = false; // 是否登录成功
	public static String username = ""; // 用户名
	public static String UserName = ""; // 用户名
	public static Boolean showImage = true; // 是否显示图片
	public static Boolean isNetwork = true; // 网络是否正常
	public static Boolean getScore = false; // 是否领取积分
	public static int cartNum = 0;
	public static int Width = 0; // 快捷方式高度
	public static int Height = 0; // 快捷方式宽度
	public static String cityName = ""; // 定位所在城市名称
	public static int LIMIT_BUY_COUNT = 9999; // 最大限购数量
	
	//public final static String urlName = "http://fxmhv811.groupfly.cn"; // 门户pc端
	public final static String urlName = "http://appapi.6868xp.com"; // 门户pc端
	//public static String hostName = "http://fxmhv811app.groupfly.cn"; // 门户接口地址
	public static String hostName = "http://appapi.6868xp.com"; // 门户接口地址
	
	public static String shareURL = "http://www.6868xp.com"; // 分享地址-注册
	
	public static String shareProductDetialURL = "http://fwxxy.6868xp.com"; // 分享地址-注册
//	
//	public final static String urlName = "http://fxv811.groupfly.cn"; // 主站pc端
//	public static String hostName = "http://fxv811app.groupfly.cn"; // 主站接口地址
//	

	
	public static String subHostName = "http://fxmhv811appfxs.groupfly.cn/";
	public static String AppSign = "33bd618c1ca2149f14dcf2065d4d9466";

	// 设置网络
	public void setNetwork(final Context context) {
		if (!isNetwork) {
			final Dialog dialog = new Dialog(context, R.style.MyDialog);
			View view = LayoutInflater.from(context).inflate(R.layout.dialog5,
					null);
			((TextView) view.findViewById(R.id.dialog_text)).setText("网络连接失败");
			dialog.setContentView(view);
			dialog.show();

			((Button) view.findViewById(R.id.yes)).setText("重试");
			((Button) view.findViewById(R.id.yes))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							new Thread() {
								public void run() {
									StringBuffer result = getArray("/api/SignSet/?isSet=1");
									try {
										AppSign = new JSONObject(result
												.toString())
												.getString("AppSign");
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}.start();
							getNetwork(context);
							setNetwork(context);
							dialog.dismiss();
						}
					});

			((Button) view.findViewById(R.id.no)).setText("设置网络");
			((Button) view.findViewById(R.id.no))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = null;
							if (android.os.Build.VERSION.SDK_INT > 10) { // 判断手机系统的版本
								intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
							} else {
								intent = new Intent();
								ComponentName component = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
								intent.setComponent(component);
								intent.setAction("android.intent.action.VIEW");
							}
							context.startActivity(intent);
							dialog.dismiss();
						}
					});
		}
	}

	// 获取网络
	public void getNetwork(Context context) {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (mNetworkInfo != null) {
			if (mNetworkInfo.isConnected()) {
				isNetwork = true;
			} else {
				isNetwork = false;
			}
		} else {
			isNetwork = false;
		}
	}
	/**
	 * HttpClient请求数据 新接口数据
	 * @param url
	 * @return
	 */
	public StringBuffer getData(String url){
		StringBuffer result = new StringBuffer();
		HttpClient client;
		try {
			HttpGet get = new HttpGet(hostName + url);
			client = new DefaultHttpClient();
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
			HttpResponse response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream inputStream = response.getEntity().getContent();
				BufferedReader bf=new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
				String line="";  
				while((line=bf.readLine())!=null){  
					result.append(line);  
				}  
			}
		} catch (ClientProtocolException e) {
			return new StringBuffer();
		} catch (IOException e) {
			return new StringBuffer();
		}
		return result;
	}
	public StringBuffer getArray(String portName) {
		StringBuffer result = new StringBuffer();
		try {
			URL url = new URL(hostName + portName);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setConnectTimeout(10000);
			conn.connect();
			InputStreamReader reader = new InputStreamReader(conn.getInputStream());
			char[] c = new char[1024];
			
			int length = -1;
			while ((length = reader.read(c)) != -1) {
				result.append(c, 0, length);
			}
			reader.close();
			Log.i("StringBuffer", "here");
//			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return new StringBuffer();
		}
		return result;
	}

	/**
	 * 1、扫描条形码获取商品guid
	 * 
	 * */
	public StringBuffer getArray1(String portName) {
		try {
			URL url = new URL(urlName + portName);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setConnectTimeout(6000);
			conn.connect();

			InputStreamReader reader = new InputStreamReader(
					conn.getInputStream());
			char[] c = new char[1024];
			StringBuffer result = new StringBuffer();
			int length = -1;
			while ((length = reader.read(c)) != -1) {
				result.append(c, 0, length);
			}
			reader.close();
			return result;
		} catch (Exception e) {
			return new StringBuffer();
		}
	}

	public Bitmap getImage(String portName) {
		try {
			URL url = new URL(portName);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setConnectTimeout(6000);
			conn.connect();
			InputStream is = conn.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			is.close();
			return bitmap;
		} catch (Exception e) {
			return null;
		}
	}

	// HttpPost
	public StringBuffer postData(String portName, String content) {
		try {
			String path = hostName + portName;
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			conn.connect();
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.write(content.getBytes());
			out.flush();
			out.close();

			InputStreamReader reader = new InputStreamReader(
					conn.getInputStream());
			char[] c = new char[1024];
			StringBuffer result = new StringBuffer();
			int length = -1;
			while ((length = reader.read(c)) != -1) {
				result.append(c, 0, length);
			}
			reader.close();
			return result;
		} catch (IOException e) {
			return new StringBuffer();
		}
	}

	public StringBuffer postData2(String portName, String content) {
		try {
//			String path = urlName + portName;
			String path = "http://www.6868xp.com" + portName;
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.connect();
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.write(content.getBytes());
			out.flush();
			out.close();

			InputStreamReader reader = new InputStreamReader(
					conn.getInputStream());
			char[] c = new char[1024];
			StringBuffer result = new StringBuffer();
			int length = -1;
			while ((length = reader.read(c)) != -1) {
				result.append(c, 0, length);
			}
			reader.close();
			return result;
		} catch (IOException e) {
			return new StringBuffer();
		}
	}

	// HttpPost
	public StringBuffer postJSON(String portName, String content) {
		try {
			String path = hostName + portName;
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.connect();
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.write(content.getBytes());
			out.flush();
			out.close();

			InputStreamReader reader = new InputStreamReader(
					conn.getInputStream());
			char[] c = new char[1024];
			StringBuffer result = new StringBuffer();
			int length = -1;
			while ((length = reader.read(c)) != -1) {
				result.append(c, 0, length);
			}
			reader.close();
			return result;
		} catch (IOException e) {
			return new StringBuffer();
		}
	}

	// 编码
	public String escape(String src) {
		int i;
		char j;
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length() * 6);
		for (i = 0; i < src.length(); i++) {
			j = src.charAt(i);
			if (Character.isDigit(j) || Character.isLowerCase(j)
					|| Character.isUpperCase(j))
				tmp.append(j);
			else if (j < 256) {
				tmp.append("%");
				if (j < 16)
					tmp.append("0");
				tmp.append(Integer.toString(j, 16));
			} else {
				tmp.append("%u");
				tmp.append(Integer.toString(j, 16));
			}
		}
		return tmp.toString();
	}

	@SuppressWarnings("deprecation")
	public void showMenu(final Context context, View id) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.shortcut, null);
		PopupWindow window = new PopupWindow(view,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		window.setBackgroundDrawable(new BitmapDrawable());
		window.setFocusable(true);
		Rect frame = new Rect();
		MainActivity.instance.getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(frame);
		window.showAtLocation(id, Gravity.TOP, Width, Height + frame.top);

		if (cartNum > 0) {
			((ImageView) view.findViewById(R.id.point))
					.setVisibility(View.VISIBLE);
		} else {
			((ImageView) view.findViewById(R.id.point))
					.setVisibility(View.GONE);
		}

		((LinearLayout) view.findViewById(R.id.menu1))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						context.startActivity(new Intent(context,
								MainActivity.class));
						((Activity) context).finish();
					}
				});
		((LinearLayout) view.findViewById(R.id.menu2))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						context.startActivity(new Intent(context,
								MemberActivity.class));
						((Activity) context).finish();
					}
				});
		((LinearLayout) view.findViewById(R.id.menu3))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						context.startActivity(new Intent(context,
								CartActivity.class));
						((Activity) context).finish();
					}
				});
		((LinearLayout) view.findViewById(R.id.menu4))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						context.startActivity(new Intent(context,
								SetupActivity.class));
						((Activity) context).finish();
					}
				});
	}

}
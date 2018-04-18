package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
//设置
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class SetupActivity extends Activity {

	private HttpConn httpget = new HttpConn();
	private ProgressDialog pBar;
	private int verCode, newCode, fileSize, sumSize;
	private InputStream is;
	private FileOutputStream fos;
	public boolean isFirstPaymentPwd;// 有没有设置支付密码

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);
		init();
	}

	@Override
	protected void onResume() {
		getPayment();
		super.onResume();
	}

	private void init() {
		initLayout();

		if (HttpConn.showImage) {
			((ImageView) findViewById(R.id.noimage))
					.setBackgroundResource(R.drawable.noimage);
		} else {
			((ImageView) findViewById(R.id.noimage))
					.setBackgroundResource(R.drawable.noimage1);
		}

	}

	// 初始化
	public void initLayout() {
		// 返回
		((LinearLayout) findViewById(R.id.back))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
		// 快捷方式
		((LinearLayout) findViewById(R.id.more))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						httpget.showMenu(SetupActivity.this,
								findViewById(R.id.activity_setup));
					}
				});
		// 切换无图
		((ImageView) findViewById(R.id.noimage))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!HttpConn.showImage) {
							((ImageView) findViewById(R.id.noimage))
									.setBackgroundResource(R.drawable.noimage);
							HttpConn.showImage = true;
							// MainActivity.instance.getBanner();
							// MainActivity.instance.initGridView();
						} else {
							((ImageView) findViewById(R.id.noimage))
									.setBackgroundResource(R.drawable.noimage1);
							HttpConn.showImage = false;
							// MainActivity.instance.getBanner();
							// MainActivity.instance.initGridView();
						}
					}
				});
		// 清除缓存
		((RelativeLayout) findViewById(R.id.clear))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						final Dialog dialog = new Dialog(SetupActivity.this,
								R.style.MyDialog);
						View view = LayoutInflater.from(getBaseContext())
								.inflate(R.layout.dialog, null);
						((TextView) view.findViewById(R.id.dialog_text))
								.setText("是否清除缓存图片？");
						dialog.setContentView(view);
						dialog.show();
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
										File defaultFile = new File(
												"/data/data/com.shopnum1/databases");
										File cacheFile = new File(
												Environment
														.getExternalStorageDirectory()
														.getPath()
														+ "/Android/data/com.shopnum1/cache");
										if (defaultFile != null
												&& defaultFile.exists()
												&& defaultFile.isDirectory()) {
											deleteFilesByDirectory(defaultFile);
										}
										if (cacheFile != null
												&& cacheFile.exists()
												&& cacheFile.isDirectory()) {
											deleteFilesByDirectory(cacheFile);
										}
										dialog.dismiss();
									}
								});
					}
				});
		// 修改登录密码
		((RelativeLayout) findViewById(R.id.pwd))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
							startActivity(new Intent(getApplicationContext(),UserLoginPwd.class));
					}
				});
		// 设置支付密码
		((RelativeLayout) findViewById(R.id.pwd2))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (isFirstPaymentPwd) {
							startActivity(new Intent(getBaseContext(),PayPasswordActivity.class));
						} else {
							startActivity(new Intent(getApplicationContext(),PayPasswordActivity2.class));
						}
						overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
					}
				});
		// 检测版本更新
		((RelativeLayout) findViewById(R.id.rl_check_version))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						update();
					}
				});
		// 关于
		((RelativeLayout) findViewById(R.id.about))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								UserAbout.class));
					}
				});
		// 退出登录
		((Button) findViewById(R.id.exit))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						exit();
					}
				});
	}

	// 删除目录文件
	private static void deleteFilesByDirectory(File directory) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			for (File item : directory.listFiles())
				item.delete();
		}
	}

	public void exit() {
		HttpConn.cartNum = 0;
		Editor editor = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).edit();
		HttpConn.isLogin = false;
		editor.putBoolean("islogin", false);
		editor.commit();
		startActivity(new Intent(getApplicationContext(), UserLogin.class));
		finish();
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1) {
			exit();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				isFirstPaymentPwd = true;
				
				break;
			case 2:
				isFirstPaymentPwd = false;
				
				break;
			case 3:
				if (newCode > verCode) {
					doNewVersionUpdate();
				} else {
					Toast.makeText(getApplicationContext(), "当前已经是最新版本!",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case 4:
				pBar.setProgress(sumSize * 100 / fileSize);
				break;
			case 5:
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
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * 自动更新
	 */
	public void update() {
		new Thread() {
			@Override
			public void run() {
				StringBuffer result = httpget
						.getArray("/DownLoad/main/VersionCode.xml");
				if (result.toString() != "") {
					newCode = parser(result.toString());
					try {
						verCode = getPackageManager().getPackageInfo(
								getPackageName(), 0).versionCode;
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
					Message msg = new Message();
					msg.what = 3;
					handler.sendMessage(msg);
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

	private void doNewVersionUpdate() {
		Dialog dialog = new AlertDialog.Builder(SetupActivity.this)
				.setTitle("软件更新")
				.setMessage("发现有新版本")
				.setPositiveButton("立即更新",
						new DialogInterface.OnClickListener() {
							@SuppressWarnings("deprecation")
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								pBar = new ProgressDialog(SetupActivity.this);
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
															"P8686.apk");
													if (file.exists()) {
														file.delete();
													}
													dialog.dismiss();
												} catch (IOException e) {
													e.printStackTrace();
												}
											}
										});
								downFile(HttpConn.hostName
										+ "/DownLoad/main/Main.apk");
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
						msg.what = 4;
						handler.sendMessage(msg);
					}
					is.close();
					fos.close();
					Message msg = new Message();
					msg.what = 5;
					handler.sendMessage(msg);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	// 判断是否设置过支付密码
	public void getPayment() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				StringBuffer result = httpget
						.getArray("/api/getpaypwd/?memLoginID="
								+ HttpConn.username + "&AppSign="
								+ HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					if (new JSONObject(result.toString()).getString("Data")
							.equals("")) {
						msg.what = 1;
					} else {
						msg.what = 2;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}).start();
	}
}
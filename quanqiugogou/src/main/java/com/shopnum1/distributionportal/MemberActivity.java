package com.shopnum1.distributionportal;


import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.CommonUtility;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.PhotoZoomUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zxing.view.CircleImageView;

public class MemberActivity extends Activity {

	private HttpConn httpget = new HttpConn();
	private String username;
	private Double AdvancePayment;
	private String Score;
	private Dialog dialog, pBar;
	private Bitmap bitmap;
	private CircleImageView userImg;
	private String Photo; // 获取用户头像地址
	private String imgUrl; // 上传用户头像地址
	private File file ;
	private Uri fileUri;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_member);
		initLayout();
		userImg = (CircleImageView) findViewById(R.id.user_head);
		httpget.getNetwork(this); // 判断网络
		initMenu();
	}

	// 初始化
	public void initLayout() {
		findViewById(R.id.refund).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MemberActivity.this, ReturnMoeny.class));
			}
		});

		// 主页
		((RelativeLayout) findViewById(R.id.imageButton1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								MainActivity.class));
						overridePendingTransition(android.R.anim.fade_in,
								android.R.anim.fade_out);
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
					}

				});
		// 购物车
		((RelativeLayout) findViewById(R.id.imageButton3))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),CartActivity.class));
						overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
					}
				});

		// 设置
		findViewById(R.id.setup).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								SetupActivity.class));
					}

				});

		// 预存款布局
		((TextView) findViewById(R.id.paymentNum))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// 跳到预存款详细界面
						Intent intent = new Intent(getBaseContext(),PaymentDetail.class);
						intent.putExtra("AdvancePayment", AdvancePayment);
						startActivity(intent);
					}
				});
		 // 充值
		findViewById(R.id.rl_recharge).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// 跳到预存款详细界面
						Intent intent = new Intent(getBaseContext(),
								RechargeActivity.class);
						intent.putExtra("AdvancePayment", AdvancePayment);
						startActivity(intent);
					}
				});
		}

	// 获取用户信息
	public void getData() {
		new Thread() {
			public void run() {
				Message msg = Message.obtain();
				try {
					StringBuffer result = httpget
							.getArray("/api/accountget/?MemLoginID="
									+ HttpConn.username + "&AppSign="
									+ HttpConn.AppSign);
					username = new JSONObject(result.toString()).getJSONObject(
							"AccoutInfo").getString("MemLoginID");
					AdvancePayment = new JSONObject(result.toString())
							.getJSONObject("AccoutInfo").getDouble(
									"AdvancePayment");
					Score = new JSONObject(result.toString()).getJSONObject(
							"AccoutInfo").getString("Score");
					Photo = new JSONObject(result.toString()).getJSONObject(
							"AccoutInfo").getString("Photo");

//					ImageLoader.getInstance().displayImage(Photo,
//							userImg, MyApplication.options);

					msg.what = 1;
				} catch (JSONException e) {
					msg.what = 0;
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	// 菜单列表
	public void initMenu() {
		// 积分明细
		((TextView) findViewById(R.id.score))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								MemberScoreDetail.class));
					}

				});

		// 上传照片
				findViewById(R.id.relativeLayout2).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								dialog = new Dialog(MemberActivity.this, R.style.dialog);
								dialog.setContentView(R.layout.dialog_userhead);
								Window dialogWindow = dialog.getWindow();
								dialogWindow
										.setGravity(Gravity.CENTER | Gravity.CENTER);
								// 拍照片
								((Button) dialog.findViewById(R.id.btn_take_photo))
										.setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												//先验证手机是否有sdcard 
												String status=Environment.getExternalStorageState(); 
												if (status.equals(Environment.MEDIA_MOUNTED)) {
													try {
														File dir = new File(Environment.getExternalStorageDirectory() + "/img");
														if (!dir.exists())
															dir.mkdirs();
														String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
														file = new File(dir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
														fileUri = Uri.fromFile(file);
														
														Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
														intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
														intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
														intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
														startActivityForResult(intent, 1);
													} catch (ActivityNotFoundException e) {
														Toast.makeText(MemberActivity.this, "没有找到储存目录", Toast.LENGTH_LONG).show();
													}
												} else {
													Toast.makeText(MemberActivity.this, "没有储存卡", Toast.LENGTH_LONG).show();
												}
											}
										});
								// 相册选择
								((Button) dialog.findViewById(R.id.btn_pick_photo))
										.setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												try {
													dialog.dismiss();
													Intent intent=new Intent(Intent.ACTION_GET_CONTENT);  
								                    intent.addCategory(Intent.CATEGORY_OPENABLE);  
								                    intent.setType("image/*");  
													startActivityForResult(intent, 2);
													
												 } catch (ActivityNotFoundException e) {
													 Toast.makeText(MemberActivity.this, "未找到照片", Toast.LENGTH_LONG).show();
												 }
											}
										});

								((Button) dialog.findViewById(R.id.btn_cancel))
										.setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												dialog.dismiss();
											}
										});
								dialog.show();
								try {
									dialog.dismiss();
									Intent intent = new Intent(
											Intent.ACTION_GET_CONTENT, null);
									intent.setType("image/*");
									intent.putExtra("crop", "true");
									intent.putExtra("aspectX", 1);
									intent.putExtra("aspectY", 1);
									// outputX outputY 是裁剪图片宽高
									intent.putExtra("outputX", 240);
									intent.putExtra("outputY", 240);
									intent.putExtra("return-data", true);
									startActivityForResult(intent, 2);
								} catch (ActivityNotFoundException e) {
									Toast.makeText(MemberActivity.this, "未能找到照片",
											Toast.LENGTH_LONG).show();
								}
							}
						});
		// 待支付
		((LinearLayout) findViewById(R.id.daizhifu))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getBaseContext(),
								OrderStatusActivity.class);
						intent.putExtra("type", "1");
						startActivity(intent);
					}

				});
		// 待收货
		((LinearLayout) findViewById(R.id.daishouhuo))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getBaseContext(),
								OrderStatusActivity.class);
						intent.putExtra("type", "3");
						startActivity(intent);
					}
				});
		// 待发货
		((LinearLayout) findViewById(R.id.daifahuo))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
//						Intent intent = new Intent(getBaseContext(),
//								OrderActivity.class);
//						intent.putExtra("title", "待发货");
//						intent.putExtra("type", 2);
//						startActivity(intent);
						Intent intent = new Intent(getBaseContext(),
								OrderStatusActivity.class);
						intent.putExtra("type", "2");
						startActivity(intent);
					}
				});
		// 全部订单
		((RelativeLayout) findViewById(R.id.orderall))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getBaseContext(),
								OrderStatusActivity.class);
						intent.putExtra("type", "0");
						startActivity(intent);
//						Intent intent = new Intent(getBaseContext(),
//								OrderActivity.class);
//						intent.putExtra("title", "全部订单");
//						intent.putExtra("type", 0);
//						startActivity(intent);
					}

				});
		// 评价
		((LinearLayout) findViewById(R.id.wait_access))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
//						Intent intent = new Intent(getBaseContext(),
//								OrderActivity.class);
//						intent.putExtra("title", "全部订单");
//						intent.putExtra("type", 8);
//						startActivity(intent);
						Intent intent = new Intent(getBaseContext(),
								OrderStatusActivity.class);
						intent.putExtra("type", "8");
						startActivity(intent);
					}
				});
		// 我的收藏
		((RelativeLayout) findViewById(R.id.collect_layout))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								MemberCollect.class));
					}

				});
		// 我的消息
		((RelativeLayout) findViewById(R.id.msg_layout))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								MemberMessage.class));
					}

				});
		// 收货地址
		((RelativeLayout) findViewById(R.id.address_layout))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								MemberAddress.class));
					}

				});
		// // 去评价
		// ((RelativeLayout) findViewById(R.id.assess)).setOnClickListener(new
		// OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// startActivity(new Intent(getBaseContext(), AssessActivity.class));
		// }
		// });
		// 去晒单
		((RelativeLayout) findViewById(R.id.show_layout))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								OrderList.class));
					}

				});
		// 发展会员
		((RelativeLayout) findViewById(R.id.recommend))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								MyTeamActivity.class));
					}

				});
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(getApplicationContext(), "获取用户信息失败",
						Toast.LENGTH_SHORT).show();
				break;
			case 1:
				Editor editor = PreferenceManager.getDefaultSharedPreferences(
						getBaseContext()).edit();
				HttpConn.username = UserLogin.toUTF8(username);
				HttpConn.UserName = username;
				editor.putString("name", username);
				editor.commit();

				((TextView) findViewById(R.id.username)).setText(username);
				((TextView) findViewById(R.id.score))
						.setText("会员积分 : " + Score);
				DecimalFormat decimalFormat = new DecimalFormat("#.00");
				String format = decimalFormat.format(AdvancePayment);
				if (".00".equals(format)) {
					format = "0";
				}
				((TextView) findViewById(R.id.paymentNum)).setText("预存款:"
						+ format + " 元");
				if (Photo == null || Photo.equals("") || Photo.equals("null")) {
					userImg.setImageResource(R.drawable.user_head);
				} else {
					if (HttpConn.showImage) {
						String userImgUrl;
						if (Photo.startsWith("~")) {
							userImgUrl = HttpConn.urlName + Photo.substring(1);
							ImageLoader.getInstance().displayImage(userImgUrl,
									userImg, MyApplication.options);
						} else {
							ImageLoader.getInstance().displayImage(
									Photo, userImg,
									MyApplication.options);
						}
					} else {
						userImg.setImageResource(R.drawable.user_head);
					}
				}
				break;
			case 2:
				if (imgUrl == null || imgUrl.equals("")
						|| imgUrl.equals("null")) {
					userImg.setImageResource(R.drawable.user_head);
					Toast.makeText(getApplicationContext(), "设置头像失败",
							Toast.LENGTH_SHORT).show();
				} else {
					// 上传用户头像地址
					updatePhoto();
				}
				pBar.dismiss();
				break;
			case 3:
				if (msg.obj != null && msg.obj.equals("200")) {
					if (HttpConn.showImage) {
						ImageLoader.getInstance().displayImage(
								HttpConn.urlName + imgUrl, userImg,
								MyApplication.options);
					} else {
						userImg.setImageResource(R.drawable.user_head);
					}
				} else {
					String userImgUrl;
					if (Photo != null && !Photo.equals("")) {
						if (Photo.startsWith("~")) {
							userImgUrl = HttpConn.urlName + Photo.substring(1);
							ImageLoader.getInstance().displayImage(userImgUrl,
									userImg, MyApplication.options);
						} else {
							ImageLoader.getInstance().displayImage(
									HttpConn.urlName + Photo, userImg,
									MyApplication.options);
						}
					} else {
						userImg.setImageResource(R.drawable.user_head);
					}
					Toast.makeText(getApplicationContext(), "设置头像失败",
							Toast.LENGTH_SHORT).show();
				}

				break;
			}
			super.handleMessage(msg);
		}
	};

	/** 上传头像地址 */
	private void updatePhoto() {
		new Thread() {
			public void run() {
				Message msg = Message.obtain();
				msg.what = 3;
				try {
					StringBuffer result = httpget
							.getArray("/api/updatephoto/?MemLoginID="
									+ HttpConn.UserName + "&Photo=" + imgUrl
									+ "&AppSign=" + HttpConn.AppSign);
					msg.obj = new JSONObject(result.toString())
							.getString("return"); // 200 成功 404 失败
				} catch (JSONException e) {
					msg.obj = "";
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	public void postData() {
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		new Thread() {
			public void run() {

//				String strimageToBase64 = "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UmVjdFRvcGxvbmcAAAAAADhCSU0D7QAAAAAAEABIAAAAAQACAEgAAAABAAI4QklNBCYAAAAAAA4AAAAAAAAAAAAAP4AAADhCSU0EDQAAAAAABAAAAB44QklNBBkAAAAAAAQAAAAeOEJJTQPzAAAAAAAJAAAAAAAAAAABADhCSU0nEAAAAAAACgABAAAAAAAAAAI4QklNA/UAAAAAAEgAL2ZmAAEAbGZmAAYAAAAAAAEAL2ZmAAEAoZmaAAYAAAAAAAEAMgAAAAEAWgAAAAYAAAAAAAEANQAAAAEALQAAAAYAAAAAAAE4QklNA/gAAAAAAHAAAP////////////////////////////8D6AAAAAD/////////////////////////////A+gAAAAA/////////////////////////////wPoAAAAAP////////////////////////////8D6AAAOEJJTQQIAAAAAAAQAAAAAQAAAkAAAAJAAAAAADhCSU0EHgAAAAAABAAAAAA4QklNBBoAAAAAA0EAAAAGAAAAAAAAAAAAAAEKAAABHwAAAAYAaQBuAGQAZQB4ADEAAAABAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAR8AAAEKAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAEAAAAAEAAAAAAABudWxsAAAAAgAAAAZib3VuZHNPYmpjAAAAAQAAAAAAAFJjdDEAAAAEAAAAAFRvcCBsb25nAAAAAAAAAABMZWZ0bG9uZwAAAAAAAAAAQnRvbWxvbmcAAAEKAAAAAFJnaHRsb25nAAABHwAAAAZzbGljZXNWbExzAAAAAU9iamMAAAABAAAAAAAFc2xpY2UAAAASAAAAB3NsaWNlSURsb25nAAAAAAAAAAdncm91cElEbG9uZwAAAAAAAAAGb3JpZ2luZW51bQAAAAxFU2xpY2VPcmlnaW4AAAANYXV0b0dlbmVyYXRlZAAAAABUeXBlZW51bQAAAApFU2xpY2VUeXBlAAAAAEltZyAAAAAGYm91bmRzT2JqYwAAAAEAAAAAAABSY3QxAAAABAAAAABUb3AgbG9uZwAAAAAAAAAATGVmdGxvbmcAAAAAAAAAAEJ0b21sb25nAAABCgAAAABSZ2h0bG9uZwAAAR8AAAADdXJs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rTrC5VJL7En6vUO+07fZ845+SjXEfyZ938VzKSIWl9Cs+mfRj0tNkTER+Zu92z9zco/pF5+kisGz6B+lTfpVwCSSnvj6qb9KuCSSU95+lSHqSuDSSU//2ThCSU0EIQAAAAAAUwAAAAEBAAAADwBBAGQAbwBiAGUAIABQAGgAbwB0AG8AcwBoAG8AcAAAABIAQQBkAG8AYgBlACAAUABoAG8AdABvAHMAaABvAHAAIABDAEMAAAABADhCSU0EBgAAAAAAB///AAAAAQEA/+EMvmh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8APD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS41LWMwMTQgNzkuMTUxNDgxLCAyMDEzLzAzLzEzLTEyOjA5OjE1ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1sbnM6ZGM9Imh0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvIiB4bWxuczpwaG90b3Nob3A9Imh0dHA6Ly9ucy5hZG9iZS5jb20vcGhvdG9zaG9wLzEuMC8iIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1wTU06RG9jdW1lbnRJRD0iOTJDNjQwN0I0NENGMDE0MEJCNkFBQURENzVERTYyNTEiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6OTMwMmU3NTUtMWY2NC00MGJkLTg3MDMtMjQ0NDJhYTE2NTU2IiB4bXBNTTpPcmlnaW5hbERvY3VtZW50SUQ9IjkyQzY0MDdCNDRDRjAxNDBCQjZBQUFERDc1REU2MjUxIiBkYzpmb3JtYXQ9ImltYWdlL2pwZWciIHBob3Rvc2hvcDpDb2xvck1vZGU9IjMiIHhtcDpDcmVhdGVEYXRlPSIyMDE3LTA3LTMxVDE3OjI1OjA0KzA4OjAwIiB4bXA6TW9kaWZ5RGF0ZT0iMjAxNy0wNy0zMVQxOTo0NDozOSswODowMCIgeG1wOk1ldGFkYXRhRGF0ZT0iMjAxNy0wNy0zMVQxOTo0NDozOSswODowMCI+IDx4bXBNTTpIaXN0b3J5PiA8cmRmOlNlcT4gPHJkZjpsaSBzdEV2dDphY3Rpb249InNhdmVkIiBzdEV2dDppbnN0YW5jZUlEPSJ4bXAuaWlkOjkzMDJlNzU1LTFmNjQtNDBiZC04NzAzLTI0NDQyYWExNjU1NiIgc3RFdnQ6d2hlbj0iMjAxNy0wNy0zMVQxOTo0NDozOSswODowMCIgc3RFdnQ6c29mdHdhcmVBZ2VudD0iQWRvYmUgUGhvdG9zaG9wIENDIChNYWNpbnRvc2gpIiBzdEV2dDpjaGFuZ2VkPSIvIi8+IDwvcmRmOlNlcT4gPC94bXBNTTpIaXN0b3J5PiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8P3hwYWNrZXQgZW5kPSJ3Ij8+/+4ADkFkb2JlAGSAAAAAAf/bAIQAEg4ODhAOFRAQFR4TERMeIxoVFRojIhcXFxcXIhEMDAwMDAwRDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAEUExMWGRYbFxcbFA4ODhQUDg4ODhQRDAwMDAwREQwMDAwMDBEMDAwMDAwMDAwM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";


				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				StringBuffer result = httpget.postData2(
						"/Api/uploadpic.ashx",
						"memloginid="
								+ HttpConn.UserName
								+ "&filedata="
								+ Base64.encodeToString(baos.toByteArray(),
										Base64.DEFAULT).replace("+", "%2B"));
				Message msg = new Message();
				msg.what = 2;
				try {
					imgUrl = new JSONObject(result.toString())
							.getString("success");
				} catch (JSONException e) {
					imgUrl = "";
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	@Override
	protected void onResume() {
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
			getData();
		} else {
			httpget.setNetwork(this); // 设置网络
		}
		super.onResume();
	}
	public void startPhotoZoom(Uri uri) {  
        if (uri == null) {  
            Log.i("tag", "The uri is not exist.");  
            return;  
        }  
     
          
        Intent intent = new Intent("com.android.camera.action.CROP");  
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {  
            String url=PhotoZoomUtil.getPath(this,uri);
			imgUrl =url;
            intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");  
        }else{  
            intent.setDataAndType(uri, "image/*");  
        }  
          
        // 设置裁剪  
        intent.putExtra("crop", "true");  
        // aspectX aspectY 是宽高的比例  
        intent.putExtra("aspectX", 1);  
        intent.putExtra("aspectY", 1);  
        // outputX outputY 是裁剪图片宽高  
        intent.putExtra("outputX", 200);  
        intent.putExtra("outputY", 200);  
        intent.putExtra("return-data", true);  
        startActivityForResult(intent, 3);  
    }  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case Activity.RESULT_OK:
		if (data != null) {
				
				final Bundle extras = data.getExtras();
				if (extras != null) {

					bitmap = (Bitmap) extras.get("data");
					
				}
			}
		
	
		if (requestCode == 1) {
			final BitmapFactory.Options options = new BitmapFactory.Options();  
		    options.inJustDecodeBounds = true;  
		    Log.i("file path", file.getAbsolutePath());
		    int degree = CommonUtility.readPictureDegree(file.getAbsolutePath());
		    Log.i("degree", degree + "");
		    
		    BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		    options.inSampleSize = CommonUtility.calculateInSampleSize(options, 400, 400);  
		    options.inJustDecodeBounds = false;
		    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options); 
		    bitmap = CommonUtility.rotateBitmap(bitmap, degree);
		    //startPhotoZoom(data.getData());
		} else if (requestCode == 2) {
			//开始裁剪图片
			startPhotoZoom(data.getData());
		}else if(requestCode == 3){
			//获得裁剪后的数据
			if (data != null) { 
            	Bundle extras = data.getExtras();  
            	if (extras != null) {  
            		bitmap = extras.getParcelable("data");  
                }  
            }  
		}
		
		if (bitmap != null) {
		
			userImg.setImageBitmap(bitmap);
			
			postData();
			//iv_img_delete.setVisibility(View.VISIBLE);
		}
		
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 返回主页
	@Override
	public void onBackPressed() {
		startActivity(new Intent(getBaseContext(), MainActivity.class));
		super.onBackPressed();
	}

}
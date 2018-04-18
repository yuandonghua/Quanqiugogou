package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.alipay.sdk.pay.PayUtil;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
public class OrderAllToPay extends Activity {
	private String Guid;
	private String Number;
	private Double ProductPrice, ScorePrice, DispatchPrice;
	private HttpConn httpget = new HttpConn();
	private ArrayList<Map<String, String>> PaymentList;
	private Dialog pBar;
	private String payMode;
	private Button okbtn;
	private String paymentGuid;
	private JSONArray orderList, paymentList;
	private String productList;
	private String format;
	private boolean isFirstPaymentPwd;
	private JSONArray array;
	String paymentType;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_paymode);
		isSetPaymentPassword();
		init();
		getPayment();
	}
//	@Override
//	protected void onResume() {
//		getPayment();
//		super.onResume();
//	}
	private void init() {
		try {
			productList = getIntent().getStringExtra("ProductList");
			productArray = new JSONArray(productList);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ScorePrice = getIntent().getDoubleExtra("ScorePrice", 0.00);
		ProductPrice = getIntent().getDoubleExtra("ProductPrice", 0.00);
		DispatchPrice = getIntent().getDoubleExtra("DispatchPrice", 0.00);
		df = new DecimalFormat("#.00");
		shouldPrice = (ProductPrice - ScorePrice + DispatchPrice);
		format = df.format(shouldPrice);
		if(shouldPrice < 1.00){
			format= "0"+format;
		}
		((TextView) findViewById(R.id.tv_total)).setText(format + "元");
		Number = getIntent().getStringExtra("OrderNumber");
		paymentGuid = getIntent().getStringExtra("PaymentGuid");
		ListView lv2 = (ListView) findViewById(R.id.lv2);
		MyAdapter2 adapter2 = new MyAdapter2();
		lv2.setAdapter(adapter2);
	
		findViewById(R.id.iv_back).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						finish();
					}
				});
		okbtn = (Button) findViewById(R.id.okbtn);
		okbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					paymentType = array.getJSONObject(0).getString("PaymentType");
					if ("Alipay.aspx".equals(paymentType)) {
						Intent intent = new Intent(new Intent(getApplicationContext(),AlipayActivity.class));
						intent.putExtra("order", Number);
						intent.putExtra("source", "PayModeActivity");
						intent.putExtra("total", shouldPrice + "");
						startActivity(intent);
						finish();
					} else if ("PreDeposits.aspx".equals(paymentType)) {
						isSetPaymentPassword();
					} else if ("JDpay.aspx".equals(paymentType)) {
						Intent intent = new Intent(new Intent(getApplicationContext(),JingDongActivity.class));
						intent.putExtra("order", Number);
						intent.putExtra("total", shouldPrice + "");
						startActivity(intent);
						finish();
					} else if ("Tenpay.aspx".equals(paymentType)) {
						Intent intent = new Intent(new Intent(getApplicationContext(),TenpayActivity.class));
						intent.putExtra("order_no", Number);
						intent.putExtra("order_price", shouldPrice+ "");
						startActivity(intent);
						finish();
					} else if ("Weixin.aspx".equals(paymentType)) {
						Toast.makeText(getApplicationContext(), "请绑定商户ID", Toast.LENGTH_SHORT).show();
					} else if ("AlipaySDK.aspx".equals(paymentType)) {
						PayUtil PayUtil = new PayUtil(
								OrderAllToPay.this, "分销门户", Number,
								shouldPrice + "",
								new PayUtil.CallbackListener() {

									@Override
									public void updateOrderState() {
										updateState(Number);
									}
								});
						PayUtil.pay();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

	}

	// 获取支付方式
	public void getPayment() {
		new Thread() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = 2;
				try {
					String payMentListUrl = "/api/PayMentList/?Source=" + "Android"+ "&AppSign=" + HttpConn.AppSign+"&agentID="+MyApplication.agentId;
					StringBuffer result = httpget.getArray(payMentListUrl);
					array = new JSONObject(result.toString()).getJSONArray("Data");
					PaymentList = new ArrayList<Map<String, String>>();
					for (int i = 0; i < array.length(); i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("guid", array.getJSONObject(i)
								.getString("Guid"));
						map.put("name", array.getJSONObject(i)
								.getString("NAME"));
						if (array.getJSONObject(i).getString("NAME").startsWith("支付宝支付")) {
							if (array.getJSONObject(i).getString("Public_Key") != null
									&& !"".equals(array.getJSONObject(i)
											.getString("Public_Key"))) {
								PayUtil.RSA_PUBLIC = array.getJSONObject(i)
										.getString("Public_Key");
							}
							if (array.getJSONObject(i).getString("Private_Key") != null
									&& !"".equals(array.getJSONObject(i)
											.getString("Private_Key"))) {
								PayUtil.RSA_PRIVATE = array.getJSONObject(i)
										.getString("Private_Key");
							}
							if (array.getJSONObject(i)
									.getString("MerchantCode") != null
									&& !"".equals(array.getJSONObject(i)
											.getString("Partner"))) {
								PayUtil.PARTNER = array.getJSONObject(i)
										.getString("MerchantCode");
							}
							if (array.getJSONObject(i).getString("Email") != null
									&& !"".equals(array.getJSONObject(i)
											.getString("Email"))) {
								PayUtil.SELLER = array.getJSONObject(i)
										.getString("Email");
							}
						}
						PaymentList.add(map);
					}
					msg.obj = "1";
				} catch (JSONException e) {
					msg.obj = "0";
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				pBar.dismiss();
			case 1:
				pBar.dismiss();
				Intent intent2 = new Intent(new Intent(getApplicationContext(),
						OrderActivity.class));
				intent2.putExtra("type", 0);
				intent2.putExtra("title", "全部订单");
				startActivity(intent2);
				finish();
				break;
			case 2:
				String str = (String) msg.obj;
				if (str != null && !("").equals(str) && !("null").equals(str)) {
					if (str.equals("1")) {
						paymentDialog();
					}
				}
				break;
			case 4:
				isFirstPaymentPwd = true;
				break;
			case 5:
				isFirstPaymentPwd = false;
				break;
			case 6:
				pBar.dismiss();
				Toast.makeText(getApplicationContext(), "余额不足",Toast.LENGTH_SHORT).show();
				Intent intent1 = new Intent(new Intent(getApplicationContext(),OrderActivity.class));
				intent1.putExtra("type", 0);
				intent1.putExtra("title", "全部订单");
				startActivity(intent1);
				finish();
				break;
			case 7:
				pBar.dismiss();
				if (msg.obj.equals("true")) {
					Toast.makeText(getApplicationContext(), "付款成功",Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "付款失败",Toast.LENGTH_SHORT).show();
				}
				Intent intent = new Intent(new Intent(getApplicationContext(),OrderActivity.class));
				intent.putExtra("type", 0);
				intent.putExtra("title", "全部订单");
				startActivity(intent);
				finish();
				break;
			case 8:
				if (msg.obj.equals("202")) {
					getData();
				} else {
					Toast.makeText(getApplicationContext(), "订单更新失败",Toast.LENGTH_SHORT).show();
				}

				break;
			case 9:
				Toast.makeText(getApplicationContext(), "密码错误", 0).show();
				if (pBar.isShowing()) {
					pBar.dismiss();
				}
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				Intent intent3 = new Intent(new Intent(getApplicationContext(),OrderActivity.class));
				intent3.putExtra("type", 0);
				intent3.putExtra("title", "全部订单");
				startActivity(intent3);
				finish();
				break;
			default:
				break;
			}
		};
	};
	private JSONArray productArray;
	private DecimalFormat df;
	private Dialog dialog;
	private double shouldPrice;
	private MyAdapter adapter;

	// 选择支付方式
	public void paymentDialog() {

		if (PaymentList.size() > 0) {
			final ListView lv = (ListView) findViewById(R.id.lv);
			adapter = new MyAdapter();
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						final int position, long id) {
					for (int i = 0; i < lv.getChildCount(); i++) {
						if (position == i) {
							((ImageView) lv.getChildAt(i).findViewById(R.id.iv))
									.setImageResource(R.drawable.gouxuan_red);
						} else {
							((ImageView) lv.getChildAt(i).findViewById(R.id.iv))
									.setImageResource(R.drawable.gouxuan_gay);
						}
					}

					okbtn.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							String paymentType = null;
							try {
								paymentType = array.getJSONObject(position).getString("PaymentType");
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if ("Alipay.aspx".equals(paymentType)) {
								Intent intent = new Intent(new Intent(getApplicationContext(),AlipayActivity.class));
								intent.putExtra("order", Number);
								intent.putExtra("source", "PayModeActivity");
								intent.putExtra("total",((TextView) findViewById(R.id.tv_total)).getText().toString() + "");
								startActivity(intent);
								finish();
							} else if ("PreDeposits.aspx".equals(paymentType)) {
								prePayment();
							} else if ("JDpay.aspx".equals(paymentType)) {
								Intent intent = new Intent(new Intent(getApplicationContext(),JingDongActivity.class));
								intent.putExtra("order", Number);
								intent.putExtra("total",((TextView) findViewById(R.id.tv_total)).getText().toString() + "");
								startActivity(intent);
								finish();
							} else if ("Tenpay.aspx".equals(paymentType)) {
								Intent intent = new Intent(new Intent(getApplicationContext(),TenpayActivity.class));
								intent.putExtra("order_no", Number);
								intent.putExtra("order_price",((TextView) findViewById(R.id.tv_total)).getText().toString() + "");
								startActivity(intent);
								finish();
							} else if ("Weixin.aspx".equals(paymentType)) {
								// 微信支付
								Toast.makeText(OrderAllToPay.this, "请绑定您的商户号", 0).show();

							} else if ("AlipaySDK.aspx".equals(paymentType)) {
								PayUtil PayUtil = new PayUtil(OrderAllToPay.this,"分销门户",Number,
										shouldPrice + "",
										new PayUtil.CallbackListener() {
											@Override
											public void updateOrderState() {
												updateState(Number);
											}
										});
								PayUtil.pay();
							}
						}
					});

				}
			});
		}

	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return PaymentList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(getApplicationContext(),
					R.layout.paymode_item, null);
			payMode = PaymentList.get(position).get("name");
			((TextView) view.findViewById(R.id.tv_payname)).setText(payMode);
			if (position == 0) {
				((ImageView) view.findViewById(R.id.iv))
						.setBackgroundResource(R.drawable.gouxuan_red);
			} else {
				((ImageView) view.findViewById(R.id.iv))
						.setBackgroundResource(R.drawable.gouxuan_gay);
			}
			if (position == PaymentList.size() - 1) {
				view.findViewById(R.id.view_line).setVisibility(View.GONE);
			}
			return view;
		}
	}

	/**
	 *  预存款支付
	 */
	public void prePayment() {
		if (isFirstPaymentPwd) {
			startActivity(new Intent(getApplicationContext(),PayPasswordActivity.class));
			finish();
		} else {
			dialog = new Dialog(OrderAllToPay.this, R.style.MyDialog);
			final View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog3, null);
			dialog.setContentView(view);
			dialog.show();
			pBar = new Dialog(this, R.style.dialog);
			pBar.setContentView(R.layout.progress);

			((Button) view.findViewById(R.id.no)).setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							pBar.dismiss();
							dialog.dismiss();
							Intent intent = new Intent(new Intent(getApplicationContext(),OrderActivity.class));
							intent.putExtra("type", 0);
							intent.putExtra("title", "全部订单");
							startActivity(intent);
							finish();
						}
					});
			((Button) view.findViewById(R.id.yes)).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							dialog.dismiss();
							final String password = ((EditText) view.findViewById(R.id.edit)).getText().toString();
							if(TextUtils.isEmpty(password)){
								Toast.makeText(OrderAllToPay.this, "密码为空", 0).show();
							}else{
								pBar.show();
								new Thread(new Runnable() {
									@Override
									public void run() {
										try {
											// 检验原密码是否有误
											StringBuffer result = httpget
													.getArray("/api/checkequalpaypwd/?MemLoginID="
															+ HttpConn.username
															+ "&PayPwd="
															+ password
															+ "&AppSign="
															+ HttpConn.AppSign);
											Message msg = Message.obtain();
											if (new JSONObject(result.toString()).getString("return").equals("200")) {
												StringBuffer result2 = httpget.getArray("/api/accountget/?MemLoginID="+ HttpConn.UserName+ "&AppSign="+ HttpConn.AppSign);
												double AdvancePayment = new JSONObject(result2.toString()).getJSONObject("AccoutInfo").getDouble("AdvancePayment");
												if (AdvancePayment < shouldPrice) {
													msg.what = 6;
												} else {
													StringBuffer payresult = httpget
															.getArray("/api/order/BuyAdvancePayment/"
																	+ HttpConn.username
																	+ "?OrderNumber="
																	+ Number
																	+ "&PayPwd="
																	+ password
																	+ "&AppSign="
																	+ HttpConn.AppSign);
													msg.obj = new JSONObject(
															payresult.toString())
													.getString("sbool");
													msg.what = 7;
													if (payresult != null
															&& !("").equals(payresult)
															&& !("null")
															.equals(payresult)) {
														// 预存款支付状态
														msg.obj = new JSONObject(
																payresult
																.toString())
														.getString("sbool");
													} else {
														// 支付失败
														msg.obj = "false";
													}
												}
											} else {
												msg.what = 9;// 密码错误
											}
											handler.sendMessage(msg);
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
								}).start();
							}

						}
					});
		}
	}

//	public void method() {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				StringBuffer result2 = httpget.getArray("/api/UpdateOrderInfopayment?"+ "PaymentGuid=" + paymentGuid + "&Guid="
//								+ Guid + "&PaymentName=" + payMode);
//			}
//		}).start();
//	}

	public void getData() {
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();

		new Thread() {
			public void run() {
				String orderListUrl  = "/api/order/member/OrderList/?pageIndex=1&pageCount=100&memLoginID="
						+ HttpConn.username+ "&t="+ getIntent().getIntExtra("type", 0)
						+ "&AppSign=" + HttpConn.AppSign+"&agentID="+MyApplication.agentId;
				StringBuffer result = httpget.getArray(orderListUrl);
				StringBuffer payment = httpget.getArray("/api/payment/"+ "?AppSign=" + HttpConn.AppSign);
				StringBuffer refundList = httpget.getArray("/api/GetReturnOrderByMemLoginId?MemLoginID="
								+ HttpConn.username + "&AppSign="
								+ HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					orderList = new JSONObject(result.toString())
							.getJSONArray("Data");
					paymentList = new JSONObject(payment.toString())
							.getJSONArray("data");
					for (int i = 0; i < orderList.length(); i++) {
						if (refundList.toString().contains("[]")) {
							orderList.getJSONObject(i).put("OrderStatus", -1);
						} else {
							orderList.getJSONObject(i).put(
									"OrderStatus",
									new JSONObject(refundList.toString())
											.getJSONArray("Data")
											.getJSONObject(0)
											.getString("OrderStatus"));
						}
					}
					if (orderList.length() == 0)
						msg.what = 0;
					else
						msg.what = 1;
				} catch (JSONException e) {
					msg.what = 0;
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	// 修改订单状态
	public void updateState(final String orderNumber) {
		new Thread() {
			public void run() {
				StringBuffer stateList = httpget
						.getArray("/api/order/UpdatePaymentStatus/?OrderNumber="
								+ orderNumber + "&AppSign=" + HttpConn.AppSign);
				try {
					Message msg = Message.obtain();
					msg.obj = new JSONObject(stateList.toString()).getString("return");
					msg.what = 8;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
		//		method();
			}
		}.start();
	}

	private class MyAdapter2 extends BaseAdapter {

		@Override
		public int getCount() {

			return productArray.length();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			try {
				JSONArray productArray = new JSONArray(productList);
				view = View.inflate(getApplicationContext(),R.layout.activity_paymode_item, null);
				((TextView) view.findViewById(R.id.tv_product_name)).setText(productArray.getJSONObject(position)
								.getString("NAME"));
				String BuyPrice = productArray.getJSONObject(position).getString("BuyPrice");
				String format2 = df.format(Double.parseDouble(BuyPrice));
				if (Double.parseDouble(BuyPrice) < 1.00) {
					((TextView) view.findViewById(R.id.tv_price)).setText(0+ format2 + "元");
				} else {
					((TextView) view.findViewById(R.id.tv_price)).setText(format2 + "元");
				}
				((TextView) view.findViewById(R.id.tv_number)).setText(productArray.getJSONObject(position)
								.getString("BuyNumber"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (position == 0) {
				view.findViewById(R.id.line).setVisibility(View.GONE);
			}
			return view;
		}

	}

	// 判断是否设置过支付密码
	public void isSetPaymentPassword() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				StringBuffer result = httpget.getArray("/api/getpaypwd/?memLoginID="+ HttpConn.username + "&AppSign="+ HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					if (new JSONObject(result.toString()).getString("Data").equals("")) {
						msg.what = 4;
					} else {
						msg.what = 5;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}).start();
	}

}

package com.shopnum1.distributionportal;

import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.sourceforge.simcpux.Constants;
import net.sourceforge.simcpux.MD5;
import net.sourceforge.simcpux.Util;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
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
import com.alipay.sdk.pay.PayUtil;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
public class PayModeActivity extends Activity {
	private String Guid;
	private String Number;
	private Double ShouldPayPrice, ScorePrice;
	private HttpConn httpget = new HttpConn();
	private ArrayList<Map<String, String>> PaymentList;
	private Dialog pBar;
	private String payMode;
	private Button okbtn;
	private String paymentGuid;
	private JSONArray orderList;
	private String productList;
	private String format;
	private JSONArray array;
	private boolean isFirstPaymentPwd;
	private String paymentType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_paymode);
		init();
	}
	/**
	 * 获取支付方式
	 */
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
						if (array.getJSONObject(i).getString("NAME")
								.startsWith("支付宝支付")) {
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

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				pBar.dismiss();
			case 1:
				pBar.dismiss();
//				Intent intent2 = new Intent(new Intent(getApplicationContext(),
//						OrderActivity.class));
//				intent2.putExtra("type", 0);
//				intent2.putExtra("title", "全部订单");
//				startActivity(intent2);
				Intent intent1 = new Intent(getBaseContext(),
						OrderStatusActivity.class);
				intent1.putExtra("type", "0");
				startActivity(intent1);
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
				prePayment();
				break;
			case 5:
				isFirstPaymentPwd = false;
				prePayment();
				init();
				break;
			case 6:
				pBar.dismiss();
				Toast.makeText(getApplicationContext(), "余额不足",Toast.LENGTH_SHORT).show();
//				Intent intent1 = new Intent(new Intent(getApplicationContext(),OrderActivity.class));
//				intent1.putExtra("type", 0);
//				intent1.putExtra("title", "全部订单");
//				startActivity(intent1);
				Intent intent2 = new Intent(getBaseContext(),
						OrderStatusActivity.class);
				intent2.putExtra("type", "0");
				startActivity(intent2);
				finish();
				break;
			case 7:
				pBar.dismiss();
				if (msg.obj.equals("true")) {
					Toast.makeText(getApplicationContext(), "付款成功",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "付款失败",
							Toast.LENGTH_SHORT).show();
				}
//				Intent intent = new Intent(new Intent(getApplicationContext(),
//						OrderActivity.class));
//				intent.putExtra("type", 0);
//				intent.putExtra("title", "全部订单");
//				startActivity(intent);
				Intent intent = new Intent(getBaseContext(),
						OrderStatusActivity.class);
				intent.putExtra("type", "0");
				startActivity(intent);
				finish();
				break;
			case 8:
				if (msg.obj.equals("202")) {
					getData();
				} else {
					Toast.makeText(getApplicationContext(), "订单更新失败",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case 9:
				pBar.dismiss();
				Toast.makeText(getApplicationContext(), "密码错误",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}

	};

	private void init() {
		productList = getIntent().getStringExtra("ProductList");
		try {
			productArray = new JSONArray(productList);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ScorePrice = getIntent().getDoubleExtra("ScorePrice", 0.00);
		ShouldPayPrice = getIntent().getDoubleExtra("ShouldPayPrice", 0.00);
		df = new DecimalFormat("#.00");
		format = df.format((ShouldPayPrice - ScorePrice));
		if ((ShouldPayPrice - ScorePrice) < 1.00) {
			((TextView) findViewById(R.id.tv_total)).setText(0 + format + "元");
		} else {
			((TextView) findViewById(R.id.tv_total)).setText(format + "元");
		}
		Number = getIntent().getStringExtra("OrderNumber");
		paymentGuid = getIntent().getStringExtra("PaymentGuid");
		ListView lv2 = (ListView) findViewById(R.id.lv2);
		MyAdapter2 adapter2 = new MyAdapter2();
		lv2.setAdapter(adapter2);
		getPayment();
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
						intent.putExtra("total", ShouldPayPrice + "");
						startActivity(intent);
						finish();
					} else if ("PreDeposits.aspx".equals(paymentType)) {
						isSetPaymentPassword();
					} else if ("JDpay.aspx".equals(paymentType)) {
						Intent intent = new Intent(new Intent(getApplicationContext(),JingDongActivity.class));
						intent.putExtra("order", Number);
						intent.putExtra("total", ShouldPayPrice + "");
						startActivity(intent);
						finish();
					} else if ("Tenpay.aspx".equals(paymentType)) {
						Intent intent = new Intent(new Intent(getApplicationContext(),TenpayActivity.class));
						intent.putExtra("order_no", Number);
						intent.putExtra("order_price", ShouldPayPrice+ "");
						startActivity(intent);
						finish();
					} else if ("Weixin.aspx".equals(paymentType)) {
						Toast.makeText(getApplicationContext(), "请绑定商户ID", Toast.LENGTH_SHORT).show();
					} else if ("AlipaySDK.aspx".equals(paymentType)) {
						PayUtil PayUtil = new PayUtil(
								PayModeActivity.this, "分销门户", Number,
								(ShouldPayPrice - ScorePrice) + "",
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
	};
	private JSONArray productArray;
	private DecimalFormat df;
	private Dialog dialog;
	/**
	 * 选择支付方式
	 */
	public void paymentDialog() {

		if (PaymentList.size() > 0) {
			final ListView lv = (ListView) findViewById(R.id.lv);
			MyAdapter adapter = new MyAdapter();
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
							try {
								paymentType = array.getJSONObject(position)
										.getString("PaymentType");
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if ("Alipay.aspx".equals(paymentType)) {//支付宝网页支付
								Intent intent = new Intent(new Intent(
										getApplicationContext(),
										AlipayActivity.class));
								intent.putExtra("order", Number);
								intent.putExtra("source", "PayModeActivity");
								intent.putExtra("total", ShouldPayPrice + "");
								startActivity(intent);
								finish();
							} else if ("PreDeposits.aspx".equals(paymentType)) {
								isSetPaymentPassword();
							} else if ("JDpay.aspx".equals(paymentType)) {
								Intent intent = new Intent(new Intent(
										getApplicationContext(),
										JingDongActivity.class));
								intent.putExtra("order", Number);
								intent.putExtra("total", ShouldPayPrice + "");
								startActivity(intent);
								finish();
							} else if ("Tenpay.aspx".equals(paymentType)) {
								Intent intent = new Intent(new Intent(
										getApplicationContext(),
										TenpayActivity.class));
								intent.putExtra("order_no", Number);
								intent.putExtra("order_price", ShouldPayPrice
										+ "");
								startActivity(intent);
								finish();
							} else if ("Weixin.aspx".equals(paymentType)) {
								// 微信支付入口
								Toast.makeText(getApplicationContext(), "请绑定商户ID", Toast.LENGTH_SHORT).show();
							} else if ("AlipaySDK.aspx".equals(paymentType)) {
								PayUtil PayUtil = new PayUtil(
										PayModeActivity.this, "全球购", Number,
										(ShouldPayPrice - ScorePrice)+"",
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
	 * 预存款支付
	 */
	public void prePayment() {
		if (isFirstPaymentPwd) {
			startActivity(new Intent(getApplicationContext(),
					PayPasswordActivity.class));
			finish();
		} else {
			dialog = new Dialog(PayModeActivity.this, R.style.MyDialog);
			final View view = LayoutInflater.from(getBaseContext()).inflate(
					R.layout.dialog3, null);
			dialog.setContentView(view);
			dialog.show();
			pBar = new Dialog(this, R.style.dialog);
			pBar.setContentView(R.layout.progress);
			((Button) view.findViewById(R.id.no))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							pBar.dismiss();
							dialog.dismiss();
//							Intent intent = new Intent(new Intent(
//									getApplicationContext(),
//									OrderActivity.class));
//							intent.putExtra("type", 0);
//							intent.putExtra("title", "全部订单");
//							startActivity(intent);
							Intent intent = new Intent(getBaseContext(),
									OrderStatusActivity.class);
							intent.putExtra("type", "0");
							startActivity(intent);
							finish();
						}
					});
			((Button) view.findViewById(R.id.yes))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							dialog.dismiss();
							final String password = ((EditText) view
									.findViewById(R.id.edit)).getText()
									.toString();
							if(TextUtils.isEmpty(password)){
								Toast.makeText(PayModeActivity.this, "密码为空", 0).show();
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
											if (new JSONObject(result.toString())
											.getString("return").equals(
													"200")) {
												StringBuffer result2 = httpget
														.getArray("/api/accountget/?MemLoginID="
																+ HttpConn.UserName
																+ "&AppSign="
																+ HttpConn.AppSign);
												Double AdvancePayment = new JSONObject(
														result2.toString())
												.getJSONObject("AccoutInfo")
												.getDouble("AdvancePayment");
												if (AdvancePayment < ShouldPayPrice) {
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

	public void method() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				StringBuffer result2 = httpget
						.getArray("/api/UpdateOrderInfopayment?"
								+ "PaymentGuid=" + paymentGuid + "&Guid="
								+ Guid + "&PaymentName=" + payMode);
			}
		}).start();
	}

	public void getData() {
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		new Thread() {
			public void run() {
				String orderListUrl  = "/api/order/member/OrderList/?pageIndex=1&pageCount=100&memLoginID="
						+ HttpConn.username
						+ "&t="
						+ getIntent().getIntExtra("type", 0)
						+ "&AppSign=" + HttpConn.AppSign +"&agentID="+MyApplication.agentId;
				StringBuffer result = httpget
						.getArray(orderListUrl);
				StringBuffer refundList = httpget
						.getArray("/api/GetReturnOrderByMemLoginId?MemLoginID="
								+ HttpConn.username + "&AppSign="
								+ HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					orderList = new JSONObject(result.toString())
							.getJSONArray("Data");
					for (int i = 0; i < orderList.length(); i++) {
						Log.i("fly", refundList.toString());
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
					msg.obj = new JSONObject(stateList.toString())
							.getString("return");
					msg.what = 8;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				method();
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
				view = View.inflate(getApplicationContext(),
						R.layout.activity_paymode_item, null);
				((TextView) view.findViewById(R.id.tv_product_name))
						.setText(productArray.getJSONObject(position)
								.getString("Name"));
				String BuyPrice = productArray.getJSONObject(position)
						.getString("BuyPrice");
				double parseDouble = Double.parseDouble(BuyPrice);
				String format2 = df.format(parseDouble);
				if (parseDouble < 1.00) {
					((TextView) view.findViewById(R.id.tv_price)).setText(0
							+ format2 + "元");
				} else {
					((TextView) view.findViewById(R.id.tv_price))
							.setText(format2 + "元");
				}
				((TextView) view.findViewById(R.id.tv_number))
						.setText(productArray.getJSONObject(position)
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
	/**
	 * 判断是否设置过支付密码
	 */
	public void isSetPaymentPassword() {
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

	/************ TODO 微信支付相关 ************/
	StringBuffer sb;
	Map<String, String> resultunifiedorder;

	private class GetPrepayIdTask extends
			AsyncTask<Void, Void, Map<String, String>> {

		private ProgressDialog dialog;

		// @Override
		// protected void onPreExecute() {
		// dialog = ProgressDialog.show(PayModeActivity.this,
		// getString(R.string.app_tip), getString(R.string.getting_prepayid));
		// }

		@Override
		protected void onPostExecute(Map<String, String> result) {
			if (dialog != null) {
				dialog.dismiss();
			}
			sb.append("prepay_id\n" + result.get("prepay_id") + "\n\n");
			// show.setText(sb.toString());
			resultunifiedorder = result;
			Message.obtain(handler, 0x13).sendToTarget();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Map<String, String> doInBackground(Void... params) {

			String url = String
					.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
			String entity = genProductArgs();

			Log.e("orion", entity);

			byte[] buf = Util.httpPost(url, entity);

			String content = new String(buf);
			Log.e("orion", content);
			Map<String, String> xml = decodeXml(content);

			return xml;
		}
	}

	PayReq req;

	private void genPayReq() {

		req.appId = Constants.APP_ID;
		req.partnerId = Constants.MCH_ID;
		req.prepayId = resultunifiedorder.get("prepay_id");
		req.packageValue = "Sign=WXPay";
		req.nonceStr = genNonceStr();
		req.timeStamp = String.valueOf(genTimeStamp());

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

		req.sign = genAppSign(signParams);

		sb.append("sign\n" + req.sign + "\n\n");

		// show.setText(sb.toString());

		Log.e("orion", signParams.toString());

	}

	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

	final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);

	private void sendPayReq() {

		msgApi.registerApp(Constants.APP_ID);
		msgApi.sendReq(req);
	}

	private String genNonceStr() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}

	private String genAppSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Constants.API_KEY);

		this.sb.append("sign str\n" + sb.toString() + "\n\n");
		String appSign = MD5.getMessageDigest(sb.toString().getBytes())
				.toUpperCase();
		Log.e("orion", appSign);
		return appSign;
	}

	private String genProductArgs() {
		StringBuffer xml = new StringBuffer();

		try {
			String nonceStr = genNonceStr();
			xml.append("</xml>");
			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams
					.add(new BasicNameValuePair("appid", Constants.APP_ID));
			packageParams.add(new BasicNameValuePair("body", "weixin"));
			packageParams
					.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));
			packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
			// 回调地址
			packageParams.add(new BasicNameValuePair("notify_url",
					"http://121.40.35.3/test"));
			// 订单号
			// packageParams.add(new
			// BasicNameValuePair("out_trade_no",genOutTradNo()));
			packageParams.add(new BasicNameValuePair("out_trade_no", Number));
			packageParams.add(new BasicNameValuePair("spbill_create_ip",
					"127.0.0.1"));
			// packageParams.add(new BasicNameValuePair("total_fee", "1"));
			Log.e("money", ShouldPayPrice + "");
			packageParams.add(new BasicNameValuePair("total_fee",
					((int) (ShouldPayPrice * 100)) + ""));
			Log.e("money", (int) (ShouldPayPrice * 100) + "");
			packageParams.add(new BasicNameValuePair("trade_type", "APP"));

			String sign = genPackageSign(packageParams);
			packageParams.add(new BasicNameValuePair("sign", sign));

			String xmlstring = toXml(packageParams);

			return xmlstring;

		} catch (Exception e) {
			// Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
			return null;
		}
	}

	private String toXml(List<NameValuePair> params) {
		Log.e("toXml", "params" + params.iterator());
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		for (int i = 0; i < params.size(); i++) {
			sb.append("<" + params.get(i).getName() + ">");

			sb.append(params.get(i).getValue());
			sb.append("</" + params.get(i).getName() + ">");
		}
		sb.append("</xml>");

		Log.e("orion", sb.toString());
		return sb.toString();
	}

	private String genOutTradNo() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000))
				.getBytes());
	}

	/**
	 * 生成签名
	 */
	private String genPackageSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Constants.API_KEY);

		String packageSign = MD5.getMessageDigest(sb.toString().getBytes())
				.toUpperCase();
		Log.e("orion", packageSign);
		return packageSign;
	}

	public Map<String, String> decodeXml(String content) {

		try {
			Map<String, String> xml = new HashMap<String, String>();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(content));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {

				String nodeName = parser.getName();
				switch (event) {
				case XmlPullParser.START_DOCUMENT:

					break;
				case XmlPullParser.START_TAG:

					if ("xml".equals(nodeName) == false) {
						// 实例化student对象
						xml.put(nodeName, parser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event = parser.next();
			}

			return xml;
		} catch (Exception e) {
			Log.e("orion", e.toString());
		}
		return null;

	}
}

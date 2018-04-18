package com.shopnum1.distributionportal;

//订单详情
import java.text.DecimalFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
public class OrderInfo extends Activity {

	private HttpConn httpget = new HttpConn();
	private ListAdapter listAdapter;
	private String DispatchModeName;
	private JSONArray ProductList;
	private JSONArray GoodSList;
	private JSONObject refundList;
	private String OrderNumber, orderStatus, Guid, ReturnGoodsCause,ReturnGuid;
	private int OderStatus;
	private int Status = -1;
	private int PayType = -1;
	private Button btn1, btn2, btn3;
	JSONObject orderList;
	private String returnOrderStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_info);
		initLayout();
	}

	// 初始化
	public void initLayout() {
		btn1 = (Button) this.findViewById(R.id.btn1);
		btn2 = (Button) this.findViewById(R.id.btn2);
		btn3 = (Button) this.findViewById(R.id.btn3);
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
						httpget.showMenu(OrderInfo.this,
								findViewById(R.id.order_info));
					}
				});
		try {
			orderList = new JSONObject(getIntent().getStringExtra("orderList"));
			Guid = orderList.getString("Guid");
			PayType = orderList.getInt("PayType");
			OderStatus = orderList.getInt("OderStatus");
			int PaymentStatus = orderList.getInt("PaymentStatus");
			int ShipmentStatus = orderList.getInt("ShipmentStatus");
			returnOrderStatus = orderList.getString("ReturnOrderStatus");
//			if (OderStatus == 0) {// 未确定
//				orderStatus = "待付款";
//			} else if (OderStatus == 1) { // 已确认
//				if (PaymentStatus == 0) { // 未付款
//					orderStatus = "待付款";
//				} else if (PaymentStatus == 2) {// 已付款
//					if (ShipmentStatus == 0) {// 发货状态 未发货
//						if (returnOrderStatus != null
//								&& returnOrderStatus.equals("")) {
//							orderStatus = "待发货";
//						} else {
//							orderStatus = returnOrderStatus;
//						}
//					} else if (ShipmentStatus == 1) {// 已发货
//						// 退货状态
//						if (TextUtils.isEmpty(returnOrderStatus)) {
//							orderStatus = "待收货";
//						} else {
//							orderStatus = returnOrderStatus;
//						}
//					} else if (ShipmentStatus == 2) {// 已收货
//						orderStatus = "已收货";
//					} else if (ShipmentStatus == 3) {// 配货中
//						orderStatus = "配货中";
//					} else if (ShipmentStatus == 4) {// 退货
//						orderStatus = "已退货";
//						// 新加
//					} else if (ShipmentStatus == 5) {// 完成
//						orderStatus = "完成";
//					}
//				} else if (PaymentStatus == 3) {
//					if (TextUtils.isEmpty(returnOrderStatus)) {
//						orderStatus = "已退款";
//					} else {
//						orderStatus = returnOrderStatus;
//					}
//				}
//			} else if (OderStatus == 2) {// 已取消
//				if (PaymentStatus == 0) {// 未付款
//					orderStatus = "已取消";
//				} else if (PaymentStatus == 2) {// 已付款
//					if (ShipmentStatus == 0) {// 未发货
//						orderStatus = "退款审核中";
//					} else if (ShipmentStatus == 1) {// 已发货
//
//					}
//				} else if (!TextUtils.isEmpty(returnOrderStatus)) {
//					orderStatus = returnOrderStatus;
//				}
//			} else if (OderStatus == 5) {
//				if (PaymentStatus == 2) {
//					if (ShipmentStatus == 2) {
//						orderStatus = "交易成功";
//					} else if (ShipmentStatus == 4) {
//						orderStatus = "已退货";
//					}
//				}
//			}
			String ReturnOrderStatus = orderList.optString("ReturnOrderStatus");
			String StatusName=orderList.optString("StatusName");

			if (ReturnOrderStatus != null && !"".equals(ReturnOrderStatus)) {
				orderStatus = ReturnOrderStatus;
			}else{
				
				orderStatus=StatusName;
			}
			OrderNumber = orderList.getString("OrderNumber");
			ProductList = orderList.getJSONArray("ProductList");
			((TextView) findViewById(R.id.text1)).setText(orderList
					.getString("Name"));
			((TextView) findViewById(R.id.order_state)).setText("订单状态："
					+ orderStatus);
			((TextView) findViewById(R.id.order_time)).setText("订单时间："
					+ orderList.getString("CreateTime"));
			((TextView) findViewById(R.id.order_num)).setText(orderList
					.getString("OrderNumber"));
			((TextView) findViewById(R.id.fapiao_title)).setText("发票抬头："
					+ orderList.getString("InvoiceTitle"));
			((TextView) findViewById(R.id.fapiao_type)).setText("发票类型："
					+ orderList.getString("InvoiceType"));
			((TextView) findViewById(R.id.fapiao_comment)).setText("发票内容："
					+ orderList.getString("InvoiceContent"));
			((TextView) findViewById(R.id.text2)).setText(orderList
					.getString("Mobile"));
			((TextView) findViewById(R.id.text3)).setHint(orderList
					.getString("Address"));
			((TextView) findViewById(R.id.text4)).setText("￥"
					+ new DecimalFormat("0.00").format(orderList
							.getDouble("ProductPrice")));
			((TextView) findViewById(R.id.score)).setHint("￥-"
					+ new DecimalFormat("0.00").format(orderList
							.getDouble("ScorePrice")));
			realityPay = orderList.getDouble("ProductPrice")+ orderList.getDouble("DispatchPrice")- orderList.getDouble("ScorePrice");
			((TextView) findViewById(R.id.text7)).setText("￥"
					+ new DecimalFormat("0.00").format(orderList.getDouble("ProductPrice")
							+ orderList.getDouble("DispatchPrice")
							- orderList.getDouble("ScorePrice")));
			((TextView) findViewById(R.id.text13)).setHint(orderList
					.getString("ClientToSellerMsg"));
			if (orderList.getInt("ShipmentStatus") == 1
					|| orderList.getInt("ShipmentStatus") == 4) {
				getRefund();
			}

			ListView listview = (ListView) findViewById(R.id.listview);
			listAdapter = new ListAdapter(ProductList);
			listview.setAdapter(listAdapter);

			getDispatch(OrderNumber);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			showButton();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void getRefund() {
		new Thread() {
			public void run() {
				StringBuffer result = httpget.getArray("/api/getreturnorderlist/?MemLoginID="+ HttpConn.username + "&OrderGuid=" + Guid
								+ "&AppSign=" + HttpConn.AppSign);
				try {
					refundList = new JSONObject(result.toString())
							.getJSONObject("data");
					GoodSList = refundList.getJSONArray("GoodSList");
					Status = refundList.getInt("OrderStatus");
					ReturnGuid = refundList.getString("guid");
					ReturnGoodsCause = refundList.getString("ReturnGoodsCause");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Message msg = Message.obtain();
				msg.what = 2;
				handler.sendMessage(msg);
			}
		}.start();
	}

	// 获取配送方式
	public void getDispatch(final String OrderNumber) {
		new Thread() {
			@Override
			public void run() {
				StringBuffer result = httpget
						.getArray("/api/orderget/?OrderNumber=" + OrderNumber
								+ "&AppSign=" + HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					JSONObject object = (new JSONObject(result.toString())).getJSONObject("Orderinfo");
					DispatchModeName = object.getJSONObject("DispatchMode").getString("NAME");
					msg.what = 1;
				} catch (JSONException e) {
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 2:
				Button button = (Button) findViewById(R.id.button);
				button.setVisibility(View.VISIBLE);

				if (refundList == null) {
					button.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(getBaseContext(),RefundGoods.class);
							intent.putExtra("realityPay", realityPay);
							intent.putExtra("ProductList",ProductList.toString());
							intent.putExtra("OrderNumber", OrderNumber);
							intent.putExtra("Guid", Guid);
							intent.putExtra("orderList", orderList.toString());
							intent.putExtra("OderStatus", OderStatus);
							startActivityForResult(intent, 0);
						}
					});
				} else if (Status == 0) {
					button.setText("退货审核中");
				} else if (Status == 2) {
					button.setText("填写退货物流信息");
					button.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(getBaseContext(),
									RefundGoods2.class);
							intent.putExtra("ReturnGuid", ReturnGuid);
							intent.putExtra("ReturnGoodsCause",
									ReturnGoodsCause);
							startActivityForResult(intent, 0);
						}
					});
				} else if (Status == 3) {
					button.setText("退货中");
				} else if (Status == 4) {
					Double price = 0.00;
					for (int i = 0; i < GoodSList.length(); i++) {
						try {
							int ReturnCount = GoodSList.getJSONObject(i)
									.getInt("ReturnCount");
							Double BuyPrice = GoodSList.getJSONObject(i)
									.getDouble("BuyPrice");
							price += BuyPrice * ReturnCount;
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					button.setText("已退款￥"
							+ new DecimalFormat("0.00").format(price));
				}
				break;
			case 4:
				Intent i = new Intent(OrderInfo.this, OrderStatusActivity.class);
				i.putExtra("type", "0");
				startActivity(i);
				OrderInfo.this.finish();
				break;
			case 5:
				Intent i3 = new Intent(OrderInfo.this, OrderStatusActivity.class);
				i3.putExtra("type", "0");
				startActivity(i3);
				OrderInfo.this.finish();
				break;
			case 6:
				Intent i2 = new Intent(OrderInfo.this, OrderStatusActivity.class);
				i2.putExtra("type", "0");
				startActivity(i2);
				OrderInfo.this.finish();
				break;
			}
			super.handleMessage(msg);
		}

	};
	private Double realityPay;

	class ListAdapter extends BaseAdapter {

		JSONArray ProductList;

		public ListAdapter(JSONArray ProductList) {
			this.ProductList = ProductList;
		}

		@Override
		public int getCount() {

			return ProductList.length();
		}

		@Override
		public Object getItem(int arg0) {

			return arg0;
		}

		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			if (convertView == null) {
				convertView = LayoutInflater.from(OrderInfo.this).inflate(
						R.layout.order_item3, null);
			}

			try {
				((TextView) convertView.findViewById(R.id.text1))
						.setText(ProductList.getJSONObject(position).getString(
								"NAME"));
				((TextView) convertView.findViewById(R.id.text2))
						.setText(ProductList.getJSONObject(position).getString(
								"Attributes"));
				((TextView) convertView.findViewById(R.id.text3))
						.setText("￥"+ new DecimalFormat("0.00").format(ProductList.getJSONObject(position).getDouble("BuyPrice")));
				((TextView) convertView.findViewById(R.id.goods)).setText("x"+ ProductList.getJSONObject(position).getString("BuyNumber"));
				ImageView imageview = (ImageView) convertView.findViewById(R.id.imageView1);
				if (HttpConn.showImage)
					ImageLoader.getInstance().displayImage(ProductList.getJSONObject(position).getString(
									"OriginalImge"), imageview,
							MyApplication.options);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					try {
						Intent intent = new Intent(OrderInfo.this,
								ProductDetails.class);
						intent.putExtra("guid",ProductList.getJSONObject(position).getString("ProductGuid"));
						startActivity(intent);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});

			return convertView;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1) {
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showButton() throws JSONException {
		final int PayType = orderList.getInt("PayType");

		if (("待付款").equals(orderStatus)) {
			btn1.setVisibility(View.VISIBLE);
			btn1.setText("取消订单");
			btn2.setVisibility(View.VISIBLE);
			btn2.setText("立即支付");
			btn3.setVisibility(View.GONE);
			btn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					final Dialog dialog = new Dialog(OrderInfo.this,
							R.style.MyDialog);
					View view = LayoutInflater.from(OrderInfo.this).inflate(
							R.layout.dialog, null);
					((TextView) view.findViewById(R.id.dialog_text))
							.setText("是否取消订单？");
					dialog.setContentView(view);
					dialog.show();

					((Button) view.findViewById(R.id.no)).setText("取消");
					((Button) view.findViewById(R.id.no))
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									dialog.dismiss();
								}
							});

					((Button) view.findViewById(R.id.yes)).setText("确定");
					((Button) view.findViewById(R.id.yes))
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									try {
										orderCancel(orderList.getString("Guid"));
										dialog.dismiss();
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});
				}
			});

			btn2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					try {
						if (PayType == 1) {
							Intent intent = new Intent(OrderInfo.this,
									OrderAllToPay.class);
							intent.putExtra("ShouldPayPrice",
									orderList.getDouble("ShouldPayPrice"));
							intent.putExtra("ProductList",
									orderList.getString("ProductList"));
							intent.putExtra("OrderNumber",
									orderList.getString("OrderNumber"));
							intent.putExtra("PaymentGuid",
									orderList.getString("PaymentGuid"));
							intent.putExtra("ScorePrice",
									orderList.getDouble("ScorePrice"));
							startActivity(intent);
						} else if (PayType == 2) {
							Toast.makeText(OrderInfo.this, "请尽快联系商家付款！",
									Toast.LENGTH_SHORT).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		} else if (("配货中").equals(orderStatus)) {
			btn1.setVisibility(View.INVISIBLE);
			btn2.setVisibility(View.VISIBLE);
			btn3.setVisibility(View.GONE);
			btn2.setText("申请退款");

			btn2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent i = new Intent(OrderInfo.this, RefoundActivity.class);
					i.putExtra("orderObject", orderList.toString());
					startActivity(i);
				}
			});
		} else if (("待发货").equals(orderStatus)) {
			btn1.setVisibility(View.INVISIBLE);
			btn2.setVisibility(View.VISIBLE);
			btn3.setVisibility(View.GONE);
			btn2.setText("申请退款");
			btn2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(getApplicationContext(),
							RefoundActivity.class);
					intent.putExtra("orderObject", orderList.toString());
					startActivity(intent);
				}
			});
		} else if (("待收货").equals(orderStatus)) {
			btn1.setVisibility(View.VISIBLE);
			btn1.setText("查看物流");
			btn2.setVisibility(View.VISIBLE);
			btn2.setText("确认收货");
			btn3.setVisibility(View.GONE);
			btn1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					try {
						Intent intent = new Intent(OrderInfo.this,
								KuaidiActivity.class);
						intent.putExtra("code",
								orderList.getString("LogisticsCompanyCode"));
						intent.putExtra("id",
								orderList.getString("ShipmentNumber"));
						startActivity(intent);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			btn2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					final Dialog dialog = new Dialog(OrderInfo.this,
							R.style.MyDialog);
					View view = LayoutInflater.from(OrderInfo.this).inflate(
							R.layout.dialog, null);
					((TextView) view.findViewById(R.id.dialog_text))
							.setText("是否确认收货");
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
									try {
										updateStatus(orderList
												.getString("Guid"));
										dialog.dismiss();
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});
				}
			});
		} else if (("已收货").equals(orderStatus)) {
			btn1.setVisibility(View.GONE);
			btn2.setVisibility(View.VISIBLE);
			btn2.setText("申请退货");
			btn2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(OrderInfo.this, "请联系客服工作人员退货！",
							Toast.LENGTH_LONG).show();
				}
			});
		} else if (("已取消").equals(orderStatus)) {
			btn1.setVisibility(View.GONE);
			btn2.setVisibility(View.GONE);
			btn3.setVisibility(View.VISIBLE);
		} else if (("已退货").equals(orderStatus)) {
			btn1.setVisibility(View.GONE);
			btn2.setVisibility(View.GONE);
			btn3.setVisibility(View.VISIBLE);
		} else if (("已退款").equals(orderStatus)) {
			btn1.setVisibility(View.GONE);
			btn2.setVisibility(View.GONE);
			btn3.setVisibility(View.VISIBLE);
		} else if (("交易成功").equals(orderStatus)) {
			btn1.setVisibility(View.GONE);
			btn2.setVisibility(View.GONE);
			btn3.setVisibility(View.VISIBLE);
		} else if (("退款审核中").equals(OrderNumber)) {
			btn1.setVisibility(View.GONE);
			btn2.setVisibility(View.GONE);
			btn3.setVisibility(View.GONE);
		} else if (("退货审核中").equals(OrderNumber)) {
			btn1.setVisibility(View.GONE);
			btn2.setVisibility(View.GONE);
			btn3.setVisibility(View.GONE);
		}
		btn3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					orderDelete(orderList.getString("OrderNumber"));
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		});

	}

	/**
	 * 删除订单
	 * 
	 * @param guid
	 */
	public void orderDelete(final String orderNumber) {
		new Thread() {
			public void run() {
				try {
					StringBuffer result = httpget
							.getArray("/api/DeleteOrder/?AppSign="
									+ HttpConn.AppSign + "&orderNumber="
									+ orderNumber + "&memLoginID="
									+ HttpConn.username);
					Message msg = Message.obtain();
					msg.obj = new JSONObject(result.toString())
							.getString("Data");
					msg.what = 6;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 更新订单状态
	 * 
	 * @param guid
	 */
	public void updateStatus(final String guid) {
		new Thread() {
			public void run() {
				StringBuffer result = httpget
						.getArray("/api/order/UpdateShipmentStatus/?id=" + guid
								+ "&AppSign=" + HttpConn.AppSign);
				Log.i("fly", result.toString());
				try {
					Message msg = Message.obtain();
					msg.obj = new JSONObject(result.toString())
							.getString("return");
					msg.what = 5;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 取消订单
	 * 
	 * @param guid
	 */
	public void orderCancel(final String guid) {
		new Thread() {
			public void run() {
				try {
					StringBuffer result = httpget
							.getArray("/api/ordercancel/?id=" + guid
									+ "&AppSign=" + HttpConn.AppSign);
					Message msg = Message.obtain();
					msg.obj = new JSONObject(result.toString())
							.getString("return");
					msg.what = 4;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 申请退款
	 * 
	 * @param guid
	 */
	public void orderReturn(final String guid, final String orderNumber,
			final Double alreadPayPrice) {
		new Thread() {
			public void run() {
				try {
					JSONObject object = new JSONObject();
					object.put("guid", guid);
					object.put("orderNumber", orderNumber);
					object.put("alreadPayPrice", alreadPayPrice);
					object.put("memLoginId", HttpConn.username);
					object.put("AppSign", HttpConn.AppSign);
					StringBuffer result = httpget.postJSON("/api/orderquxiao/",
							object.toString());
					Message msg = Message.obtain();
					msg.obj = new JSONObject(result.toString())
							.getString("return");
					msg.what = 7;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

}
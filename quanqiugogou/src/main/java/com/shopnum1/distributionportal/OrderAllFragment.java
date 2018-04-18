package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.NoScrollListView;
import com.shopnum1.distributionportal.util.PullToRefreshView;
import com.shopnum1.distributionportal.util.PullToRefreshView.OnHeaderRefreshListener;

import java.text.DecimalFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

@SuppressLint("ValidFragment")
public class OrderAllFragment extends Fragment implements OnHeaderRefreshListener {
	private HttpConn httpget = new HttpConn();
	private JSONArray orderList, paymentList;
	private ListAdapter listAdapter;
	private Dialog pBar, dialog; // 加载进度
	private int type;
	private String PaymentGuid = "00000000-0000-0000-0000-000000000000";
	private String returnOrderStatus;
	private View v;
	private int index;
	private DisplayImageOptions options;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.order_all, null);
		initLayout();
		isFromPullDown = false;
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.notype) // 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.notype) // 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.notype) // 设置图片加载或解码过程中发生错误显示的图片
				.cacheInMemory(true) // default 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true) // default 设置下载的图片是否缓存在SD卡中
				.imageScaleType(ImageScaleType.EXACTLY) // default
				.bitmapConfig(Bitmap.Config.ARGB_8888) // default 设置图片的解码类型
				.handler(new Handler()) // default
				.build();
		return v;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			// 只有当该Fragment被用户可见的时候,才加载网络数据
			getData();
		}
	}

	public OrderAllFragment(int index) {
		this.index = index;
	}

	// 初始化
	public void initLayout() {
		listview = (ListView) v.findViewById(R.id.listview);
		mPullToRefreshView = (PullToRefreshView) v.findViewById(R.id.main_pull_refresh_view);
		mPullToRefreshView.setEnablePullLoadMoreDataStatus(false);
		mPullToRefreshView.setOnHeaderRefreshListener(this);
	}

	private boolean isFromPullDown;

	public void getData() {
		if (!isFromPullDown) {
			pBar = new Dialog(getActivity(), R.style.dialog);
			pBar.setContentView(R.layout.progress);
			pBar.show();
		}
		new Thread() {
			public void run() {
//				

				String orderListUrl ="/api/order/member/OrderList/?pageIndex=1&pageCount=100&memLoginID="+ HttpConn.username+ "&t="
						+ index
						+ "&AppSign=" + HttpConn.AppSign+"&agentID="+MyApplication.agentId;
				StringBuffer result = httpget.getArray(orderListUrl);
				StringBuffer payment = httpget.getArray("/api/payment/"
						+ "?AppSign=" + HttpConn.AppSign);
				Message msg = Message.obtain();
				try {
					orderList = new JSONObject(result.toString()).getJSONArray("Data");
					paymentList = new JSONObject(payment.toString()).getJSONArray("data");

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

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (!isFromPullDown) {
					pBar.dismiss();
				}
				((TextView) v.findViewById(R.id.nocontent))
						.setVisibility(View.VISIBLE);
				break;
			case 1:
				if (!isFromPullDown) {
					pBar.dismiss();
				}
				listAdapter = new ListAdapter();
				listview.setAdapter(listAdapter);
				break;
			case 2:
				pBar.dismiss();
				Toast.makeText(getActivity().getApplicationContext(), "余额不足",
						Toast.LENGTH_SHORT).show();
				break;
			case 3:
				pBar.dismiss();
				if (msg.obj.equals("true")) {
					Toast.makeText(getActivity().getApplicationContext(),"付款成功", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(new Intent(getActivity().getApplicationContext(), OrderActivity.class));
					intent.putExtra("type", 0);
					intent.putExtra("title", "全部订单");
					startActivity(intent);
					getActivity().finish();
				} else {
					Toast.makeText(getActivity().getApplicationContext(),"付款失败", Toast.LENGTH_SHORT).show();
				}
				break;
			case 4:
				if (msg.obj.equals("202")) {
					listview.setAdapter(null);
					isFromPullDown = false;
					getData();
				} else {
					Toast.makeText(getActivity().getApplicationContext(),"取消失败", Toast.LENGTH_SHORT).show();
				}
				break;
			case 5:
				if (msg.obj.equals("202")) {
					listview.setAdapter(null);
					isFromPullDown = false;
					getData();
					
				} else {
					Toast.makeText(getActivity().getApplicationContext(),"确认失败", Toast.LENGTH_SHORT).show();
				}
				break;
			case 6:
				getData();
				break;
			case 7:
				if (msg.obj.equals("202")) {
					listview.setAdapter(null);
					isFromPullDown = false;
					getData();
				} else {
					Toast.makeText(getActivity().getApplicationContext(),"申请失败", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			super.handleMessage(msg);
		}

	};
	private PullToRefreshView mPullToRefreshView;
	private ListView listview;

	public void toPay(String PaymentGuid, double ShouldPayPrice,
			String OrderNumber) {
		try {
			for (int i = 0; i < paymentList.length(); i++) {
				String guid = paymentList.getJSONObject(i).getString("Guid");
				String name = paymentList.getJSONObject(i).getString("NAME");
				if (name.startsWith("预存款")) {
					prePayment(ShouldPayPrice, OrderNumber);
				} else if (name.startsWith("支付宝手机网站支付")) {
					Intent intent = new Intent(new Intent(getActivity().getApplicationContext(), AlipayActivity.class));
					intent.putExtra("order", OrderNumber);
					intent.putExtra("source", "PayModeActivity");
					intent.putExtra("total", ShouldPayPrice + "");
					startActivity(intent);
					((Activity) getActivity()).finish();
				} else if ("00000000-0000-0000-0000-000000000000".equals(PaymentGuid)) {
					prePayment(ShouldPayPrice, OrderNumber);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void prePayment(final double ShouldPayPrice, final String OrderNumber) {
		if (dialog == null) {
			dialog = new Dialog(getActivity(), R.style.MyDialog);
		}
		final View view = LayoutInflater.from(
				getActivity().getApplicationContext()).inflate(
				R.layout.dialog3, null);
		dialog.setContentView(view);
		if (!dialog.isShowing()) {
			dialog.show();
		}
		pBar = new Dialog(getActivity(), R.style.dialog);
		pBar.setContentView(R.layout.progress);

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
						dialog.dismiss();
						String pwd = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("pwd", "");
						final String password = ((EditText) view.findViewById(R.id.edit)).getText().toString();
						if (password.equals(pwd)) {
							pBar.show();
							new Thread() {
								public void run() {
									Message msg = Message.obtain();
									try {
										StringBuffer result = httpget
												.getArray("/api/accountget/?MemLoginID="
														+ HttpConn.username
														+ "&AppSign="
														+ HttpConn.AppSign);
										Double AdvancePayment = new JSONObject(
												result.toString())
												.getJSONObject("AccoutInfo")
												.getDouble("AdvancePayment");
										if (AdvancePayment < ShouldPayPrice) {
											msg.what = 2;
										} else {
											StringBuffer payresult = httpget.getArray("/api/order/BuyAdvancePayment/MemLoginID="
															+ HttpConn.username
															+ "?OrderNumber="
															+ OrderNumber
															+ "&PayPwd="
															+ password
															+ "&AppSign="
															+ HttpConn.AppSign);
											msg.obj = new JSONObject(payresult
													.toString())
													.getString("sbool");
											msg.what = 3;
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
									handler.sendMessage(msg);
								}
							}.start();
						} else {
							pBar.dismiss();
							Toast.makeText(getActivity().getApplicationContext(),
									"密码错误", Toast.LENGTH_SHORT).show();
						}
					}
				});
	}
	/**
	 * 取消订单
	 * @param guid
	 */
	public void orderCancel(final String guid) {
		new Thread() {
			public void run() {
				try {
					StringBuffer result = httpget.getArray("/api/ordercancel/?id=" + guid+ "&AppSign=" + HttpConn.AppSign);
					Message msg = Message.obtain();
					msg.obj = new JSONObject(result.toString()).getString("return");
					msg.what = 4;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 删除订单
	 * @param guid
	 */
	public void orderDelete(final String orderNumber) {
		new Thread() {
			public void run() {
				try {
					StringBuffer result = httpget.getArray("/api/DeleteOrder/?AppSign="+ HttpConn.AppSign + "&orderNumber="+ orderNumber + "&memLoginID="+ HttpConn.username);
					Message msg = Message.obtain();
					msg.obj = new JSONObject(result.toString()).getString("Data");
					msg.what = 6;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 申请退款
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

	/**
	 * 更新订单状态
	 * @param guid
	 */
	public void updateStatus(final String guid) {
		new Thread() {
			public void run() {
				StringBuffer result = httpget.getArray("/api/order/UpdateShipmentStatus/?id=" + guid + "&AppSign=" + HttpConn.AppSign);
				try {
					Message msg = Message.obtain();
					msg.obj = new JSONObject(result.toString()).getString("return");
					msg.what = 5;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	class ListAdapter extends BaseAdapter {

		public JSONObject getInfo(int position) {
			JSONObject result = null;
			try {
				result = orderList.getJSONObject(position);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		public int getCount() {

			return orderList.length();
		}

		@Override
		public Object getItem(int arg0) {

			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.order_item, null);
				holder = new ViewHolder();
				holder.status = (TextView) convertView.findViewById(R.id.status);
				holder.price = (TextView) convertView.findViewById(R.id.price);
				holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);
				holder.listview2 = (NoScrollListView) convertView.findViewById(R.id.listview2);
				holder.btn1 = (Button) convertView.findViewById(R.id.btn1);
				holder.btn2 = (Button) convertView.findViewById(R.id.btn2);
				holder.btn3 = (Button) convertView.findViewById(R.id.btn3);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				resetViewHolder(holder);
			}
			try {
				final JSONObject orderObject = orderList.getJSONObject(position);
				String OrderStatus = "";
				int OderStatus = orderObject.getInt("OderStatus");// 订单状态
				int ShipmentStatus = orderObject.getInt("ShipmentStatus");// 发货状态
				int PaymentStatus = orderObject.getInt("PaymentStatus");// 支付状态
				returnOrderStatus = orderObject.getString("ReturnOrderStatus");
				final int PayType = orderObject.getInt("PayType");
				if (OderStatus == 0) {// 未确定
					OrderStatus = "待付款";
				} else if (OderStatus == 1) { // 已确认
					if (PaymentStatus == 0) { // 未付款
						OrderStatus = "待付款";
					} else if (PaymentStatus == 2) {// 已付款
						if (ShipmentStatus == 0) {// 发货状态 未发货
							if (returnOrderStatus != null
									&& returnOrderStatus.equals("")) {
								OrderStatus = "待发货";
							} else {
								OrderStatus = returnOrderStatus;
							}
						} else if (ShipmentStatus == 1) {// 已发货
							// 退货状态
							if (TextUtils.isEmpty(returnOrderStatus)) {
								OrderStatus = "待收货";
							} else {
								OrderStatus = returnOrderStatus;
							}
						} else if (ShipmentStatus == 2) {// 已收货
							OrderStatus = "已收货";
						} else if (ShipmentStatus == 3) {// 配货中
							OrderStatus = "配货中";
						} else if (ShipmentStatus == 4) {// 退货
							OrderStatus = "已退货";
							// 新加
						} else if (ShipmentStatus == 5) {// 完成
							OrderStatus = "完成";
						}
					} else if (PaymentStatus == 3) {
						if (TextUtils.isEmpty(returnOrderStatus)) {
							OrderStatus = "已退款";
						} else {
							OrderStatus = returnOrderStatus;
						}
					}
				} else if (OderStatus == 2) {// 已取消
					if (PaymentStatus == 0) {// 未付款
						OrderStatus = "已取消";
					} else if (PaymentStatus == 2) {// 已付款
						if (ShipmentStatus == 0) {// 未发货
							OrderStatus = "退款审核中";
						} else if (ShipmentStatus == 1) {// 已发货

						}
					} else if (!TextUtils.isEmpty(returnOrderStatus)) {
						OrderStatus = returnOrderStatus;
					}
				} else if (OderStatus == 5) {
					if (PaymentStatus == 2) {
						if (ShipmentStatus == 2) {
							OrderStatus = "交易成功";
						} else if (ShipmentStatus == 4) {
							OrderStatus = "已退货";
						}
					}
				}
				holder.status.setText("订单状态：" + OrderStatus);
				double BuyPrice = orderObject.getDouble("ProductPrice")+ orderObject.getDouble("DispatchPrice") - orderObject.getDouble("ScorePrice");

				holder.price.setText("￥"+ new DecimalFormat("0.00").format(BuyPrice));
				if (holder.listview2.getAdapter() == null) {
					holder.listview2.setAdapter(new ListAdapter2(orderObject.getJSONArray("ProductList")));
				} else {
					((BaseAdapter) holder.listview2.getAdapter()).notifyDataSetChanged();
				}
				holder.listview2.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0,View arg1, int arg2, long arg3) {
						try {
							Intent intent = new Intent(getActivity().getApplicationContext(), OrderInfo.class);
							intent.putExtra("orderList", orderList
									.getJSONObject(position).toString());
							startActivity(intent);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
				holder.arrow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						try {
							Intent intent = new Intent(getActivity().getApplicationContext(), OrderInfo.class);
							intent.putExtra("orderList", orderList.getJSONObject(position).toString());
							startActivityForResult(intent, 1);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
				if (("待付款").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.VISIBLE);
					holder.btn1.setText("取消订单");
					holder.btn2.setVisibility(View.VISIBLE);
					holder.btn2.setText("立即支付");
					holder.btn3.setVisibility(View.GONE);

					holder.btn1.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							final Dialog dialog = new Dialog(getActivity(),
									R.style.MyDialog);
							View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.dialog, null);
							((TextView) view.findViewById(R.id.dialog_text)).setText("是否取消订单？");
							dialog.setContentView(view);
							dialog.show();

							((Button) view.findViewById(R.id.no)).setText("取消");
							((Button) view.findViewById(R.id.no)).setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View arg0) {
									dialog.dismiss();
								}
							});

							((Button) view.findViewById(R.id.yes)).setText("确定");
							((Button) view.findViewById(R.id.yes)).setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View arg0) {
									try {
										orderCancel(orderObject.getString("Guid"));
										dialog.dismiss();
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});
						}
					});

					holder.btn2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							try {
								if (PayType == 1) {
									Intent intent = new Intent(getActivity().getApplicationContext(),OrderAllToPay.class);
									intent.putExtra("ProductPrice",orderObject.getDouble("ProductPrice"));
									intent.putExtra("ProductList", orderObject.getString("ProductList"));
									intent.putExtra("OrderNumber", orderObject.getString("OrderNumber"));
									intent.putExtra("PaymentGuid", orderObject.getString("PaymentGuid"));
									intent.putExtra("ScorePrice",orderObject.getDouble("ScorePrice"));
									intent.putExtra("DispatchPrice", orderObject.getDouble("DispatchPrice"));
									startActivity(intent);
								} else if (PayType == 2) {
									Toast.makeText(getActivity().getApplicationContext(),"请尽快联系商家付款！", Toast.LENGTH_SHORT).show();
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				} else if (("配货中").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.INVISIBLE);
					holder.btn2.setVisibility(View.VISIBLE);
					holder.btn3.setVisibility(View.GONE);
					holder.btn2.setText("申请退款");
					holder.btn2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent i = new Intent(OrderAllFragment.this.getActivity(), RefoundActivity.class);
							try {
								i.putExtra("orderObject", orderList.getJSONObject(position).toString());
							} catch (JSONException e) {
								e.printStackTrace();
							}
							startActivity(i);
						}
					});
				} else if (("待发货").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.INVISIBLE);
					holder.btn2.setVisibility(View.VISIBLE);
					holder.btn3.setVisibility(View.GONE);
					holder.btn2.setText("申请退款");
					holder.btn2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Log.i("test", orderList.toString() + "==");
							Intent i = new Intent(OrderAllFragment.this.getActivity(), RefoundActivity.class);
							try {
								i.putExtra("orderObject", orderList.getJSONObject(position).toString());
							} catch (JSONException e) {
								e.printStackTrace();
							}
							startActivity(i);

						}
					});
				} else if (("待收货").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.VISIBLE);
					holder.btn1.setText("查看物流");
					holder.btn2.setVisibility(View.VISIBLE);
					holder.btn2.setText("确认收货");
					holder.btn3.setVisibility(View.GONE);
					holder.btn1.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							try {
								Intent intent = new Intent(getActivity().getApplicationContext(), KuaidiActivity.class);
								intent.putExtra("code", orderObject.getString("LogisticsCompanyCode"));
								intent.putExtra("id", orderObject.getString("ShipmentNumber"));
								startActivity(intent);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});

					holder.btn2.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							final Dialog dialog = new Dialog(getActivity(),R.style.MyDialog);
							View view = LayoutInflater.from(getActivity().getApplicationContext())
									.inflate(R.layout.dialog, null);
							((TextView) view.findViewById(R.id.dialog_text))
									.setText("是否确认收货");
							dialog.setContentView(view);
							dialog.show();

							((Button) view.findViewById(R.id.no)).setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View arg0) {
									dialog.dismiss();
								}
							});

							((Button) view.findViewById(R.id.yes)).setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View arg0) {
									try {
										updateStatus(orderObject.getString("Guid"));
										dialog.dismiss();
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});
						}
					});
				} else if (("已收货").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.GONE);
					holder.btn2.setVisibility(View.VISIBLE);
					holder.btn2.setText("申请退货");
					holder.btn2.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Toast.makeText(
									getActivity().getApplicationContext(),
									"请联系客服工作人员退货！", Toast.LENGTH_LONG).show();
						}
					});
				} else if (("已取消").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.GONE);
					holder.btn2.setVisibility(View.GONE);
					holder.btn3.setVisibility(View.VISIBLE);
				} else if (("已退货").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.GONE);
					holder.btn2.setVisibility(View.GONE);
					holder.btn3.setVisibility(View.VISIBLE);
				} else if (("已退款").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.GONE);
					holder.btn2.setVisibility(View.GONE);
					holder.btn3.setVisibility(View.VISIBLE);
				} else if (("交易成功").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.GONE);
					holder.btn2.setVisibility(View.GONE);
					holder.btn3.setVisibility(View.VISIBLE);
				} else if (("退款审核中").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.GONE);
					holder.btn2.setVisibility(View.GONE);
					holder.btn3.setVisibility(View.GONE);
				}else if (("退货审核中").equals(OrderStatus)) {
					holder.btn1.setVisibility(View.GONE);
					holder.btn2.setVisibility(View.GONE);
					holder.btn3.setVisibility(View.GONE);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
			holder.btn3.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						orderDelete(orderList.getJSONObject(position).getString("OrderNumber"));
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
			});
			return convertView;
		}

	}

	static class ViewHolder {
		 Button btn3;
		TextView status;
		TextView price;
		NoScrollListView listview2;
		Button btn1;
		Button btn2;
		ImageView arrow;
	}

	protected void resetViewHolder(ViewHolder p_ViewHolder) {
		p_ViewHolder.status.setText(null);
		p_ViewHolder.price.setText(null);
		p_ViewHolder.listview2.setAdapter(null);
		p_ViewHolder.arrow.setImageDrawable(null);
	}

	class ListAdapter2 extends BaseAdapter {

		JSONArray ProductList;

		public ListAdapter2(JSONArray ProductList) {
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
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder2 vh;
			if (convertView == null) {
				vh = new ViewHolder2();
				convertView = View.inflate(getActivity(), R.layout.order_item2,
						null);
				vh.name = (TextView) convertView.findViewById(R.id.name);
				vh.spec = (TextView) convertView.findViewById(R.id.spec);
				vh.num = (TextView) convertView.findViewById(R.id.num);
				vh.price = (TextView) convertView.findViewById(R.id.price);
				vh.imageview = (ImageView) convertView
						.findViewById(R.id.imageView1);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder2) convertView.getTag();
			}

			try {

				vh.name.setText(ProductList.getJSONObject(position).getString("NAME"));
				vh.price.setText("￥"+ ProductList.getJSONObject(position).getString(
								"BuyPrice"));
				vh.num.setText("x"
						+ ProductList.getJSONObject(position).getString(
								"BuyNumber"));
				vh.spec.setText(ProductList.getJSONObject(position).getString(
						"Attributes"));

				if (HttpConn.showImage)
					ImageLoader.getInstance().displayImage(
							ProductList.getJSONObject(position).getString(
									"OriginalImge"), vh.imageview, options);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return convertView;
		}

	}

	static class ViewHolder2 {
		TextView name;
		TextView spec;
		TextView num;
		TextView price;
		ImageView imageview;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 通知adapter刷新
		if (null != listAdapter) {
			listAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		// 通知adapter刷新
		if (null != listAdapter) {
			listAdapter.notifyDataSetChanged();
		}
		isFromPullDown = true;
		getData();

		mPullToRefreshView.postDelayed(new Runnable() {

			@Override
			public void run() {
				// 设置更新时间
				mPullToRefreshView.onHeaderRefreshComplete();
			}
		}, 1000);

	}
}
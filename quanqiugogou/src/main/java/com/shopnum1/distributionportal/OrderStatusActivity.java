package com.shopnum1.distributionportal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.LoggerUtils;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.ThreadPoolTools;
public class OrderStatusActivity extends Activity implements OnRefreshListener2<ExpandableListView>{
	/**
	 * type订单状态
	 * 0 -- 全部订单
	 * 1 -- 待付款
	 * 2 -- 待发货
	 * 3 -- 待收货
	 * 4 -- 待评价
	 */
	private String type = "";
	/**
	 * 用来存放所有的jsonArr数据
	 * 通过页码对应数据
	 */
	private Map<Integer, JSONArray> dataMap = new HashMap<Integer, JSONArray>();
	private Dialog pBar;
	private ExpandableListView expandView;
	private int page = 1;//而码
	private final int COUNT = 20;//每次加载数据条数
	private PullToRefreshExpandableListView mExpandableListView;
	private ExecutorService executor;
	private Handler mHandler = new Handler();
	private MyExpandableAdapter mAdapter;
	private boolean hasData = true;
	private DisplayImageOptions options;
	private List<OrderInfos> orderList;
	private List<ProductInfo> productList;
//	private JSONArray array = new JSONArray();
	private RadioGroup rb_group;
	private TextView title;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_all_ui);
		options = new DisplayImageOptions.Builder()  
        // 设置图片在下载期间显示的图片  
                .showImageOnLoading(R.drawable.pic1)  
                // 设置图片Uri为空或是错误的时候显示的图片  
                .showImageForEmptyUri(R.drawable.pic1)  
                // 设置图片加载/解码过程中错误时候显示的图片  
                .showImageOnFail(R.drawable.pic1)  
                // 设置下载的图片是否缓存在内存中  
                .cacheInMemory(true)  
                // 设置下载的图片是否缓存在SD卡中  
                .cacheOnDisc(true)  
                // 保留Exif信息  
                .considerExifParams(true)  
                // .decodingOptions(android.graphics.BitmapFactory.Options  
                // decodingOptions)//设置图片的解码配置  
                .considerExifParams(true)  
                // 设置图片下载前的延迟  
                .delayBeforeLoading(100)// int  
                // delayInMillis为你设置的延迟时间  
                // 设置图片加入缓存前，对bitmap进行设置  
                // .preProcessor(BitmapProcessor preProcessor)  
                .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位  
                // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少  
                .build(); 
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.setCanceledOnTouchOutside(false);
		type = getIntent().getExtras().getString("type");
		initView();
//		if("0".equals(type))
//			getOrderData();
	}
	
	private void initView() {
		title = (TextView) findViewById(R.id.tv_title);
		((ImageView)findViewById(R.id.order_back)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		// 获取线程池对象，则于这里只会请求一次列表数据，所以使用缓存型的线程池，以便数据获取失败后再次重新获取，不需要创建新的线程，复用当前线程即可
		executor = ThreadPoolTools.getInstance().getCachedThreadPool();
		mExpandableListView = (PullToRefreshExpandableListView) findViewById(R.id.order_expand);
		mExpandableListView.setMode(Mode.BOTH);//设置属性为上拉与下拉
		mExpandableListView.setOnRefreshListener(this);//设置上拉与下拉监听
		// 初始化选项卡
		rb_group = (RadioGroup) findViewById(R.id.rb_group);
		rb_group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				type = checkedId == R.id.rb_quanbu ? "0" : checkedId == R.id.rb_fukuan ? "1" : checkedId == R.id.rb_fahuo ? "2" : checkedId == R.id.rb_shouhuo ? "3" : checkedId == R.id.rb_pingjia ? "4" : "";
				title.setText(checkedId == R.id.rb_quanbu ? "全部订单" : checkedId == R.id.rb_fukuan ? "待付款" : checkedId == R.id.rb_fahuo ? "待发货" : checkedId == R.id.rb_shouhuo ? "待收货" : checkedId == R.id.rb_pingjia ? "待评价" : "商品订单");
				LoggerUtils.getLog(OrderStatusActivity.this.getClass()).error("调用--------");
				reStartGetData();// 获取数据操作
			}
		});
		((RadioButton)findViewById("0".equals(type) ? R.id.rb_quanbu : "1".equals(type) ? R.id.rb_fukuan : "2".equals(type) ? R.id.rb_fahuo : "3".equals(type) ? R.id.rb_shouhuo : R.id.rb_pingjia)).setChecked(true);
//		rb_group.check("0".equals(type) ? R.id.rb_quanbu : "1".equals(type) ? R.id.rb_fukuan : "2".equals(type) ? R.id.rb_fahuo : "3".equals(type) ? R.id.rb_shouhuo : R.id.rb_pingjia);
	}

	private void getOrderData() {
		if (pBar != null && !pBar.isShowing()) {
			pBar.show();
		}
		executor.execute(new Runnable() {
			@Override
			public void run() {
				String orderListUrl  = "/api/order/member/OrderList/?pageIndex=" + page + "&pageCount=" + COUNT + "&memLoginID="
						+ HttpConn.username+ "&t=" + type + "&AppSign=" + HttpConn.AppSign +"&agentID="+MyApplication.agentId;
				final String result = (new HttpConn().getData(orderListUrl)).toString();
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						if (mExpandableListView != null) {
							if (mExpandableListView.isRefreshing()) {
								mExpandableListView.onRefreshComplete();
							}
						}
						if (result == null || "".equals(result)) {
							// 当数据没有获取到的时候重试请求
							getOrderData();
							return;
						}
						if (pBar != null && pBar.isShowing()) {
							pBar.dismiss();
						}
						try {
							if (Integer.parseInt(new JSONObject(result).optString("Count")) == 0) {
								//无订单数据
								hasData = false;
								if (mAdapter != null) {
									mAdapter.notifyDataSetChanged();
								}
								return;
							}
//							hasData = Integer.parseInt(new JSONObject(result).optString("Count")) == COUNT;
							// 控制是否能上拉加载数据
							JSONArray jsonArr = new JSONObject(result).optJSONArray("Data");
							if (jsonArr.length() < COUNT) {
								hasData = false;
							}else {
								hasData = true;
							}
//							hasData = COUNT > jsonArr.length();
							// 存储数据，方便后期查找使用
							dataMap.put(page, jsonArr);
//							array = jsonArr;
							JSONObject jsonObj = new JSONObject();
							if (orderList == null) {
								orderList = new ArrayList<OrderStatusActivity.OrderInfos>();
							}
							for (int i = 0; i < jsonArr.length(); i++) {
								// 解析外层数据
								jsonObj = jsonArr.optJSONObject(i);
								// 状态
								String orderStatus = "";
								// 支付类型
								String payType = jsonObj.optString("PayType");
								// 是否已评价
								String IsComment = jsonObj.optString("IsComment");
								// 订单号
								String orderNumber = jsonObj.optString("OrderNumber");
								// 订单号
								String ReturnOrderStatus = jsonObj.optString("ReturnOrderStatus");
								// GUID
								String GUID = jsonObj.optString("Guid");
								// 总价
								double price = jsonObj.getDouble("ProductPrice")+ jsonObj.getDouble("DispatchPrice") - jsonObj.getDouble("ScorePrice");
								String totalPrice = new DecimalFormat("0.00").format(price);
								// 状态计算
								int ShipmentStatus = jsonObj.optInt("ShipmentStatus");// 发货状态
								int paymentStatus = jsonObj.optInt("PaymentStatus");
								String StatusName=jsonObj.optString("StatusName");
//								switch (jsonObj.optInt("OderStatus")) {
//								case 0:
//									orderStatus = "待付款";
//									break;
//								case 1:
//									if (paymentStatus == 0) { // 未付款
//										orderStatus = "待付款";
//									} else if (paymentStatus == 2) {// 已付款
//										if (ReturnOrderStatus != null && !"".equals(ReturnOrderStatus)) {
//											orderStatus = ReturnOrderStatus;
//										}else{
//											if (ShipmentStatus == 0) {// 发货状态 未发货
//												orderStatus = "待发货";
//											} else if (ShipmentStatus == 1) {// 已发货
//												orderStatus = "待收货";
//											} else if (ShipmentStatus == 2) {// 已收货
//												orderStatus = "已收货";
//											} else if (ShipmentStatus == 3) {// 配货中
//												orderStatus = "配货中";
//											} else if (ShipmentStatus == 4) {// 退货
//												orderStatus = "已退货";
//												// 新加
//											} else if (ShipmentStatus == 5) {// 完成
//												orderStatus = "完成";
//											}
//										}
//									} else if (paymentStatus == 3) {
//										orderStatus = "已退款";
//									}
//									break;
//								case 2:
//									if (paymentStatus == 0) {// 未付款
//										orderStatus = "已取消";
//									} else if (paymentStatus == 2) {// 已付款
//										if (ShipmentStatus == 0) {// 未发货
//											orderStatus = "退款审核中";
//										} else if (ShipmentStatus == 1) {// 已发货
//											orderStatus = "已发货";
//										}
//									}
//									break;
//								case 5:
//									if (paymentStatus == 2) {
//										if (ShipmentStatus == 2) {
//											orderStatus = "交易成功";
//										} else if (ShipmentStatus == 4) {
//											orderStatus = "已退货";
//										}
//									}
//									break;
//
//								default:
//									break;
//								}
								
								

								if (ReturnOrderStatus != null && !"".equals(ReturnOrderStatus)) {
									orderStatus = ReturnOrderStatus;
								}else{
									
									orderStatus=StatusName;
								}
								// 解析内层数据
								JSONArray arr = jsonObj.optJSONArray("ProductList");
								productList = new ArrayList<OrderStatusActivity.ProductInfo>();
								for (int j = 0; j < arr.length(); j++) {
									//图片
									String productImage = arr.optJSONObject(j).optString("OriginalImge");
									//名称
									String productName = arr.optJSONObject(j).optString("NAME");
									//数量
									String productCount = arr.optJSONObject(j).optString("BuyNumber");
									//价格
									String productPrice = arr.optJSONObject(j).optString("BuyPrice");
									//规格
									String productStandard = arr.optJSONObject(j).optString("Attributes");
									productList.add(new ProductInfo(productImage, productName, productCount, productPrice, productStandard));
								}
								orderList.add(new OrderInfos(orderStatus, orderNumber, payType, totalPrice, GUID, IsComment, productList));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
						if (mAdapter == null) {
							mAdapter = new MyExpandableAdapter();
							expandView = mExpandableListView.getRefreshableView();
							expandView.setAdapter(mAdapter);
							for (int i = 0; i < orderList.size(); i++) {
								expandView.expandGroup(i);
							}
							expandView.setOnGroupClickListener(new OnGroupClickListener() {
								@Override
								public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
//									JSONObject obj = array.optJSONObject(groupPosition % 20);
									JSONObject obj = dataMap.get(groupPosition / COUNT + 1).optJSONObject(groupPosition % COUNT);
									Intent intent = new Intent(OrderStatusActivity.this, OrderInfo.class);
									intent.putExtra("orderList", obj.toString());
									startActivity(intent);
									return true;
								}
							});
							expandView.setOnChildClickListener(new OnChildClickListener() {
								@Override
								public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//									JSONObject obj = array.optJSONObject(groupPosition % 20);
									JSONObject obj = dataMap.get(groupPosition / COUNT + 1).optJSONObject(groupPosition % COUNT);
									Intent intent = new Intent(OrderStatusActivity.this, OrderInfo.class);
									intent.putExtra("orderList", obj.toString());
									startActivity(intent);
									return true;
								}
							});
						}else {
							mAdapter.notifyDataSetChanged();
							for (int i = 0; i < orderList.size(); i++) {
								expandView.expandGroup(i);
							}
						}
					}
				});
			}
		});
	}
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
		// TODO
		reStartGetData();
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
		// TODO
		if (hasData) {
			page ++ ;
			getOrderData();
		}else {
			Toast.makeText(OrderStatusActivity.this, "暂无更多数据", 0).show();
			mExpandableListView.onRefreshComplete();
		}
	}
	/**
	 * ExpandableListView适配器
	 */
	class MyExpandableAdapter extends BaseExpandableListAdapter{

		@Override
		public int getGroupCount() {
			return orderList == null ? 0 : orderList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return orderList == null ? 0 : orderList.get(groupPosition).list == null ? 0 : orderList.get(groupPosition).list.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return orderList == null ? null : orderList.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return orderList == null ? null : orderList.get(groupPosition).list == null ? null : orderList.get(groupPosition).list.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// parent layout : expand_parent_item.xml
			ParentHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(OrderStatusActivity.this, R.layout.expand_parent_item, null);
				holder = new ParentHolder();
				holder.orderStatus = (TextView) convertView.findViewById(R.id.tv_order_status);
				holder.totalPrice = (TextView) convertView.findViewById(R.id.tv_order_total_price);
				convertView.setTag(holder);
			}else {
				holder = (ParentHolder) convertView.getTag();
			}
			OrderInfos info = orderList.get(groupPosition);
			holder.orderStatus.setText("订单状态：" + info.orderStatus);
			String str = "总价：￥" + info.totalPrice;
			SpannableStringBuilder builder = new SpannableStringBuilder(str);  
			  
			//ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色  
			ForegroundColorSpan blackSpan = new ForegroundColorSpan(getResources().getColor(R.color.black));  
			ForegroundColorSpan redSpan = new ForegroundColorSpan(getResources().getColor(R.color.red));  
			builder.setSpan(blackSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
			builder.setSpan(redSpan, 3, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
			holder.totalPrice.setText(builder);
			return convertView;
		}

		@Override
		public View getChildView(final int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// chirld layout : expand_chirld_item.xml
			ChirldHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(OrderStatusActivity.this, R.layout.expand_chirld_item, null);
				holder = new ChirldHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.product_img);
				holder.name = (TextView) convertView.findViewById(R.id.product_name);
				holder.price = (TextView) convertView.findViewById(R.id.product_price);
				holder.number = (TextView) convertView.findViewById(R.id.product_num);
				holder.standard = (TextView) convertView.findViewById(R.id.spec);
				holder.bottom = (LinearLayout) convertView.findViewById(R.id.rl_bottom);
				holder.bt_red = (Button) convertView.findViewById(R.id.bt_red);
				holder.bt_black = (Button) convertView.findViewById(R.id.bt_black);
				convertView.setTag(holder);
			}else {
				holder = (ChirldHolder) convertView.getTag();
			}
//			final int position = groupPosition % 20;
			OrderInfos info = orderList.get(groupPosition);
			String orderStatus = info.orderStatus;
			final String payType = info.payType;
			final String IsComment = info.IsComment;
			final String orderNumber = info.orderNumber;
			final String GUID = info.GUID;
			ProductInfo productInfo = orderList.get(groupPosition).list.get(childPosition);
			ImageLoader.getInstance().displayImage(productInfo.productImage, holder.image, options);
			holder.name.setText(productInfo.productName);
			holder.price.setText("￥" + productInfo.productPrice);
			holder.number.setText("x" + productInfo.productCount);
			if ("".equals(productInfo.productStandard) || productInfo.productStandard == null) {
				holder.standard.setVisibility(View.GONE);
			}else {
				holder.standard.setVisibility(View.VISIBLE);
				holder.standard.setText(productInfo.productStandard);
			}
			int count = getChildrenCount(groupPosition);
			// 是否显示底部按钮
			if (count - 1 == childPosition) { 
				//说明到底部了
				holder.bottom.setVisibility(View.VISIBLE);
				if ("待付款".equals(orderStatus)) {
					holder.bt_red.setVisibility(View.VISIBLE);
					holder.bt_black.setVisibility(View.VISIBLE);
					holder.bt_red.setText("立即支付");
					holder.bt_black.setText("取消订单");
					holder.bt_black.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							final Dialog dialog = new Dialog(OrderStatusActivity.this,
									R.style.MyDialog);
							View view = LayoutInflater.from(OrderStatusActivity.this).inflate(R.layout.dialog, null);
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
									orderCancel(GUID);
									dialog.dismiss();
								}
							});
						}
					});

					holder.bt_red.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							try {
								if ("1".equals(payType)) {
									// TODO
									JSONObject orderObject = dataMap.get(groupPosition / COUNT + 1).optJSONObject(groupPosition % COUNT);
//									JSONObject orderObject = array.optJSONObject(groupPosition % 20);
									Intent intent = new Intent(OrderStatusActivity.this,OrderAllToPay.class);
									intent.putExtra("ProductPrice",orderObject.getDouble("ProductPrice"));
									intent.putExtra("ProductList", orderObject.getString("ProductList"));
									intent.putExtra("OrderNumber", orderObject.getString("OrderNumber"));
									intent.putExtra("PaymentGuid", orderObject.getString("PaymentGuid"));
									intent.putExtra("ScorePrice",orderObject.getDouble("ScorePrice"));
									intent.putExtra("DispatchPrice", orderObject.getDouble("DispatchPrice"));
									startActivity(intent);
								} else if ("2".equals(payType)) {
									Toast.makeText(OrderStatusActivity.this,"请尽快联系商家付款！", Toast.LENGTH_SHORT).show();
								} else if ("0".equals(payType)){
									Toast.makeText(OrderStatusActivity.this,"此商品为货到付款！", Toast.LENGTH_SHORT).show();
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				}else if ("配货中".equals(orderStatus)) {
					holder.bt_red.setVisibility(View.VISIBLE);
					holder.bt_black.setVisibility(View.GONE);
					holder.bt_red.setText("申请退款");
					holder.bt_red.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							JSONObject orderObject = dataMap.get(groupPosition / COUNT + 1).optJSONObject(groupPosition % COUNT);
							Intent i = new Intent(OrderStatusActivity.this, RefoundActivity.class);
							// TODO
//							i.putExtra("orderObject", array.optJSONObject(groupPosition).toString());
							i.putExtra("orderObject", orderObject.toString());
							startActivity(i);
						}
					});
				}else if("退货审核中".equals(orderStatus)){
					holder.bt_red.setVisibility(View.GONE);
					holder.bt_black.setVisibility(View.GONE);
				}else if("退款审核中".equals(orderStatus)){
					holder.bt_red.setVisibility(View.GONE);
					holder.bt_black.setVisibility(View.GONE);
				}else if ("待发货".equals(orderStatus)) {
					holder.bt_red.setVisibility(View.VISIBLE);
					holder.bt_black.setVisibility(View.GONE);
					holder.bt_red.setText("申请退款");
					holder.bt_red.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							JSONObject orderObject = dataMap.get(groupPosition / COUNT + 1).optJSONObject(groupPosition % COUNT);
							Intent i = new Intent(OrderStatusActivity.this, RefoundActivity.class);
							// TODO
//							i.putExtra("orderObject", array.optJSONObject(groupPosition).toString());
							i.putExtra("orderObject", orderObject.toString());
							startActivity(i);
						}
					});
				}else if ("待收货".equals(orderStatus)) {
					holder.bt_red.setVisibility(View.VISIBLE);
					holder.bt_black.setVisibility(View.VISIBLE);
					holder.bt_red.setText("确认收货");
					holder.bt_black.setText("查看物流");
					holder.bt_black.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO
							try {
								JSONObject orderObject = dataMap.get(groupPosition / COUNT + 1).optJSONObject(groupPosition % COUNT);
//								JSONObject orderObject = array.optJSONObject(groupPosition % 20);
								Intent intent = new Intent(OrderStatusActivity.this, KuaidiActivity.class);
								intent.putExtra("code", orderObject.getString("LogisticsCompanyCode"));
								intent.putExtra("id", orderObject.getString("ShipmentNumber"));
								startActivity(intent);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});

					holder.bt_red.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							final Dialog dialog = new Dialog(OrderStatusActivity.this,R.style.MyDialog);
							View view = LayoutInflater.from(OrderStatusActivity.this.getApplicationContext())
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
									// TODO
									JSONObject orderObject = dataMap.get(groupPosition / COUNT + 1).optJSONObject(groupPosition % COUNT);
//									JSONObject orderObject = array.optJSONObject(groupPosition % 20);
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
				}
				
//				else if ("已收货".equals(orderStatus)) {
//					holder.bt_black.setVisibility(View.GONE);
//					holder.bt_red.setVisibility(View.VISIBLE);
//					holder.bt_red.setText("申请退货");
//					holder.bt_red.setOnClickListener(new OnClickListener() {
//						@Override
//						public void onClick(View v) {
//							Toast.makeText(OrderStatusActivity.this, "请联系客服工作人员退货！", Toast.LENGTH_LONG).show();
//						}
//					});
//				}
				
				
				else if ("已完成".equals(orderStatus)) {
					holder.bt_black.setVisibility(View.GONE);
					holder.bt_red.setVisibility(View.VISIBLE);
					if ("false".equals(IsComment)) {
						holder.bt_red.setText("去评价");
						holder.bt_red.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(OrderStatusActivity.this, AssessShowActivity.class);
								
							//	String productList = dataMap.get(groupPosition / COUNT + 1).optJSONObject(groupPosition % COUNT).optJSONArray("ProductList").optJSONObject(0).toString().replace("}", "") + ",\"OrderNumber\":" + orderList.get(groupPosition).orderNumber + "}";
							
								JSONArray productList = dataMap.get(groupPosition / COUNT + 1).optJSONObject(groupPosition % COUNT).optJSONArray("ProductList") ;
								for(int i=0;i<productList.length();i++){
									try {
										productList.optJSONObject(i).put("OrderNumber", orderList.get(groupPosition).orderNumber);
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} 
								}
								intent.putExtra("ProductList", productList.toString());
//								intent.putExtra("ProductList", array.getJSONObject(groupPosition % 20).toString());
								startActivityForResult(intent, 0);
							}
						});
					}else {
						holder.bt_red.setText("已评价");
						holder.bt_red.setOnClickListener(null);
					}
				}else {
					holder.bt_red.setVisibility(View.GONE);
					holder.bt_black.setVisibility(View.VISIBLE);
					holder.bt_black.setText("删除订单");
					holder.bt_black.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							orderDelete(orderNumber);
						}
					});
				}
			}else {
				holder.bottom.setVisibility(View.GONE);
			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
	
	static class ParentHolder{
		TextView orderStatus;
		TextView totalPrice;
	}
	
	static class ChirldHolder{
		ImageView image;
		TextView name;
		TextView price;
		TextView number;
		TextView standard;
		LinearLayout bottom;
		Button bt_red;
		Button bt_black;
	}
	
	
	class OrderInfos{
		String orderStatus;//订单状态
		String orderNumber;//订单号
		String payType;//支付类型
		String totalPrice;//总价
		String GUID;
		String IsComment;
		List<ProductInfo> list;
		public OrderInfos(String orderStatus, String orderNumber,
				String payType, String totalPrice, String gUID,
				String isComment, List<ProductInfo> list) {
			super();
			this.orderStatus = orderStatus;
			this.orderNumber = orderNumber;
			this.payType = payType;
			this.totalPrice = totalPrice;
			GUID = gUID;
			IsComment = isComment;
			this.list = list;
		}
	}
	class ProductInfo{
		String productImage;//图片
		String productName;//名称
		String productCount;//数量
		String productPrice;//价格
		String productStandard;//规格
		public ProductInfo(String productImage, String productName,
				String productCount, String productPrice, String productStandard) {
			super();
			this.productImage = productImage;
			this.productName = productName;
			this.productCount = productCount;
			this.productPrice = productPrice;
			this.productStandard = productStandard;
		}
	}
	/**
	 * 取消订单
	 * @param guid
	 */
	public void orderCancel(final String guid) {
		new Thread() {
			public void run() {
				final StringBuffer result = new HttpConn().getArray("/api/ordercancel/?id=" + guid+ "&AppSign=" + HttpConn.AppSign);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						try {
							if (new JSONObject(result.toString()).getString("return").equals("202")) {
								reStartGetData();
							} else {
								Toast.makeText(OrderStatusActivity.this,"取消失败", Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}							
					}
				});
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
				final StringBuffer result = new HttpConn().getArray("/api/DeleteOrder/?AppSign="+ HttpConn.AppSign + "&orderNumber="+ orderNumber + "&memLoginID="+ HttpConn.username);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						reStartGetData();
					}
				});
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
				StringBuffer result = new HttpConn().getArray("/api/order/UpdateShipmentStatus/?id=" + guid + "&AppSign=" + HttpConn.AppSign);
				try {
					final String str = new JSONObject(result.toString()).getString("return");
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if ("202".equals(str)) {
								reStartGetData();
							} else {
								Toast.makeText(OrderStatusActivity.this,"确认失败", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	/**
	 * 当订单被修改、删除等操作后重新刷新数据
	 */
	void reStartGetData(){
		page = 1;
		hasData = true;
		orderList = null;
		if(mAdapter != null)
			mAdapter.notifyDataSetChanged();
		getOrderData();
	}
}

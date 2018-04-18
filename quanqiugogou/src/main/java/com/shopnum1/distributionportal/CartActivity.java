package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CartActivity extends Activity {

	private HttpConn httpget = new HttpConn();
	private Dialog pBar; // 加载进度
	private JSONArray cartList;
	private List<String> guidList = new ArrayList<String>();
	private HashSet<String> productguid = new HashSet<String>();
	private ListView listview;
	private ListAdapter adapter;
	private int checkNum, totalNumber;// 商品种数和件数
	/** 购物车最大选中数 40 */
	private static int maxCheck = 40; // 最大选中数
	private CheckBox checkAll;
	private boolean isShowToast = false;
	private ArrayList<JSONObject> productList = new ArrayList<JSONObject>();
	private int number;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cart);
		initLayout();
		httpget.getNetwork(this); // 判断网络
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
	}

	@Override
	protected void onResume() {
		new Thread() {
			public void run() {
				String shoppingcartgetUrl = "/api/shoppingcartget/?loginId="
						+ HttpConn.username + "&AppSign="
						+ HttpConn.AppSign + "&agentID="+MyApplication.agentId;
				StringBuffer result = httpget
						.getArray(shoppingcartgetUrl);
				try {
					HttpConn.cartNum = new JSONObject(result.toString())
							.getJSONArray("Data").length();
				} catch (JSONException e) {
					HttpConn.cartNum = 0;
					e.printStackTrace();
				}
				Message msg = Message.obtain();
				msg.what = 3;
				handler.sendMessage(msg);
			}
		}.start();
		super.onResume();
	}
	// 初始化
	public void initLayout() {
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

		// 个人中心
		((RelativeLayout) findViewById(R.id.imageButton4))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),
								MemberActivity.class));
						overridePendingTransition(android.R.anim.fade_in,
								android.R.anim.fade_out);
					}

				});
		}

	// 获取数据
	public void getData() {
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();

		new Thread() {
			public void run() {
				String shoppingcartgetUrl = "/api/shoppingcartget/?loginId="
						+ HttpConn.username + "&AppSign="
						+ HttpConn.AppSign + "&agentID="+MyApplication.agentId;
				StringBuffer result = httpget
						.getArray(shoppingcartgetUrl);
				try {
					cartList = new JSONObject(result.toString())
							.getJSONArray("Data");
					Log.i("test", cartList + "");
					Message msg = Message.obtain();
					msg.what = 1;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				pBar.dismiss();
				if (cartList.length() > 0) {
					((Button) findViewById(R.id.delete))
							.setVisibility(View.VISIBLE);
					addList();
				} else {
					((Button) findViewById(R.id.delete))
							.setVisibility(View.GONE);
					((LinearLayout) findViewById(R.id.nocontent))
							.setVisibility(View.VISIBLE);
				}
				break;
			case 2:
				((LinearLayout) findViewById(R.id.nocontent))
						.setVisibility(View.VISIBLE);

				finish();
				startActivity(new Intent(getApplicationContext(),
						CartActivity.class));
				break;
			case 3:
				if (HttpConn.cartNum > 0) {
					((TextView) findViewById(R.id.num))
							.setVisibility(View.VISIBLE);
					((TextView) findViewById(R.id.num))
							.setText(HttpConn.cartNum + "");
				} else {
					((TextView) findViewById(R.id.num))
							.setVisibility(View.GONE);
				}
				break;
			case 4:
				if (msg.obj.toString().equals("101")){
					
					Toast.makeText(getBaseContext(), "该商品已达最大限购量！", Toast.LENGTH_SHORT)
							.show();
					}else if(msg.obj.toString().equals("100")){
						Toast.makeText(getBaseContext(), "库存不足！", Toast.LENGTH_SHORT).show();
					}else{
						getData();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	public void addList() {
		((RelativeLayout) findViewById(R.id.total)).setVisibility(View.VISIBLE);
		((RelativeLayout) findViewById(R.id.total)).getBackground().setAlpha(
				100);
		adapter = new ListAdapter();
		listview = (ListView) findViewById(R.id.listview);
		listview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		checkNum = 0;
		listview.setAdapter(adapter);
		checkAll = (CheckBox) findViewById(R.id.check);
		checkAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				productList = new ArrayList<JSONObject>();
				if (checkAll.isChecked()) {
					if (checkNum == 0) {
						// TODO 购物车单次下单商品不超过40种
						if (cartList.length() > maxCheck) {
							Toast.makeText(getApplicationContext(),
									"购物车单次最多提交40件商品", Toast.LENGTH_SHORT)
									.show();
							checkNum = maxCheck;
						} else {
							checkNum = cartList.length();
						}
						for (int i = 0; i < checkNum; i++) {
							adapter.getIsSelected().put(i, true);
							try {
								productList.add(cartList.getJSONObject(i));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					} else {
						checkNum = maxCheck;
						if (cartList.length() > maxCheck) {
							checkNum = maxCheck;
							for (int i = 0; i < maxCheck; i++) {
								adapter.getIsSelected().put(i, true);
							}
							for (int i = maxCheck; i < cartList.length(); i++) {
								adapter.getIsSelected().put(i, false);
							}
						} else {
							checkNum = cartList.length();
							for (int i = 0; i < cartList.length(); i++) {
								adapter.getIsSelected().put(i, true);
							}
						}
					}
				} else {
					checkNum = 0;
					for (int i = 0; i < cartList.length(); i++) {
						adapter.getIsSelected().put(i, false);
					}
				}
				adapter.notifyDataSetChanged();
				setPrice();
			}
		});
		// 删除
		((Button) findViewById(R.id.delete))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (checkNum == 0) {
							Toast.makeText(getApplicationContext(), "请选择商品",
									Toast.LENGTH_SHORT).show();
						} else {
							final Dialog dialog = new Dialog(CartActivity.this,
									R.style.MyDialog);
							View view = LayoutInflater.from(getBaseContext())
									.inflate(R.layout.dialog, null);
							((TextView) view.findViewById(R.id.dialog_text))
									.setText("是否删除已选商品？");
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
											for (int i = 0; i < listview
													.getAdapter().getCount(); i++) {
												View view = listview
														.getAdapter().getView(
																i, null,
																listview);
												CheckBox check = (CheckBox) view
														.findViewById(R.id.check);
												TextView text7 = (TextView) view
														.findViewById(R.id.text7);
												if (check.isChecked()) {
													guidList.add(text7
															.getText()
															.toString());
												}
											}
											new Thread() {
												public void run() {
													for (int i = 0; i < guidList
															.size(); i++) {
														httpget.getArray("/api/shoppingcartdelete/?Guid="
																+ guidList
																		.get(i)
																+ "&MemLoginID="
																+ HttpConn.username
																+ "&AppSign="
																+ HttpConn.AppSign);
													}
													String shoppingcartgetUrl = "/api/shoppingcartget/?loginId="
															+ HttpConn.username + "&AppSign="
															+ HttpConn.AppSign + "&agentID="+MyApplication.agentId;
													StringBuffer result = httpget
															.getArray(shoppingcartgetUrl);
													try {
														HttpConn.cartNum = new JSONObject(
																result.toString())
																.getJSONArray(
																		"Data")
																.length();
													} catch (JSONException e) {
														e.printStackTrace();
													}
													Message msg = Message
															.obtain();
													msg.what = 2;
													handler.sendMessage(msg);
												}
											}.start();
										}
									});
						}
					}
				});
		// 移至收藏夹
		((Button) findViewById(R.id.collect))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						for (int i = 0; i < listview.getAdapter().getCount(); i++) {
							View view = listview.getAdapter().getView(i, null,
									listview);
							CheckBox check = (CheckBox) view
									.findViewById(R.id.check);
							TextView text8 = (TextView) view
									.findViewById(R.id.text8);
							if (check.isChecked()) {
								productguid.add(text8.getText().toString());
							}
						}
						new Thread() {
							@Override
							public void run() {
								super.run();
								Iterator<String> it = productguid.iterator();
								while (it.hasNext()) {
									StringBuffer result = httpget.getArray("/api/collectadd/?productGuid="+ it.next()
													+ "&MemLoginID="
													+ HttpConn.username
													+ "&AppSign="
													+ HttpConn.AppSign);
								}
							}
						}.start();
						if (productguid.size() > 0) {
							Toast.makeText(getApplicationContext(), "收藏成功",1000).show();
						} else {
							Toast.makeText(getApplicationContext(), "请选择商品",1000).show();
						}

					}
				});
		// 编辑
		((LinearLayout) findViewById(R.id.edit))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (((TextView) findViewById(R.id.tv_edit)).getText()
								.toString().equals("编辑")) {
							((TextView) findViewById(R.id.tv_edit))
									.setText("完成");
							findViewById(R.id.ll_more).setVisibility(
									View.VISIBLE);

							findViewById(R.id.count).setVisibility(View.GONE);
							findViewById(R.id.rl_info).setVisibility(View.GONE);
						} else {
							((TextView) findViewById(R.id.tv_edit))
									.setText("编辑");
							findViewById(R.id.ll_more).setVisibility(View.GONE);
							findViewById(R.id.count)
									.setVisibility(View.VISIBLE);
							findViewById(R.id.rl_info).setVisibility(
									View.VISIBLE);
						}
					}
				});

		// 结算
		((Button) findViewById(R.id.count))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (checkNum == 0) {
							Toast.makeText(getApplicationContext(), "请选择商品",
									Toast.LENGTH_SHORT).show();
						} else if (checkNum > maxCheck) {
							Toast.makeText(getApplicationContext(),
									"购物车单次最多购买40个商品", Toast.LENGTH_SHORT)
									.show();
						} else if (number == 0) {
							Toast.makeText(getApplicationContext(), "至少购买一件商品",
									Toast.LENGTH_SHORT).show();
						} else {
							Intent intent = new Intent(getBaseContext(),
									OrderPost.class);
							JSONArray ProductList = new JSONArray();
							for (int i = 0; i < productList.size(); i++) {
								ProductList.put(productList.get(i));
							}
							intent.putExtra("BuyNumber", totalNumber);
							intent.putExtra("ProductPrice",
									((TextView) findViewById(R.id.totalPrice))
											.getText().toString());
							intent.putExtra("ProductList",
									ProductList.toString());
							startActivityForResult(intent, 0);
						}
					}
				});
	}

	public void setPrice() {
		((TextView) findViewById(R.id.totalPrice)).setText("￥"
				+ new DecimalFormat("0.00").format(getPrice()));
		((Button) findViewById(R.id.count)).setText("结算(" + checkNum + ")");
		if (totalNumber > 0) {
			((Button) findViewById(R.id.count)).setTextColor(Color.WHITE);
			((Button) findViewById(R.id.count))
					.setBackgroundResource(R.drawable.cart_buy1);
		} else {
			((Button) findViewById(R.id.count)).setTextColor(Color.BLACK);
			((Button) findViewById(R.id.count))
					.setBackgroundResource(R.drawable.cart_buy);
		}
	}

	public Double getPrice() {
		Double totalPrice = 0.00d;
		totalNumber = 0;
		for (int i = 0; i < listview.getAdapter().getCount(); i++) {
			View view = listview.getAdapter().getView(i, null, listview);
			CheckBox check = (CheckBox) view.findViewById(R.id.check);
			TextView text3 = (TextView) view.findViewById(R.id.text3);
			TextView text5 = (TextView) view.findViewById(R.id.text5);
			if (check.isChecked()) {
				totalPrice += Float.parseFloat(text3.getText().toString()
						.substring(1))
						* Integer.parseInt(text5.getText().toString());
				totalNumber += Integer.parseInt(text5.getText().toString());
			}
		}
		return totalPrice;
	}

//	public void numberUpdate(final String Guid, final int number) {
//		new Thread() {
//			public void run() {
//				StringBuffer result = httpget
//						.getArray("/api/shoppingcartput/?Guid=" + Guid
//								+ "&MemLoginID=" + HttpConn.username
//								+ "&BuyNumber=" + number + "&AppSign="
//								+ HttpConn.AppSign);
//				Log.i("fly", result.toString());
//			}
//		}.start();
//	}
	public void numberUpdate(final String Guid, final int number, final int position,final TextView text) {
	new Thread() {
		public void run() {
			try {
			JSONObject object2 = new JSONObject();
			object2.put("MemLoginID", HttpConn.UserName);
			object2.put("ProductGuid", cartList.getJSONObject(position).getString("ProductGuid"));
			object2.put("BuyNumber", number);
			object2.put("BuyPrice", cartList.getJSONObject(position).getString("BuyPrice"));
		
			object2.put("Attributes",cartList.getJSONObject(position).getString("Attributes"));
			object2.put("DetailedSpecifications", cartList.getJSONObject(position).getString("DetailedSpecifications"));
			object2.put("AppSign", HttpConn.AppSign);
			object2.put("ExtensionAttriutes", cartList.getJSONObject(position).getString("ExtensionAttriutes"));
			StringBuffer result = httpget.postJSON("/api/shoppingcartadd/", object2.toString());
		
		
			Message msg = Message.obtain();
			msg.obj = new JSONObject(result.toString()).getString("return");
			msg.what = 4;
			handler.sendMessage(msg);
			if(text!=null){
				text.setClickable(true);
			}
			
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}.start();
}
	// 列表适配器
	class ListAdapter extends BaseAdapter {

		private HashMap<Integer, Boolean> isSelected;
		private HashMap<Integer, Integer> BuyNumber;

		public ListAdapter() {
			isSelected = new HashMap<Integer, Boolean>();
			BuyNumber = new HashMap<Integer, Integer>();

			for (int i = 0; i < cartList.length(); i++) {
				getIsSelected().put(i, false);
				try {
					getBuyNumber().put(i,cartList.getJSONObject(i).getInt("BuyNumber"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public int getCount() {

			return cartList.length();
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.item_cart, null);
				holder.check = (CheckBox) convertView.findViewById(R.id.check);
				holder.text1 = (TextView) convertView.findViewById(R.id.text1);
				holder.imageview = (ImageView) convertView
						.findViewById(R.id.imageView1);
				holder.text2 = (TextView) convertView.findViewById(R.id.text2);
				holder.text3 = (TextView) convertView.findViewById(R.id.text3);
				holder.text4 = (TextView) convertView.findViewById(R.id.text4);
				holder.text5 = (TextView) convertView.findViewById(R.id.text5);
				holder.text6 = (TextView) convertView.findViewById(R.id.text6);
				holder.text7 = (TextView) convertView.findViewById(R.id.text7);
				holder.text8 = (TextView) convertView.findViewById(R.id.text8);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			try {

				holder.check.setChecked(getIsSelected().get(position));
				holder.text1.setText(cartList.getJSONObject(position)
						.getString("Name"));
				
				holder.text2.setText(cartList.getJSONObject(position)
						.getString("Attributes"));

				holder.text3
						.setText("￥"
								+ new DecimalFormat("0.00").format(cartList
										.getJSONObject(position).getDouble(
												"BuyPrice")));
				number = getBuyNumber().get(position);
				holder.text5.setText(getBuyNumber().get(position) + "");
				holder.text7.setText(cartList.getJSONObject(position)
						.getString("Guid"));
				holder.text8.setText(cartList.getJSONObject(position)
						.getString("ProductGuid"));
				if (HttpConn.showImage)
					ImageLoader.getInstance().displayImage(
							cartList.getJSONObject(position).getString(
									"OriginalImge"), holder.imageview,
							MyApplication.options);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			final CheckBox check = holder.check;
			final TextView text5 = holder.text5;
			final TextView text4 = holder.text4;
			final TextView text6 = holder.text6;
			check.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (check.isChecked()) {
						// TODO 购物车单次下单商品不超过40种
						if (checkNum < maxCheck) {
							checkNum++;
							if (checkNum == cartList.length()) {
								checkAll.setChecked(true);
							}
							try {
								getBuyNumber().put(
										position,
										Integer.parseInt(text5.getText()
												.toString()));
								cartList.getJSONObject(position).put(
										"BuyNumber",
										getBuyNumber().get(position));
								productList.add(cartList
										.getJSONObject(position));
							} catch (JSONException e1) {
								e1.printStackTrace();
							}
							adapter.getIsSelected().put(position,
									check.isChecked());
							check.setChecked(true);
							setPrice();
						} else {
							if (!isShowToast) {
								Toast.makeText(getApplicationContext(),
										"添加失败，购物车单次最多提交40件商品",
										Toast.LENGTH_SHORT).show();
								isShowToast = true;
							}
							isShowToast = false;

							adapter.getIsSelected().put(position, false);
							check.setChecked(false);
							adapter.notifyDataSetChanged();
						}
					} else {
						remove(position);
						checkNum--;
						if (checkAll.isChecked()) {
							checkAll.setChecked(false);
						}
						adapter.getIsSelected()
								.put(position, check.isChecked());
						setPrice();
						Log.e("取消时position", position + "");
						Log.e("取消时totalNumber", totalNumber + "");
					}
					// adapter.getIsSelected().put(position, check.isChecked());
					// setPrice();
				}
			});

			holder.imageview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					try {
						Intent intent = new Intent(getApplicationContext(),
								ProductDetails.class);
						intent.putExtra(
								"guid",
								cartList.getJSONObject(position).getString(
										"ProductGuid"));
						startActivity(intent);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});

			// 修改数量
			final TextView buynum = holder.text5;
//			holder.text5.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View arg0) {
//					final Dialog dialog = new Dialog(CartActivity.this,
//							R.style.MyDialog);
//					final View view = LayoutInflater.from(getBaseContext())
//							.inflate(R.layout.dialog4, null);
//					dialog.setContentView(view);
//					dialog.show();
//
//					final EditText editText = (EditText) view
//							.findViewById(R.id.edit);
//					String num = buynum.getText().toString();
//					editText.setText(num);
//					editText.setSelection(num.length());
//
//					((Button) view.findViewById(R.id.no))
//							.setOnClickListener(new OnClickListener() {
//
//								@Override
//								public void onClick(View arg0) {
//									dialog.dismiss();
//								}
//							});
//
//					((Button) view.findViewById(R.id.yes))
//							.setOnClickListener(new OnClickListener() {
//
//								@Override
//								public void onClick(View arg0) {
//									if (!editText.getText().toString()
//											.equals("")) {
//										int i = Integer.parseInt(editText
//												.getText().toString());
//										int repertoryCount;
//										try {
//											repertoryCount = cartList
//													.getJSONObject(position)
//													.getInt("RepertoryCount");
//											if (i > repertoryCount) {
//												Toast.makeText(
//														getApplicationContext(),
//														"库存不足", 0).show();
//											} else {
//												buynum.setText(i + "");
//												try {
//													cartList.getJSONObject(
//															position).put(
//															"BuyNumber", i);
//													adapter.getBuyNumber().put(
//															position, i);
//													setPrice();
//													numberUpdate(
//															cartList.getJSONObject(
//																	position)
//																	.getString(
//																			"Guid"),
//															i,position,null);
//												} catch (JSONException e) {
//													e.printStackTrace();
//												}
//												dialog.dismiss();
//											}
//										} catch (JSONException e1) {
//											e1.printStackTrace();
//										}
//
//									}
//
//								}
//							});
//				}
//			});

			// 减少数量
			holder.text4.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					int i = Integer.parseInt(buynum.getText().toString());
					if (i > 1) {
						try {
						//	buynum.setText((i - 1) + "");
							text4.setClickable(false);
							cartList.getJSONObject(position).put("BuyNumber",
									i - 1);
							adapter.getBuyNumber().put(position, i - 1);
							setPrice();
//							numberUpdate(cartList.getJSONObject(position)
//									.getString("Guid"), i - 1,position);
							numberUpdate(cartList.getJSONObject(position)
								.getString("Guid"),  - 1,position,text4);
						
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						Toast.makeText(getApplicationContext(), "最少购买1件商品",
								Toast.LENGTH_SHORT).show();
					}
				}
			});

			// 增加数量
			holder.text6.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					try {
						int i = Integer.parseInt(buynum.getText().toString());
						int repertoryCount = cartList.getJSONObject(position)
								.getInt("RepertoryCount");
						
						if (i < repertoryCount) {
						//	buynum.setText((i + 1) + "");
							text6.setClickable(false);
							cartList.getJSONObject(position).put("BuyNumber",
									i + 1);
							adapter.getBuyNumber().put(position, i + 1);
							setPrice();
//							numberUpdate(cartList.getJSONObject(position)
//									.getString("Guid"), i + 1,position);
							numberUpdate(cartList.getJSONObject(position)
									.getString("Guid"),  1,position,text6);
							
						} else {
							Toast.makeText(getApplicationContext(),
									"最多只能购买" + i + "件",
									Toast.LENGTH_SHORT).show();
						}
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});

			return convertView;
		}

		public void remove(int position) {
			try {
				productList.remove(cartList.getJSONObject(position));
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

		public HashMap<Integer, Boolean> getIsSelected() {
			return isSelected;
		}

		public HashMap<Integer, Integer> getBuyNumber() {
			return BuyNumber;
		}

		class ViewHolder {
			CheckBox check;
			ImageView imageview;
			TextView text1;
			TextView text2;
			TextView text3;
			TextView text4;
			TextView text5;
			TextView text6;
			TextView text7;
			TextView text8;
		}

	}

	// 返回主页
	@Override
	public void onBackPressed() {
		startActivity(new Intent(getBaseContext(), MainActivity.class));
		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1)
			finish();
		startActivity(new Intent(getApplicationContext(), CartActivity.class));
		super.onActivityResult(requestCode, resultCode, data);
	}
}
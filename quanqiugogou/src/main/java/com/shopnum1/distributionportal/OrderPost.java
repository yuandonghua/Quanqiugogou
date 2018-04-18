package com.shopnum1.distributionportal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;

public class OrderPost extends Activity {
	private HttpConn httpget = new HttpConn();
	private String Number, NAME, Mobile, Address, Email, Tel, Guid, Code,Postalcode, DispatchModeGuid, PayType;
	private String PayName = "";
	private Double Weight = 0.00d;
	private String PaymentGuid = "00000000-0000-0000-0000-000000000000";
	private Double ScorePrice, peipr, baopr, ShouldPayPrice;
	private int CanByScores, UseScore;
	private JSONArray ProductList;
	private JSONObject ProductObject;
	private ArrayList<Map<String, String>> PaymentTypeList;
	private ArrayList<Map<String, String>> PaymentList;
	private ArrayList<Map<String, String>> DispatchList = new ArrayList<Map<String, String>>();;
	private Boolean posted = true;
	private Boolean isChoose1 = false;
	private Boolean isChoose2 = false;
	private Boolean isChoose3 = false;
	private Boolean isSaled = true;
	private Dialog pBar;
	private ListAdapter adapter;
	private String name;
	private String buyNumber;
	private Double buyPrice;
	private String actuaPrice;
	private CheckBox fapiao_need;
	private EditText et_1;
	private EditText et_2;
	private LinearLayout ly_edit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_post);
		((RelativeLayout) findViewById(R.id.layout_payment)).setVisibility(View.GONE);
		initLayout();
		getOrderno();
		getAddress();
	}

	// 初始化
	public void initLayout() {
		ly_edit = (LinearLayout) this.findViewById(R.id.ly_edit);
		fapiao_need = (CheckBox) this.findViewById(R.id.fapiao_need);
		et_1 = (EditText) this.findViewById(R.id.et_1);
		et_2 = (EditText) this.findViewById(R.id.et_2);
		fapiao_need.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					ly_edit.setVisibility(View.VISIBLE);
				} else {
					ly_edit.setVisibility(View.GONE);
				}
			}
		});

		// 返回
		((LinearLayout) findViewById(R.id.back))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						setResult(1, getIntent());
						finish();
					}
				});

		try {
			((TextView) findViewById(R.id.text5)).setText(getIntent().getStringExtra("ProductPrice"));
			((TextView) findViewById(R.id.text15)).setText(getIntent().getStringExtra("ProductPrice"));
			ProductList = new JSONArray(getIntent().getStringExtra("ProductList"));
			ListView listview = (ListView) findViewById(R.id.listview);
			adapter = new ListAdapter(ProductList);
			listview.setAdapter(adapter);

			getWeight();

			// 提交订单
			((Button) findViewById(R.id.okbtn))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (isChoose1 && isChoose2 && isChoose3 && posted) {
								postData();

								if (isSaled && "在线支付".equals(PayName)) {
									if (("0.00").equals(actuaPrice)) {
//										Intent intent = new Intent(
//												getApplicationContext(),
//												OrderActivity.class);
//										intent.putExtra("title", "全部订单");
//										intent.putExtra("Guid", Guid);
//										startActivity(intent);
										Intent intent = new Intent(getBaseContext(),
												OrderStatusActivity.class);
										intent.putExtra("type", "0");
										startActivity(intent);
									} else {
										Intent intent = new Intent(
												OrderPost.this,
												PayModeActivity.class);
										intent.putExtra("PayName", PayName);
										intent.putExtra("ShouldPayPrice",ShouldPayPrice);
										intent.putExtra("OrderNumber", Number);
										intent.putExtra("PaymentGuid",PaymentGuid);
										intent.putExtra("name", name);
										intent.putExtra("buyNumber", buyNumber);
										intent.putExtra("buyPrice", buyPrice);
										intent.putExtra("Guid", Guid);
										intent.putExtra("ProductList",
												ProductList.toString());
										intent.putExtra("ScorePrice",
												ScorePrice);
										startActivity(intent);
									}
								} else if (isSaled && !"在线支付".equals(PayName)) {
//									Intent intent = new Intent(new Intent(getApplicationContext(),
//											OrderActivity.class));
//									intent.putExtra("type", 0);
//									intent.putExtra("title", "全部订单");
//									startActivity(intent);
									Intent intent = new Intent(getBaseContext(),
											OrderStatusActivity.class);
									intent.putExtra("type", "0");
									startActivity(intent);
								} else {
									Toast.makeText(getApplicationContext(),"区域无货", Toast.LENGTH_SHORT).show();
								}
							} else if (isChoose1 == false || isChoose2 == false
									|| isChoose3 == false) {
								Toast.makeText(getApplicationContext(),
										"请填写完整订单信息", Toast.LENGTH_SHORT).show();
							} else if (isChoose1 && isChoose2 && isChoose3
									&& !posted) {
								Toast.makeText(getApplicationContext(),
										"订单失效，请重新下单", Toast.LENGTH_SHORT)
										.show();
							}
						}
					});
			// 没有收货地址
			((RelativeLayout) findViewById(R.id.layout_noaddress))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							clearAll();
						}
					});
			// 收货地址
			((LinearLayout) findViewById(R.id.layout_address))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							clearAll();
						}
					});

			// 选择配送方式
			((RelativeLayout) findViewById(R.id.layout_dispatchs))
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (DispatchList.size() > 0) {
								final Dialog dialog = new Dialog(
										OrderPost.this, R.style.MyDialog2);
								View view = LayoutInflater.from(getBaseContext()).inflate(
										R.layout.dialog2, null);
								ListView listview = (ListView) view
										.findViewById(R.id.listview);
								SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), DispatchList,
										R.layout.city_item, new String[] {
												"guid", "name", "peipr",
												"baopr" }, new int[] {
												R.id.guid, R.id.name, R.id.pei,
												R.id.bao });
								listview.setAdapter(adapter);
								dialog.setContentView(view);
								dialog.show();

								listview.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(
											AdapterView<?> arg0, View v,
											int arg2, long arg3) {
										((TextView) findViewById(R.id.text9)).setText(((TextView) v
												.findViewById(R.id.name))
												.getText().toString());
										DispatchModeGuid = ((TextView) v
												.findViewById(R.id.guid))
												.getText().toString();
										isChoose3 = true;
										dialog.dismiss();

										Double ProductPrice = Double
												.parseDouble(getIntent()
														.getStringExtra("ProductPrice")
														.substring(1));
										peipr = Double.parseDouble(((TextView) v.findViewById(R.id.pei))
												.getText().toString());
										baopr = Double.parseDouble(((TextView) v
												.findViewById(R.id.bao))
												.getText().toString());
										ShouldPayPrice = ProductPrice + peipr + baopr;

										((TextView) findViewById(R.id.text11)).setText("￥"+ new DecimalFormat("0.00")
																.format(peipr));
										((TextView) findViewById(R.id.text15)).setText("￥"+ new DecimalFormat("0.00")
														.format(ShouldPayPrice));
										getPrice();
									}
								});
							} else if (!isChoose1) {
								Toast.makeText(getApplicationContext(),
										"请选择收货地址", Toast.LENGTH_SHORT).show();
							} else if (!isChoose2) {
								Toast.makeText(getApplicationContext(),
										"请选择支付类型", Toast.LENGTH_SHORT).show();
							}
						}
					});

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// 清除已选
	public void clearAll() {
		isChoose2 = false;
		isChoose3 = false;
		PaymentGuid = "00000000-0000-0000-0000-000000000000";
		((TextView) findViewById(R.id.text19)).setText("请选择");
		((TextView) findViewById(R.id.text7)).setText("请选择");
		((TextView) findViewById(R.id.text9)).setText("请选择");
		((TextView) findViewById(R.id.text11)).setText("");
		((TextView) findViewById(R.id.text15)).setText(getIntent().getStringExtra("ProductPrice"));
		startActivityForResult(new Intent(getBaseContext(), MemberAddress.class), 0);
	}

	// 获取重量
	public void getWeight() {
		new Thread() {
			public void run() {
				for (int i = 0; i < ProductList.length(); i++) {
					Message msg = Message.obtain();
					try {
						String ProductGuid = ProductList.getJSONObject(i)
								.getString("ProductGuid");
						
						String productUrl = "/api/product/?id=" + ProductGuid+ "&MemLoginID=" + HttpConn.username
								+ "&AppSign=" + HttpConn.AppSign + "&AgentID="+MyApplication.agentId + "&Sbool=true";
						StringBuffer result = httpget
								.getArray(productUrl);
						Weight += new JSONObject(result.toString())
								.getJSONObject("ProductInfo").getDouble(
										"Weight");
						msg.what = 10;
					} catch (JSONException e) {
						msg.what = 10;
						e.printStackTrace();
					}
					handler.sendMessage(msg);
				}
			}
		}.start();
	}

	// 是否有货
	public void getGoods() {
		new Thread() {
			public void run() {
				for (int i = 0; i < ProductList.length(); i++) {
					try {
						String ProductGuid = ProductList.getJSONObject(i)
								.getString("ProductGuid");
						StringBuffer result = httpget.getArray("/api/productstockbyarea/?ProducGuid="
										+ ProductGuid
										+ "&province="
										+ Code.substring(0, 3)
										+ "&city="
										+ Code.substring(3, 6)
										+ "&region="
										+ Code.substring(6, 9)
										+ "&AppSign="
										+ HttpConn.AppSign);
						if (new JSONObject(result.toString()).getString(
								"return").equals("202")) {
							ProductList.getJSONObject(i).put("goods", "有货");
						} else {
							isSaled = false;
							ProductList.getJSONObject(i).put("goods", "无货");
						}

						Message msg = Message.obtain();
						msg.what = 11;
						handler.sendMessage(msg);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	// 获取订单号
	public void getOrderno() {
		new Thread() {
			@Override
			public void run() {
				StringBuffer result = httpget.getArray("/api/getorderno/"
						+ "?AppSign=" + HttpConn.AppSign);
				try {
					Number = new JSONObject(result.toString()).getString("OrderNumber");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	// 获取收货地址
	public void getAddress() {
		new Thread() {
			@Override
			public void run() {
				StringBuffer result = null;
				try {
					result = httpget.getArray("/api/address/?MemLoginID="
							+ URLEncoder.encode(HttpConn.username, "utf-8")
							+ "&AppSign=" + HttpConn.AppSign);
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				Message msg = Message.obtain();
				try {
					JSONObject addressList = new JSONObject(result.toString()).getJSONArray("Data").getJSONObject(0);
					NAME = addressList.getString("NAME");
					Mobile = addressList.getString("Mobile");
					Address = addressList.getString("Address");
					Email = addressList.getString("Email");
					Tel = addressList.getString("Tel");
					Code = addressList.getString("Code");
					Guid = addressList.getString("Guid");
					Postalcode = addressList.getString("Postalcode");

					if (Code.length() == 3) {
						Code += "001001";
					} else if (Code.length() == 6) {
						Code += "001";
					}

					isChoose1 = true;
					msg.what = 1;
				} catch (JSONException e) {
					isChoose1 = false;
					msg.what = 0;
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	// 获取支付类型
	public void getPaymentType() {
		new Thread() {
			@Override
			public void run() {
				StringBuffer result = httpget.getArray("/api/paytype/?AppSign="+ HttpConn.AppSign);
				Log.i("text", result.toString());
				try {
					String data = result.toString().replace("\\", "");
					JSONObject object = new JSONObject(data.substring(1,data.length() - 1));
					Log.i("fly", object.toString());
					PaymentTypeList = new ArrayList<Map<String, String>>();
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("guid", "1");
					map.put("name", "在线支付");
					PaymentTypeList.add(map);
					if (object.getString("HuoDaoFuKuan").equals("1")) {
						HashMap<String, String> map1 = new HashMap<String, String>();
						map1.put("guid", "0");
						map1.put("name", "货到付款");
						PaymentTypeList.add(map1);
					}
					if (object.getString("XianXia").equals("1")) {
						HashMap<String, String> map2 = new HashMap<String, String>();
						map2.put("guid", "2");
						map2.put("name", "线下支付");
						PaymentTypeList.add(map2);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Message msg = Message.obtain();
				msg.what = 9;
				handler.sendMessage(msg);
			}
		}.start();
	}

	/**
	 * 获取配送方式
	 */
	public void getDispatch() {
		new Thread() {
			public void run() {
				Message msg = Message.obtain();
				msg.what = 3;
				try {
					StringBuffer result = httpget.getArray("/api/dispatchmodelistbycode/?dataSMemberID="
									+ HttpConn.username
									+ "&dataSHGUID="
									+ Guid
									+ "&dataZFGUID="
									+ PayType
									+ "&Strpuallpr="
									+ getIntent()
											.getStringExtra("ProductPrice")
											.substring(1)
									+ "&Strpuallcou="
									+ getIntent().getIntExtra("BuyNumber", 1)
									+ "&StrpuallW="
									+ Weight
									+ "&AppSign="
									+ HttpConn.AppSign);
					JSONArray array = new JSONObject(result.toString()).getJSONArray("Data");
					DispatchList = new ArrayList<Map<String, String>>();
					for (int i = 0; i < array.length(); i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("guid", array.getJSONObject(i).getString("Guid"));
						map.put("name", array.getJSONObject(i).getString("NAME"));
						map.put("peipr",array.getJSONObject(i).getString("peipr"));
						map.put("baopr",array.getJSONObject(i).getString("baopr"));
						DispatchList.add(map);
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

	// 选择支付类型
	public void paymentTypeDialog() {
		((RelativeLayout) findViewById(R.id.layout_type))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (PaymentTypeList.size() > 0) {
							final Dialog dialog = new Dialog(OrderPost.this,R.style.MyDialog2);
							View view = LayoutInflater.from(getBaseContext())
									.inflate(R.layout.dialog2, null);
							ListView listview = (ListView) view
									.findViewById(R.id.listview);
							SimpleAdapter adapter = new SimpleAdapter(
									getBaseContext(), PaymentTypeList,
									R.layout.city_item, new String[] { "guid",
											"name" }, new int[] { R.id.guid,
											R.id.name });
							listview.setAdapter(adapter);
							dialog.setContentView(view);
							dialog.show();

							listview.setOnItemClickListener(new OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> arg0,
										View v, int arg2, long arg3) {
									PayName = ((TextView) v
											.findViewById(R.id.name)).getText()
											.toString();
									((TextView) findViewById(R.id.text19))
											.setText(PayName);
									PayType = ((TextView) v
											.findViewById(R.id.guid)).getText()
											.toString();
									isChoose2 = true;
									dialog.dismiss();

									isChoose3 = false;
									PaymentGuid = "00000000-0000-0000-0000-000000000000";
									((TextView) findViewById(R.id.text7))
											.setText("请选择");
									((TextView) findViewById(R.id.text9))
											.setText("请选择");
									((TextView) findViewById(R.id.text11))
											.setText("");
									((TextView) findViewById(R.id.text15))
											.setText(getIntent()
													.getStringExtra(
															"ProductPrice"));
									if (isChoose1)
										getDispatch();

								}
							});
						}
					}
				});
	}

	// 选择支付方式
	public void paymentDialog() {
		((RelativeLayout) findViewById(R.id.layout_payment))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (PayName == "在线支付") {
							if (PaymentList.size() > 0) {
								final Dialog dialog = new Dialog(OrderPost.this, R.style.MyDialog2);
								View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog2, null);
								ListView listview = (ListView) view.findViewById(R.id.listview);
								SimpleAdapter adapter = new SimpleAdapter(
										getBaseContext(), PaymentList,
										R.layout.city_item, new String[] {"guid", "name" }, new int[] {
												R.id.guid, R.id.name });
								listview.setAdapter(adapter);
								dialog.setContentView(view);
								dialog.show();

								listview.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(
											AdapterView<?> arg0, View v,int arg2, long arg3) {
										String name = ((TextView) v.findViewById(R.id.name)).getText().toString();
										((TextView) findViewById(R.id.text7)).setText(name);
										PaymentGuid = ((TextView) v.findViewById(R.id.guid)).getText().toString();
										Log.i("fly", PaymentGuid);
										dialog.dismiss();
									}
								});
							}
						}
					}
				});
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:

				((LinearLayout) findViewById(R.id.layout_address))
						.setVisibility(View.GONE);
				((RelativeLayout) findViewById(R.id.layout_noaddress))
						.setVisibility(View.VISIBLE);
				break;
			case 1:
				((RelativeLayout) findViewById(R.id.layout_noaddress))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.layout_address))
						.setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.text2)).setText(NAME);
				((TextView) findViewById(R.id.text3)).setText(Mobile);
				((TextView) findViewById(R.id.text4)).setText(Address);

				getGoods();
				if (isChoose1 && isChoose2)
					getDispatch();
				break;
			case 3:
				break;
			case 4:
				pBar.dismiss();
				((RelativeLayout) findViewById(R.id.layout_score))
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								Intent intent = new Intent(getBaseContext(),
										UseScore.class);
								intent.putExtra("CanByScores", CanByScores);
								startActivityForResult(intent, 0);
							}
						});
				break;
			case 5:
				Log.i("fly", msg.obj.toString());
				pBar.dismiss();
				if (msg.obj.toString().equals("202")) {
					new Thread() {
						public void run() {
							String shoppingcartgetUrl = "/api/shoppingcartget/?loginId="
									+ HttpConn.username + "&AppSign="
									+ HttpConn.AppSign + "&agentID="+MyApplication.agentId;
							StringBuffer result = httpget
									.getArray(shoppingcartgetUrl);
							try {
								HttpConn.cartNum = new JSONObject(
										result.toString()).getJSONArray("DATA")
										.length();
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}.start();
				}
				break;
			case 6:
				pBar.dismiss();
				Toast.makeText(getApplicationContext(), "余额不足",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getBaseContext(),
						OrderStatusActivity.class);
				intent.putExtra("type", "0");
				startActivity(intent);
//				Intent intent1 = new Intent(new Intent(getApplicationContext(),
//						OrderActivity.class));
//				intent1.putExtra("type", 0);
//				intent1.putExtra("title", "全部订单");
//				startActivity(intent1);
				finish();
				break;
			case 7:
				pBar.dismiss();
				if (msg.obj.equals("true")) {
					Toast.makeText(getApplicationContext(), "付款成功",Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "付款失败",Toast.LENGTH_SHORT).show();
				}
//				Intent intent = new Intent(new Intent(getApplicationContext(),
//						OrderActivity.class));
//				intent.putExtra("type", 0);
//				intent.putExtra("title", "全部订单");
//				startActivity(intent);
				Intent intent1 = new Intent(getBaseContext(),
						OrderStatusActivity.class);
				intent1.putExtra("type", "0");
				startActivity(intent1);
				finish();
				break;
			case 8:
				actuaPrice = new DecimalFormat("0.00").format(ShouldPayPrice
						- ScorePrice);
				((TextView) findViewById(R.id.text15)).setText("￥"
						+ new DecimalFormat("0.00").format(ShouldPayPrice
								- ScorePrice));
				break;
			case 9:
				paymentTypeDialog();
				break;
			case 10:
				getPaymentType();
				break;
			case 11:
				adapter.notifyDataSetChanged();
				break;
			case 12:
				pBar.dismiss();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	public void getPrice() {
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		new Thread() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				ProductObject = new JSONObject();
				try {
					ProductObject.put("Address", Address);
					ProductObject.put("DispatchPrice",new DecimalFormat("0.00").format(peipr));
					ProductObject.put("InsurePrice",
							new DecimalFormat("0.00").format(baopr));
					ProductObject.put("DispatchModeGuid", DispatchModeGuid);
					ProductObject.put("Email", Email);
					ProductObject.put("MemLoginID", HttpConn.UserName);
					ProductObject.put("Mobile", Mobile);
					ProductObject.put("Name", NAME);
					ProductObject.put("OrderNumber", Number);
					ProductObject.put("OutOfStockOperate", "");
					ProductObject.put("PaymentGuid", PaymentGuid);
					ProductObject.put("PostType", "0");
					ProductObject.put("AgentID", MyApplication.agentId);
					ProductObject.put("PayType", PayType);
					ProductObject.put("Postalcode", Postalcode);
					ProductObject.put("ProductList", ProductList);
					ProductObject.put("ProductPrice",
							((TextView) findViewById(R.id.text5)).getText()
									.toString().substring(1));
					ProductObject.put("RegionCode", Code);
					ProductObject.put("ShouldPayPrice", new DecimalFormat(
							"0.00").format(ShouldPayPrice));
					ProductObject.put("Tel", Tel);
					ProductObject.put("orderPrice",
							new DecimalFormat("0.00").format(ShouldPayPrice));
					ProductObject.put("TradeID", Number);
					ProductObject.put("UseScore", UseScore);
					ProductObject.put("AppSign", HttpConn.AppSign);
					ProductObject.put("JoinActiveType", -1);
					ProductObject.put("ActvieContent", "");
					StringBuffer result = httpget.postJSON("/api/orderpricecaculate/",ProductObject.toString());
					CanByScores = new JSONObject(result.toString()).getJSONObject("Data").getInt("CanByScores");
					msg.what = 4;
				} catch (JSONException e) {
					msg.what = 12;
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}

	public void postData() {
		if (posted) {
			pBar = new Dialog(this, R.style.dialog);
			pBar.setContentView(R.layout.progress);
			pBar.show();

			new Thread() {
				@Override
				public void run() {
					Message msg = Message.obtain();
					msg.what = 5;
					try {
						if (fapiao_need.isChecked()) {
							ProductObject.put("InvoiceContent", et_2.getText()
									.toString());
							ProductObject.put("InvoiceTitle", et_1.getText()
									.toString());
							ProductObject.put("InvoiceType", "纸质发票");
						}

						ProductObject.put("UseScore", UseScore);
						ProductObject.put("BuyType", "9");
						ProductObject.put("ClientToSellerMsg",
								((EditText) findViewById(R.id.text13))
										.getText().toString());
						Log.i("test", ((EditText) findViewById(R.id.text13))
								.getText().toString());
						Log.i("fly", ProductObject.toString());
						StringBuffer result = httpget.postJSON(
								"/api/orderadd/", ProductObject.toString());
						msg.obj = new JSONObject(result.toString())
								.getString("return");
						posted = false;
					} catch (JSONException e) {
						posted = false;
						msg.obj = "";
						e.printStackTrace();
					}
					handler.sendMessage(msg);
				}
			}.start();
		} else {
			Toast.makeText(getApplicationContext(), "该订单已提交失败，请重新下单",
					Toast.LENGTH_SHORT).show();
		}
	}
	// 列表适配器
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.order_item3, null);
			}
			try {
				name = ProductList.getJSONObject(position).getString("Name");
				buyNumber = ProductList.getJSONObject(position).getString(
						"BuyNumber");
				buyPrice = ProductList.getJSONObject(position).getDouble(
						"BuyPrice");
				((TextView) convertView.findViewById(R.id.text1))
						.setText(ProductList.getJSONObject(position).getString(
								"Name"));
				((TextView) convertView.findViewById(R.id.text2)).setText("数量："
						+ ProductList.getJSONObject(position).getString(
								"BuyNumber"));
				((TextView) convertView.findViewById(R.id.text3))
						.setText("￥"+ new DecimalFormat("0.00").format(ProductList
										.getJSONObject(position).getDouble("BuyPrice")));
				((TextView) convertView.findViewById(R.id.goods))
						.setText(ProductList.getJSONObject(position).getString(
								"goods"));
				String imgUrl = ProductList.getJSONObject(position)
						.getString("OriginalImge").replace("\\", "");
				if (!imgUrl.startsWith("http")) {
					imgUrl = HttpConn.urlName + imgUrl;
				}
				ImageView imageview = (ImageView) convertView
						.findViewById(R.id.imageView1);
				if (HttpConn.showImage)
					ImageLoader.getInstance().displayImage(imgUrl, imageview,
							MyApplication.options);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return convertView;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case 1:
			isChoose1 = true;
			((RelativeLayout) findViewById(R.id.layout_noaddress))
					.setVisibility(View.GONE);
			((LinearLayout) findViewById(R.id.layout_address))
					.setVisibility(View.VISIBLE);
			NAME = data.getStringExtra("name");
			Mobile = data.getStringExtra("mobile");
			Address = data.getStringExtra("address");
			Code = data.getStringExtra("code");
			Guid = data.getStringExtra("guid");
			((TextView) findViewById(R.id.text2)).setText(NAME);
			((TextView) findViewById(R.id.text3)).setText(Mobile);
			((TextView) findViewById(R.id.text4)).setText(Address);

			isSaled = true;
			getGoods();

			if (isChoose1 && isChoose2)
				getDispatch();
			break;
		case 2:
			isSaled = true;
			getAddress();
			break;
		case 3:
			CanByScores = data.getIntExtra("CanByScores", 0);
			UseScore = data.getIntExtra("UseScore", 0);
			((TextView) findViewById(R.id.UseScore)).setText("" + UseScore);
			scorePrice(UseScore);
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void scorePrice(final int UseScore) {
		new Thread() {
			public void run() {
				StringBuffer result = httpget
						.getArray("/api/scoreprice/?Score=" + UseScore
								+ "&AppSign=" + HttpConn.AppSign);
				Log.i("fly", result.toString());
				try {
					ScorePrice = new JSONObject(result.toString())
							.getDouble("ScorePrice");
					Message msg = Message.obtain();
					msg.what = 8;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	@Override
	public void onBackPressed() {
		setResult(1, getIntent());
		finish();
		super.onBackPressed();
	}

}
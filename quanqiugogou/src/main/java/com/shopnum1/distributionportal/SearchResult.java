package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.adater.SimpleTreeListViewAdapter;
import com.shopnum1.distributionportal.util.FileBean;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.Node;
import com.shopnum1.distributionportal.util.OrgBean;
import com.shopnum1.distributionportal.util.TreeListViewAdapter.OnTreeNodeClickListener;
//商品搜索结果
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SearchResult extends Activity {
	public static final String TAG = "SearchResult";
	private SimpleTreeListViewAdapter mAdapter = null;
	private HttpConn httpget = new HttpConn();
	private String searchstr; // 接口参数
	private int typeid; // 接口参数
	private String productCategoryID = "";
	private String sorts = "SaleNumber"; // 排序方式
	private Boolean isASC = true; // 是否升序
	private static ImageAdapter adapter1; // 商品适配器
	private static ImageAdapter adapter2; // 商品适配器
	private Dialog pBar; // 加载进度
	private List<OrgBean> mDatas2;
	private List<FileBean> mDatas;
	ListView elv;
	private String[] select = { "", "" };// 用于传递到服务器
	private String[] select2 = { "", "" };// 用于标记
	private TextView tv_confirm;
	private TextView tv_cancel;
	private TextView tv_filter;
	
	private DrawerLayout drawer_layout;
	private int width;
	long minNum = 0;// 最小值
	long maxNum = Long.MAX_VALUE;// 最大值

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_result);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;// 宽度
		elv = (ListView) findViewById(R.id.elv);
		// 接收参数
		typeid = getIntent().getIntExtra("typeid", 0);
		productCategoryID = getIntent().getStringExtra("ProductCategoryID");
		select[0] = typeid + "";// 获取传过来的TypeId
		searchstr = getIntent().getStringExtra("searchstr");// 获取得到的搜索字符串
		// 返回
		((ImageView) findViewById(R.id.back))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						finish();
					}
				});
		// 加载列表
		addList();
		getData();
		drawer_layout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
		tv_confirm = (TextView) this.findViewById(R.id.tv_confirm);
		tv_cancel = (TextView) this.findViewById(R.id.tv_cancel);
		tv_filter = (TextView) this.findViewById(R.id.tv_filter);
		tv_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				drawer_layout.closeDrawers();
			}
		});
		tv_confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				drawer_layout.closeDrawers();
				if (mAdapter.getMin() == 0
						&& mAdapter.getMax() == Long.MAX_VALUE) {
				} else {
					minNum = mAdapter.getMin();
					maxNum = mAdapter.getMax();
				}
				addList();
			}
		});
	
	tv_filter.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			drawer_layout.closeDrawers();
			if (mAdapter.getMin() == 0
					&& mAdapter.getMax() == Long.MAX_VALUE) {
			} else {
				minNum = mAdapter.getMin();
				maxNum = mAdapter.getMax();
			}
			addList();
		}
	});
}
	
	public void getData() {
		mDatas = new ArrayList<FileBean>();// 显示
		mDatas2 = new ArrayList<OrgBean>();// 点击

		HttpUtils hu = new HttpUtils();
		String productCatagoryUrl =  HttpConn.hostName + "/api/productcatagory/?id="
				+ 0 + "&AppSign=" + HttpConn.AppSign
    			+"&AgentID="+MyApplication.agentId +"&sbool=true";
		hu.send(HttpMethod.GET, productCatagoryUrl,
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(getApplicationContext(), "连接网络失败", 1000)
								.show();
					}

					@Override
					public void onSuccess(ResponseInfo<String> data) {
						FileBean fileBean = new FileBean(1, 0, "价格", 0, 0,
								searchstr);
						mDatas.add(fileBean);
						OrgBean orgBean = new OrgBean(1, 0, "价格", 0, 0,
								searchstr);
						mDatas2.add(orgBean);
						// --------------
						fileBean = new FileBean(2, 1, "0-" + Long.MAX_VALUE, 1,
								0, searchstr);
						mDatas.add(fileBean);

						orgBean = new OrgBean(2, 1, "全部价格", 1, 0, searchstr);
						mDatas2.add(orgBean);
						// --------------
						fileBean = new FileBean(3, 1, "0-199", 1, 0, searchstr);
						mDatas.add(fileBean);

						orgBean = new OrgBean(3, 1, "0-199", 1, 0, searchstr);
						mDatas2.add(orgBean);
						// --------------
						fileBean = new FileBean(4, 1, "200-399", 1, 0,
								searchstr);
						mDatas.add(fileBean);

						orgBean = new OrgBean(4, 1, "200-399", 1, 0, searchstr);
						mDatas2.add(orgBean);
						// --------------
						fileBean = new FileBean(5, 1, "400-699", 1, 0,
								searchstr);
						mDatas.add(fileBean);

						orgBean = new OrgBean(5, 1, "400-699", 1, 0, searchstr);
						mDatas2.add(orgBean);
						// --------------
						fileBean = new FileBean(6, 1, "700-1099", 1, 0,
								searchstr);
						mDatas.add(fileBean);

						orgBean = new OrgBean(6, 1, "700-1099", 1, 0, searchstr);
						mDatas2.add(orgBean);
						// --------------
						fileBean = new FileBean(7, 1, "1700-1799", 1, 0,
								searchstr);
						mDatas.add(fileBean);

						orgBean = new OrgBean(7, 1, "1700-1799", 1, 0,
								searchstr);
						mDatas2.add(orgBean);
						// --------------
						fileBean = new FileBean(8, 1, "最低价格", 2, 0, searchstr);
						mDatas.add(fileBean);

						orgBean = new OrgBean(8, 1, "最低价格", 2, 0, searchstr);
						mDatas2.add(orgBean);
						// --------------

						fileBean = new FileBean(500, 0, "分类", 0, 0, searchstr);
						mDatas.add(fileBean);

						orgBean = new OrgBean(500, 0, "分类", 0, 0, searchstr);
						mDatas2.add(orgBean);

						fileBean = new FileBean(501, 500, "0", 1, 0, searchstr);
						mDatas.add(fileBean);

						orgBean = new OrgBean(501, 500, "全部分类", 1, 0, searchstr);
						mDatas2.add(orgBean);

						try {
							JSONObject jo = new JSONObject(data.result
									.toString());
							JSONArray ja = jo.getJSONArray("Data");
							for (int i = 0; i < ja.length(); i++) {
								fileBean = new FileBean(502 + i, 500, ja
										.getJSONObject(i).getString("ID"), 1,
										0, searchstr);
								mDatas.add(fileBean);
								orgBean = new OrgBean(502 + i, 500, ja
										.getJSONObject(i).getString("Name"), 1,
										0, searchstr);
								mDatas2.add(orgBean);
							}
							getPai();
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						}
					}
				});
	}

	// 点击事件
	private void initEvent() {
		mAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
			@Override
			public void onClick(Node node, int position, View v) {
				if (node.getpId() == 1) {
					for (FileBean item : mDatas) {
						if (item.getId() == node.getId()) {
							String[] zone = item.getLabel().split("-");
							minNum = Long.parseLong(zone[0]);
							maxNum = Long.parseLong(zone[1]);
							mAdapter.ed_min.setText("");
							mAdapter.ed_max.setText("");
						}
					}
				} else if (node.getpId() == 500) {
					for (FileBean item : mDatas) {
						if (item.getId() == node.getId()) {
							select[0] = item.getLabel();
						}
					}
					select2[0] = node.getName();
				} else if (node.getpId() == 1000) {
					for (FileBean item : mDatas) {
						if (item.getId() == node.getId()) {
							select[1] = item.getLabel();
						}
					}
					select2[1] = node.getName();
				}
				try {
					if (mAdapter == null) {
						mAdapter = new SimpleTreeListViewAdapter<OrgBean>(elv,
								SearchResult.this, mDatas2, 0);
						elv.setAdapter(mAdapter);
					} else {
						mAdapter.setMin(minNum);
						Log.i("test", minNum + "---");
						Log.i("test", maxNum + "+++");
						mAdapter.setMax(maxNum);
						mAdapter.setMin(minNum);
						mAdapter.setTag(select2);
						mAdapter.notifyDataSetChanged();
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		});
	}

	// 加载列表
	public void addList() {
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		// 显示列表
		final ListView listview = (ListView) findViewById(R.id.listview);
		adapter1 = new ImageAdapter(1);
		listview.setAdapter(adapter1);
		// 商品详情
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int i,
					long arg3) {
				try {
					Intent intent = new Intent(getApplicationContext(),
							ProductDetails.class);
					intent.putExtra("guid",
							adapter1.getInfo(i).getString("Guid"));
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		// 显示大图
		final GridView gridview = (GridView) findViewById(R.id.gridview);
		gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter2 = new ImageAdapter(2);
		gridview.setAdapter(adapter2);
		// 商品详情
		gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int i,
					long arg3) {
				try {
					Intent intent = new Intent(getApplicationContext(),
							ProductDetails.class);
					intent.putExtra("guid",
							adapter2.getInfo(i).getString("Guid"));
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		// 筛选
		((LinearLayout) findViewById(R.id.filter))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
						mDrawerLayout
								.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
						mDrawerLayout.openDrawer(Gravity.RIGHT);
					}
				});
		((LinearLayout) findViewById(R.id.linearLayout4))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (listview.isShown()) {
							listview.setVisibility(View.INVISIBLE);
							gridview.setVisibility(View.VISIBLE);
							((TextView) findViewById(R.id.tv_switch))
									.setText("大图");
							((ImageView) findViewById(R.id.iv_swtich))
									.setImageResource(R.drawable.bigimage);

						} else {
							gridview.setVisibility(View.INVISIBLE);
							listview.setVisibility(View.VISIBLE);
							((TextView) findViewById(R.id.tv_switch))
									.setText("列表");
							((ImageView) findViewById(R.id.iv_swtich))
									.setImageResource(R.drawable.list_image);
						}
					}
				});

		// 综合排序
		((LinearLayout) findViewById(R.id.linearLayout1))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						((ImageView) findViewById(R.id.img1))
								.setVisibility(View.VISIBLE);
						((ImageView) findViewById(R.id.arrow1))
								.setVisibility(View.VISIBLE);
						((ImageView) findViewById(R.id.img2))
								.setVisibility(View.INVISIBLE);
						((ImageView) findViewById(R.id.arrow2))
								.setVisibility(View.INVISIBLE);
						((ImageView) findViewById(R.id.img3))
								.setVisibility(View.INVISIBLE);
						((ImageView) findViewById(R.id.arrow3))
								.setVisibility(View.INVISIBLE);
						sorts = "SaleNumber";
						if (isASC) {
							isASC = false;
							((ImageView) findViewById(R.id.arrow1))
									.setBackgroundResource(R.drawable.sort_down);
						} else {
							isASC = true;
							((ImageView) findViewById(R.id.arrow1))
									.setBackgroundResource(R.drawable.sort_up);
						}
						addList();
					}
				});
		// 销量排序
		((LinearLayout) findViewById(R.id.linearLayout2))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						((ImageView) findViewById(R.id.img2))
								.setVisibility(View.VISIBLE);
						((ImageView) findViewById(R.id.arrow2))
								.setVisibility(View.VISIBLE);
						((ImageView) findViewById(R.id.arrow1))
								.setVisibility(View.INVISIBLE);
						((ImageView) findViewById(R.id.img1))
								.setVisibility(View.INVISIBLE);
						((ImageView) findViewById(R.id.img3))
								.setVisibility(View.INVISIBLE);
						((ImageView) findViewById(R.id.arrow3))
								.setVisibility(View.INVISIBLE);
						sorts = "SaleNumber";
						if (isASC) {
							isASC = false;
							((ImageView) findViewById(R.id.arrow2))
									.setBackgroundResource(R.drawable.sort_down);
						} else {
							isASC = true;
							((ImageView) findViewById(R.id.arrow2))
									.setBackgroundResource(R.drawable.sort_up);
						}
						addList();
					}
				});
		// 价格排序
		((LinearLayout) findViewById(R.id.linearLayout3))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						((ImageView) findViewById(R.id.img3))
								.setVisibility(View.VISIBLE);
						((ImageView) findViewById(R.id.arrow3))
								.setVisibility(View.VISIBLE);
						((ImageView) findViewById(R.id.img1))
								.setVisibility(View.INVISIBLE);
						((ImageView) findViewById(R.id.arrow1))
								.setVisibility(View.INVISIBLE);
						((ImageView) findViewById(R.id.img2))
								.setVisibility(View.INVISIBLE);
						((ImageView) findViewById(R.id.arrow2))
								.setVisibility(View.INVISIBLE);
						sorts = "Price";
						if (isASC) {
							isASC = false;
							((ImageView) findViewById(R.id.arrow3))
									.setBackgroundResource(R.drawable.sort_down);
						} else {
							isASC = true;
							((ImageView) findViewById(R.id.arrow3))
									.setBackgroundResource(R.drawable.sort_up);
						}
						addList();
					}
				});
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				adapter1.notifyDataSetChanged();
				adapter2.notifyDataSetChanged();
				((LinearLayout) findViewById(R.id.tab))
						.setVisibility(View.VISIBLE);
				((LinearLayout) findViewById(R.id.nocontent))
						.setVisibility(View.GONE);
			}
			if (msg.what == 2) { // 搜索为空
				pBar.dismiss();
				((LinearLayout) findViewById(R.id.tab))
						.setVisibility(View.GONE);
				((LinearLayout) findViewById(R.id.nocontent))
						.setVisibility(View.VISIBLE);
			}
			super.handleMessage(msg);
		}

	};

	// 商品适配器
	public class ImageAdapter extends BaseAdapter {

		private JSONArray companyList = new JSONArray();
		private int id;

		public JSONObject getInfo(int position) {
			JSONObject result = null;
			try {
				result = companyList.getJSONObject(position);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}

		public ImageAdapter(int id) {
			this.id = id;
			HttpUtils httpUtils =new HttpUtils();
			
			
			String filterrUrl = HttpConn.hostName+"/api/product/filter/" + select[0]+ "/?" + "&sorts=" + sorts + "&isASC="
					+ isASC + "&pageIndex=1&pageCount=200"
					+"&ProductCategoryID="+productCategoryID
					+ "&keywords=" + searchstr
					+ "&AppSign=" + HttpConn.AppSign
					+ "&minPrice=" + (minNum)
					+ "&maxPrice=" + maxNum + "&brand="
					+ select[1]
					+ "&Sbool=true"
					+ "&AgentID="+MyApplication.agentId;
	
			Log.i(TAG, "filterrUrl = "+filterrUrl);
			httpUtils.send(HttpMethod.GET, filterrUrl, new RequestCallBack<String>() {

				@Override
				public void onFailure(HttpException arg0, String arg1) {
					((LinearLayout) findViewById(R.id.tab))
							.setVisibility(View.GONE);
					((LinearLayout) findViewById(R.id.nocontent))
							.setVisibility(View.VISIBLE);
				}

				@Override
				public void onSuccess(ResponseInfo<String> arg0) {
					try {
						companyList = new JSONObject(arg0.result.toString()).getJSONArray("Data");
						if (companyList.length() > 0) {
							adapter1.notifyDataSetChanged();
							adapter2.notifyDataSetChanged();
							((LinearLayout) findViewById(R.id.tab))
									.setVisibility(View.VISIBLE);
							((LinearLayout) findViewById(R.id.nocontent))
									.setVisibility(View.GONE);
						}else{ // 搜索为空
							pBar.dismiss();
							((LinearLayout) findViewById(R.id.tab))
									.setVisibility(View.GONE);
							((LinearLayout) findViewById(R.id.nocontent))
									.setVisibility(View.VISIBLE);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
//			new Thread() {
//				@Override
//				public void run() {
//					Message message = Message.obtain();
//					try {
//						StringBuffer result = httpget
//								.getArray("/api/product/filterr/" + select[0]
//										+ "/?" + "&sorts=" + sorts + "&isASC="
//										+ isASC + "&pageIndex=1&pageCount=200"
//										+ "&keywords=" + searchstr
//										+ "&AppSign=" + HttpConn.AppSign
//										+ "&minPrice=" + (minNum)
//										+ "&maxPrice=" + maxNum + "&brand="
//										+ select[1]);
//						if (result != null && !("").equals(result)
//								&& !("null").equals(result)) {
//							companyList = new JSONObject(result.toString())
//									.getJSONArray("Data");
//							if (companyList.length() > 0) {
//								message.what = 1;
//							} else {
//								message.what = 2;
//							}
//						} else {
//							message.what = 2;
//						}
//					} catch (JSONException e) {
//						message.what = 2;
//						e.printStackTrace();
//					}
//					handler.sendMessage(message);
//				}
//			}.start();
		}

		@Override
		public int getCount() {
			return companyList.length();
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int arg1) {
			return arg1;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (pBar.isShowing())
				pBar.dismiss();
			if (convertView == null) {
				if (id == 1)
					convertView = LayoutInflater.from(getApplicationContext())
							.inflate(R.layout.product_item, null);
				else
					convertView = LayoutInflater.from(getApplicationContext())
							.inflate(R.layout.product_item2, null);
				holder = new ViewHolder();
				holder.text1 = (TextView) convertView
						.findViewById(R.id.textView1);
				holder.text2 = (TextView) convertView
						.findViewById(R.id.textView2);
				holder.text3 = (TextView) convertView
						.findViewById(R.id.textView3);
				holder.imageview = (ImageView) convertView
						.findViewById(R.id.imageView1);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			try {
				holder.text1.setText(companyList.getJSONObject(position)
						.getString("Name"));
				holder.text2.setText("￥"
						+ new DecimalFormat("0.00")
								.format(companyList.getJSONObject(position)
										.getDouble("ShopPrice")));
				holder.text3.setText("￥"
						+ new DecimalFormat("0.00").format(companyList
								.getJSONObject(position).getDouble(

								"MarketPrice")));
				if (id == 2) {
					LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) holder.imageview
							.getLayoutParams();
					linearParams.height = (width - dip2px(SearchResult.this, 15)) / 2;
					holder.imageview.setLayoutParams(linearParams);
				}
				if (HttpConn.showImage)
					ImageLoader.getInstance().displayImage(
							companyList.getJSONObject(position).getString(
									"OriginalImge"), holder.imageview,
							MyApplication.options);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return convertView;
		}
	}

	static class ViewHolder {
		ImageView imageview;
		TextView text1, text2, text3;
	}

	private void getPai() {
		// TODO Auto-generated method stub
		HttpUtils hu = new HttpUtils();
		hu.send(HttpMethod.GET, HttpConn.hostName
				+ "/api/productbrandlistisrecommend/?IsRecommend=1&AppSign="
				+ HttpConn.AppSign, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {

			}

			@Override
			public void onSuccess(ResponseInfo<String> data) {
				FileBean fileBean = new FileBean(1000, 0, "品牌", 0, 0, searchstr);
				mDatas.add(fileBean);
				OrgBean orgBean = new OrgBean(1000, 0, "品牌", 0, 0, searchstr);
				mDatas2.add(orgBean);
				fileBean = new FileBean(1001, 1000, "", 1, 0, searchstr);
				mDatas.add(fileBean);
				orgBean = new OrgBean(1001, 1000, "全部品牌", 1, 0, searchstr);
				mDatas2.add(orgBean);
				JSONObject jo;
				try {
					jo = new JSONObject(data.result.toString());
					JSONArray ja = jo.getJSONArray("data");
					for (int i = 0; i < ja.length(); i++) {
						fileBean = new FileBean(1002 + i, 1000, ja
								.getJSONObject(i).getString("Name"), 1, 0,
								searchstr);
						mDatas.add(fileBean);
						orgBean = new OrgBean(1002 + i, 1000, ja.getJSONObject(
								i).getString("Name"), 1, 0, searchstr);
						mDatas2.add(orgBean);
					}
					mAdapter = new SimpleTreeListViewAdapter<OrgBean>(elv,
							SearchResult.this, mDatas2, 0);
					elv.setAdapter(mAdapter);
					initEvent();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
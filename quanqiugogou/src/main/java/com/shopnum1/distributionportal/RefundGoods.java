package com.shopnum1.distributionportal;
//申请退货
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
public class RefundGoods extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private ListAdapter adapter;
	private ArrayList<JSONObject> refundList = new ArrayList<JSONObject>();
	private Dialog pBar; //加载进度

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.refund_goods);
		initLayout();
	}
	//初始化
	public void initLayout() {
		
		//返回
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		//快捷方式
		((LinearLayout)findViewById(R.id.more)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				httpget.showMenu(RefundGoods.this, findViewById(R.id.refund_goods));
			}
		});
		
		try {
			JSONArray ProductList = new JSONArray(getIntent().getStringExtra("ProductList"));
			orderList = new JSONObject(getIntent().getStringExtra("orderList"));
			ListView listview = (ListView)findViewById(R.id.listview);
			adapter = new ListAdapter(ProductList);
			listview.setAdapter(adapter);
			
			((Button)findViewById(R.id.button)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					final JSONArray refundArray = new JSONArray();
					for (int i = 0; i < refundList.size(); i++) {
						refundArray.put(refundList.get(i));
					}
					if(refundArray.length() == 0){
						Toast.makeText(getApplicationContext(), "请选择商品", Toast.LENGTH_SHORT).show();
					} else if(((EditText)findViewById(R.id.edit)).getText().toString().equals("")){
						Toast.makeText(getApplicationContext(), "请输入退货原因", Toast.LENGTH_SHORT).show();
					} else {
						pBar = new Dialog(RefundGoods.this, R.style.dialog);
						pBar.setContentView(R.layout.progress);
						pBar.show();
						
						new Thread(){
							public void run(){
								JSONObject object = new JSONObject();
								try {
									object.put("OrderGuid", getIntent().getStringExtra("Guid"));
									object.put("OrderStatus", 0);
									object.put("ApplyUserID", HttpConn.UserName);
									object.put("OperateUserID", HttpConn.UserName);
									object.put("ReturnGoodsCause", ((EditText)findViewById(R.id.edit)).getText().toString());
									object.put("AppSign", HttpConn.AppSign);
									object.put("AgentID", MyApplication.agentId);
									Log.i("fly", refundArray.toString());
									JSONArray array = new JSONArray();
									for (int i = 0; i < refundArray.length(); i++) {
										JSONObject obj = new JSONObject();
										obj.put("OrderGuid", getIntent().getStringExtra("Guid"));
										obj.put("OrderType", getIntent().getStringExtra("OderStatus"));
										obj.put("ProductGuid", refundArray.getJSONObject(i).getString("ProductGuid"));
										obj.put("ProductImage", refundArray.getJSONObject(i).getString("OriginalImge"));
										obj.put("ReturnCount", refundArray.getJSONObject(i).getString("BuyNumber"));
										obj.put("Attributes", refundArray.getJSONObject(i).getString("Attributes"));
										obj.put("BuyPrice", Double.parseDouble(refundArray.getJSONObject(i).getString("BuyPrice"))
												- orderList.getDouble("ScorePrice"));
										array.put(obj);
									}
									object.put("GoodSList", array.toString());
								} catch (JSONException e) {
									e.printStackTrace();
								}
								
								String JsonStr = object.toString().replace("/", "").replace("\\", "").replace("\"[", "[").replace("]\"", "]");
								Log.i("fly", JsonStr);
								StringBuffer result = httpget.postJSON("/api/addreturnorder/", JsonStr);
								try {
									Message msg = Message.obtain();
									msg.obj = new JSONObject(result.toString()).getString("return");
									msg.what = 1;
									handler.sendMessage(msg);
								} catch (JSONException e) {
									e.printStackTrace();
								}
								
							}
						}.start();
					}
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				pBar.dismiss();
				if(msg.obj.equals("202")){
					Toast.makeText(getApplicationContext(), "申请成功", Toast.LENGTH_SHORT).show();	
				} else {
					Toast.makeText(getApplicationContext(), "申请失败", Toast.LENGTH_SHORT).show();
				}
				setResult(1, getIntent());
				finish();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	private String realityPay;
	private JSONObject orderList;
	
	class ListAdapter extends BaseAdapter{
		
		private HashMap<Integer,Boolean> isSelected;
		private HashMap<Integer,Integer> BuyNumber;
		private JSONArray ProductList;
		
		public ListAdapter(JSONArray ProductList){
			this.ProductList = ProductList;
			isSelected = new HashMap<Integer, Boolean>();
			BuyNumber = new HashMap<Integer, Integer>();
			
			for(int i = 0; i < ProductList.length(); i++) {
				getIsSelected().put(i, false);
	            try {
					getBuyNumber().put(i, ProductList.getJSONObject(i).getInt("BuyNumber"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
	        }
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
			ViewHolder holder = null;
			if(convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_cart, null);
				holder.check = (CheckBox)convertView.findViewById(R.id.check);
				holder.text1 = (TextView)convertView.findViewById(R.id.text1);
				holder.imageview = (ImageView)convertView.findViewById(R.id.imageView1);
				holder.text2 = (TextView)convertView.findViewById(R.id.text2);
				holder.text3 = (TextView)convertView.findViewById(R.id.text3);
				holder.text4 = (TextView)convertView.findViewById(R.id.text4);
				holder.text5 = (TextView)convertView.findViewById(R.id.text5);
				holder.text6 = (TextView)convertView.findViewById(R.id.text6);
				holder.text7 = (TextView)convertView.findViewById(R.id.text7);
				holder.limitnum = (TextView)convertView.findViewById(R.id.guid);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			try {
				holder.check.setChecked(getIsSelected().get(position));
				holder.text1.setText(ProductList.getJSONObject(position).getString("NAME"));
				holder.text2.setHint(ProductList.getJSONObject(position).getString("DetailedSpecifications"));
				//holder.text3.setText("￥" + new DecimalFormat("0.00").format(ProductList.getJSONObject(position).getDouble("BuyPrice")));
				holder.text3.setText("￥"
						+ new DecimalFormat("0.00").format(orderList.getDouble("ProductPrice")
								+ orderList.getDouble("DispatchPrice")
								- orderList.getDouble("ScorePrice")));
				holder.text5.setText(getBuyNumber().get(position)+"");
				holder.text7.setText(ProductList.getJSONObject(position).getString("Guid"));
				holder.limitnum.setText(ProductList.getJSONObject(position).getString("BuyNumber"));
				if(HttpConn.showImage)
					ImageLoader.getInstance().displayImage(ProductList.getJSONObject(position).getString("OriginalImge"), holder.imageview, MyApplication.options);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			final CheckBox check = holder.check;
			final TextView text5 = holder.text5;
			check.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(check.isChecked()) {
						try {
							getBuyNumber().put(position, Integer.parseInt(text5.getText().toString()));
							ProductList.getJSONObject(position).put("BuyNumber", getBuyNumber().get(position));
							refundList.add(ProductList.getJSONObject(position));
						} catch (JSONException e1) {
							e1.printStackTrace();
						}		
					}
					else {
						remove(position);	
					}
					adapter.getIsSelected().put(position, check.isChecked()); 
				}
			});
			
			final TextView buynum = holder.text5;
			final TextView limitnum = holder.limitnum;
			
			holder.text4.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					int i = Integer.parseInt(buynum.getText().toString());
					if(i > 1){
						try {
							buynum.setText((i - 1) + "");
							ProductList.getJSONObject(position).put("BuyNumber", i - 1);
							adapter.getBuyNumber().put(position, i - 1); 
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
					}
				}
			});
			
			holder.text5.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					final Dialog dialog = new Dialog(RefundGoods.this, R.style.MyDialog);
					final View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog4, null);
					dialog.setContentView(view);
					dialog.show();
					
					final EditText editText = (EditText)view.findViewById(R.id.edit);
					String num = buynum.getText().toString();
					editText.setText(num);
					editText.setSelection(num.length());
					
					((Button)view.findViewById(R.id.no)).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							dialog.dismiss();
						}
					});
					
					((Button)view.findViewById(R.id.yes)).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							if(!editText.getText().toString().equals("")){
								int i = Integer.parseInt(editText.getText().toString());
								int j = Integer.parseInt(limitnum.getText().toString());
								if(i > j){
									i = j;
								}
								buynum.setText(i+"");
								try {
									ProductList.getJSONObject(position).put("BuyNumber", i);
									adapter.getBuyNumber().put(position, i); 
								} catch (JSONException e) {
									e.printStackTrace();
								}
								dialog.dismiss();
							}
							
						}
					});
				}
			});
			
			holder.text6.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					try {
						int i = Integer.parseInt(buynum.getText().toString());
						if(i < Integer.parseInt(limitnum.getText().toString())){
							buynum.setText((i + 1) + "");
							ProductList.getJSONObject(position).put("BuyNumber", i + 1);
							adapter.getBuyNumber().put(position, i + 1); 
						} else {
							Toast.makeText(getApplicationContext(), "最多只能退" + i + "件", Toast.LENGTH_SHORT).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
			
			return convertView;
		}
		
		public void remove(int position){
			try {
				refundList.remove(ProductList.getJSONObject(position));
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
		public HashMap<Integer,Boolean> getIsSelected() {
	        return isSelected;
	    }
		
		public HashMap<Integer,Integer> getBuyNumber() {
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
	    	TextView limitnum;
		}
		
	}

}
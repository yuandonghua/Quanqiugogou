package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
import com.shopnum1.distributionportal.util.NoScrollGridView;
import com.shopnum1.distributionportal.util.RoundImageView;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class AssessShowActivity extends Activity {
	private HttpConn httpget = new HttpConn();
	private Dialog pBar; //加载进度
	private int num;//评价
	JSONArray jsondata;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_assess_show);
		initLayout();
	}
	//初始化
	public void initLayout() {
		String ProductList = getIntent().getStringExtra("ProductList");
		try {
			jsondata = new JSONArray(ProductList);
			ListView lv = (ListView) findViewById(R.id.lv);
			lv.setAdapter(new MyAdapter(jsondata));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		//返回
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		 
		((LinearLayout)findViewById(R.id.more)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg1) {
				
				for(int x=0;x<jsondata.length();x++){
					JSONObject product = jsondata.optJSONObject(x);
					if(product.optString("Rank").equals("")||product.optString("Content").equals("")){
						Toast.makeText(AssessShowActivity.this, "请完善信息", Toast.LENGTH_LONG).show();
						return ;
					}
				}
				
					pBar = new Dialog(AssessShowActivity.this, R.style.dialog);
					pBar.setContentView(R.layout.progress);
					pBar.show();
					
					new Thread(){
						public void run(){
							String urlStr = "";
							Log.i("fly", urlStr);
							Message msg = Message.obtain();
							StringBuffer result = null ;
							try {
								
								
								for(int k=0;k<jsondata.length();k++){
									JSONObject product = jsondata.optJSONObject(k);
									JSONObject object = new JSONObject();
									object.put("MemLoginID", HttpConn.UserName);
									object.put("ProductGuid", product.getString("ProductGuid"));
									object.put("OrderNumber", product.getString("OrderNumber"));
									object.put("Name", product.getString("NAME"));
									object.put("Content", product.getString("Content"));
									object.put("Rank", product.getString("Rank"));
									object.put("AppSign", HttpConn.AppSign);				
									result= httpget.postJSON("/api/addProductComment", object.toString());
								}
								
								msg.obj = new JSONObject(result.toString()).getString("return");
								msg.what = 1;
								handler.sendMessage(msg);
							} catch (JSONException e) {
								msg.what = 0;
								e.printStackTrace();
							}	
						}
					}.start();
				
			}
		});
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				pBar.dismiss();
				break;
			case 1:
				pBar.dismiss();
				if(msg.obj.equals("202")){
					Toast.makeText(getApplicationContext(), "发表成功", Toast.LENGTH_SHORT).show();	
				} 
				else if(msg.obj.equals("404")){
					Toast.makeText(getApplicationContext(), "已有一条评价记录", Toast.LENGTH_SHORT).show();
				} 
				else {
					Toast.makeText(getApplicationContext(), "发表失败", Toast.LENGTH_SHORT).show();
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
	private class MyAdapter extends BaseAdapter{
		JSONArray jsonObject;
		public MyAdapter(JSONArray jsonObject) {
			this.jsonObject = jsonObject;
		}

		@Override
		public int getCount() {
			return jsonObject.length();
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder vh;
			final ImageView miv1,miv2,miv3,miv4,miv5;
			final EditText text2;
			if(convertView == null){
				vh = new ViewHolder();
				convertView = View.inflate(AssessShowActivity.this, R.layout.assess_item, null);
				vh.product_name= (TextView)convertView.findViewById(R.id.product_name);
				vh.product_price=(TextView) convertView.findViewById(R.id.product_price);
				vh.product_number=(TextView)convertView.findViewById(R.id.product_number);
				vh.assess_image=(ImageView)convertView.findViewById(R.id.assess_image);
				vh. iv1 = (ImageView)convertView. findViewById(R.id.iv1);
				vh. iv2 = (ImageView) convertView.findViewById(R.id.iv2);
				vh. iv3 = (ImageView)convertView. findViewById(R.id.iv3);
				vh. iv4 = (ImageView)convertView. findViewById(R.id.iv4);
				vh. iv5 = (ImageView)convertView. findViewById(R.id.iv5);
				vh. text2=(EditText) convertView.findViewById(R.id.text2);
				convertView.setTag(vh);
			}else{
				vh = (ViewHolder) convertView.getTag();
			}
			
			miv1=vh. iv1;
			miv2=vh. iv2;
			miv3=vh. iv3;
			miv4=vh. iv4;
			miv5=vh. iv5;
			text2=vh. text2;
			vh.product_name.setText(jsonObject.optJSONObject(position).optString("NAME"));
			vh.product_price.setText("￥"+ new DecimalFormat("0.00").format(jsonObject.optJSONObject(position).optDouble("BuyPrice")));
			vh.product_number.setText("x"+jsonObject.optJSONObject(position).optInt("BuyNumber"));
			if (HttpConn.showImage)ImageLoader.getInstance().displayImage(jsonObject.optJSONObject(position).optString("OriginalImge"), vh.assess_image,MyApplication.options);
			vh.iv1.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					miv1.setImageResource(R.drawable.red_star);
					miv2.setImageResource(R.drawable.gray_star);
					miv3.setImageResource(R.drawable.gray_star);
					miv4.setImageResource(R.drawable.gray_star);
					miv5.setImageResource(R.drawable.gray_star);
					num = 1;
					try {
						jsonObject.optJSONObject(position).put("Rank", num);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
			});
			vh. iv2.setOnClickListener(new OnClickListener() {
				 
				 @Override
				 public void onClick(View v) {
					 num = 2;
					 miv1.setImageResource(R.drawable.red_star);
					 miv2.setImageResource(R.drawable.red_star);
					 miv3.setImageResource(R.drawable.gray_star);
					 miv4.setImageResource(R.drawable.gray_star);
					 miv5.setImageResource(R.drawable.gray_star);
					 try {
						jsonObject.optJSONObject(position).put("Rank", num);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 
				 }
			 });
			vh. iv3.setOnClickListener(new OnClickListener() {
				 
				 @Override
				 public void onClick(View v) {
					 num = 3;
					 miv1.setImageResource(R.drawable.red_star);
					 miv2.setImageResource(R.drawable.red_star);
					 miv3.setImageResource(R.drawable.red_star);
					 miv4.setImageResource(R.drawable.gray_star);
					 miv5.setImageResource(R.drawable.gray_star);
					 try {
						jsonObject.optJSONObject(position).put("Rank", num);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 
				 }
			 });
			vh. iv4.setOnClickListener(new OnClickListener() {
				 
				 @Override
				 public void onClick(View v) {
					 num = 4;
					 miv1.setImageResource(R.drawable.red_star);
					 miv2.setImageResource(R.drawable.red_star);
					 miv3.setImageResource(R.drawable.red_star);
					 miv4.setImageResource(R.drawable.red_star);
					 miv5.setImageResource(R.drawable.gray_star);
					 try {
						jsonObject.optJSONObject(position).put("Rank", num);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 
				 }
			 });
			vh.iv5.setOnClickListener(new OnClickListener() {
				 
				 @Override
				 public void onClick(View v) {
					 num = 5;
					 miv1.setImageResource(R.drawable.red_star);
					 miv2.setImageResource(R.drawable.red_star);
					 miv3.setImageResource(R.drawable.red_star);
					 miv4.setImageResource(R.drawable.red_star);
					 miv5.setImageResource(R.drawable.red_star);
					 try {
						jsonObject.optJSONObject(position).put("Rank", num);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 
				 }
			 });
			vh. text2.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
						int arg3) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterTextChanged(Editable arg0) {
					// TODO Auto-generated method stub
					try {
						jsonObject.optJSONObject(position).put("Content", text2.getText().toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			return convertView;
		}
		
	}
	
	static class ViewHolder {
		private TextView product_name;
		private TextView product_price;
		private TextView product_number;
		private ImageView assess_image;
		private ImageView iv1,iv2,iv3,iv4,iv5;
		private EditText text2;
		
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}

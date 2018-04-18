package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;

import java.text.DecimalFormat;

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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.nostra13.universalimageloader.core.ImageLoader;

public class BrandDetail extends Activity {

	private HttpConn httpget = new HttpConn();
	private JSONArray ProductList;
	private GridViewAdapter adapter;
	private String sorts = "SaleNumber"; //排序方式
	private Boolean isASC = true; //是否升序
	private Dialog pBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.brand_detail);
		initLayout();
		getBrandAll();
	}

	// 初始化
	public void initLayout() {
		((TextView) findViewById(R.id.title)).setText(getIntent().getStringExtra("Name"));
		ImageView imageview = (ImageView)findViewById(R.id.imageview);
		ImageLoader.getInstance().displayImage(getIntent().getStringExtra("Logo"), imageview, MyApplication.options);
		
		// 返回
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		//销量排序
		((LinearLayout)findViewById(R.id.linearLayout1)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				((ImageView)findViewById(R.id.img1)).setVisibility(View.VISIBLE);
				((ImageView)findViewById(R.id.arrow1)).setVisibility(View.VISIBLE);
				((ImageView)findViewById(R.id.img2)).setVisibility(View.INVISIBLE);
				((ImageView)findViewById(R.id.arrow2)).setVisibility(View.INVISIBLE);
				((ImageView)findViewById(R.id.img3)).setVisibility(View.INVISIBLE);
				((ImageView)findViewById(R.id.arrow3)).setVisibility(View.INVISIBLE);
				sorts = "SaleNumber";
				if(isASC){
					isASC = false;
					((ImageView)findViewById(R.id.arrow1)).setBackgroundResource(R.drawable.sort_down);
				}
				else{
					isASC = true;
					((ImageView)findViewById(R.id.arrow1)).setBackgroundResource(R.drawable.sort_up);
				}
				getBrandAll();
			}
		});
		//价格排序
		((LinearLayout)findViewById(R.id.linearLayout2)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				((ImageView)findViewById(R.id.img1)).setVisibility(View.INVISIBLE);
				((ImageView)findViewById(R.id.arrow1)).setVisibility(View.INVISIBLE);
				((ImageView)findViewById(R.id.img2)).setVisibility(View.VISIBLE);
				((ImageView)findViewById(R.id.arrow2)).setVisibility(View.VISIBLE);
				((ImageView)findViewById(R.id.img3)).setVisibility(View.INVISIBLE);
				((ImageView)findViewById(R.id.arrow3)).setVisibility(View.INVISIBLE);
				sorts = "Price";
				if(isASC){
					isASC = false;
					((ImageView)findViewById(R.id.arrow2)).setBackgroundResource(R.drawable.sort_down);
				}
				else{
					isASC = true;
					((ImageView)findViewById(R.id.arrow2)).setBackgroundResource(R.drawable.sort_up);
				}
				getBrandAll();
			}
		});
		//时间排序
		((LinearLayout)findViewById(R.id.linearLayout3)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				((ImageView)findViewById(R.id.img1)).setVisibility(View.INVISIBLE);
				((ImageView)findViewById(R.id.arrow1)).setVisibility(View.INVISIBLE);
				((ImageView)findViewById(R.id.img2)).setVisibility(View.INVISIBLE);
				((ImageView)findViewById(R.id.arrow2)).setVisibility(View.INVISIBLE);
				((ImageView)findViewById(R.id.img3)).setVisibility(View.VISIBLE);
				((ImageView)findViewById(R.id.arrow3)).setVisibility(View.VISIBLE);
				sorts = "ModifyTime";
				Log.i("fly", sorts);
				if(isASC){
					isASC = false;
					((ImageView)findViewById(R.id.arrow3)).setBackgroundResource(R.drawable.sort_down);
				}
				else{
					isASC = true;
					((ImageView)findViewById(R.id.arrow3)).setBackgroundResource(R.drawable.sort_up);
				}
				getBrandAll();
			}
		});
	}

	public void getBrandAll() {
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		
		new Thread(){
			public void run(){
				String productCategoryID = getIntent().getStringExtra("ProductCategoryID");
				productCategoryID = productCategoryID==null?"-1":productCategoryID;
				String guid = getIntent().getStringExtra("Guid");
				guid = guid==null?"":guid;
//				String product2Url = "/api/product2/search/?ProductCategoryID=-1&sorts="+sorts
//						+"&isASC="+isASC+"&pageIndex=1&pageCount=100&name=&BrandGuid=" + 
//						getIntent().getStringExtra("Guid") + "&AppSign=" + HttpConn.AppSign +"&AgentID="+MyApplication.agentId+"&sbool=true";
				
				String product2Url = "/api/product2/search/?ProductCategoryID="+productCategoryID+ "&sorts="+sorts
						+"&isASC="+isASC+"&pageIndex=1&pageCount=100&name=&BrandGuid=" + 
						guid+ "&AppSign=" + HttpConn.AppSign +"&AgentID="+MyApplication.agentId+"&sbool=true";

				StringBuffer result = httpget.getArray(product2Url);
				Message msg = Message.obtain();
				try {
					ProductList = new JSONObject(result.toString()).getJSONArray("Data");
					if(ProductList.length() == 0)
						msg.what = 0;
					else
						msg.what = 1;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					msg.what = 0;
					e.printStackTrace();
				}
			}
		}.start();
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				pBar.dismiss();
				((LinearLayout)findViewById(R.id.nocontent)).setVisibility(View.VISIBLE);
				break;
			case 1:
				GridView gridview = ((GridView) findViewById(R.id.gridview));
				gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
				adapter = new GridViewAdapter();
				gridview.setAdapter(adapter);
				pBar.dismiss();
				
				gridview.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View v, int i, long arg3) {
						try {
							Intent intent = new Intent(getApplicationContext(), ProductDetails.class);
							intent.putExtra("guid", ProductList.getJSONObject(i).getString("Guid"));
							startActivity(intent);
						} catch (JSONException e) {
							e.printStackTrace();
						}	
					}
				});
				break;
			default:
				break;
			}
			
			super.handleMessage(msg);
		}

	};

	class GridViewAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return ProductList.length();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.main_item, parent, false);
			}
			try {
				TextView text1 = (TextView) convertView.findViewById(R.id.text1);	
				TextView text2 = (TextView) convertView.findViewById(R.id.text2);	
				TextView text3 = (TextView) convertView.findViewById(R.id.text3);	
    			text1.setText("￥" + new DecimalFormat("0.00").format(ProductList.getJSONObject(position).getDouble("ShopPrice")));	
    			text3.setText("￥" + new DecimalFormat("0.00").format(ProductList.getJSONObject(position).getDouble("MarketPrice")));
    			text2.setText(ProductList.getJSONObject(position).getString("Name"));
				ImageView imageview = (ImageView) convertView.findViewById(R.id.img);
				if (HttpConn.showImage)
					ImageLoader.getInstance().displayImage(ProductList.getJSONObject(position).getString("OriginalImge"), imageview,
							MyApplication.options);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return convertView;
		}

	}

}
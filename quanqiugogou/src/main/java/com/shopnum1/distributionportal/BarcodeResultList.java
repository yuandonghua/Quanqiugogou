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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 商品扫码结果
 */
public class BarcodeResultList extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private static ImageAdapter adapter1; //商品适配器
	private static ImageAdapter adapter2; //商品适配器
	private Dialog pBar; //加载进度
	//标题
	private TextView title;
	//传过来的条码
	private String ProNumVal;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_list);
        
     	//标题
    	title = (TextView) findViewById(R.id.title);
    	title.setText("扫码结果");

    	//传过来的条码
    	ProNumVal = getIntent().getStringExtra("ProNum");
    	
        ((LinearLayout)findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
        //加载列表
        addList();
    }
	//加载列表
	public void addList(){
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		
		//显示列表
		final ListView listview = (ListView) findViewById(R.id.listview);
		adapter1 = new ImageAdapter(1);
		listview.setAdapter(adapter1);
		//商品详情
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int i, long arg3) {
				try {
					Intent intent = new Intent(getApplicationContext(), ProductDetails.class);
					intent.putExtra("guid", adapter1.getInfo(i).getString("Guid"));
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}	
			}
		});
		
		//显示大图
		final GridView gridview = (GridView) findViewById(R.id.gridview);
		adapter2 = new ImageAdapter(2);
		gridview.setAdapter(adapter2);
		//商品详情
		gridview.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int i, long arg3) {
				try {
					Intent intent = new Intent(getApplicationContext(), ProductDetails.class);
					intent.putExtra("guid", adapter2.getInfo(i).getString("Guid"));
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}	
			}
		});
		
		//切换显示
		((LinearLayout)findViewById(R.id.more)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(listview.isShown()) {
					listview.setVisibility(View.INVISIBLE);
					gridview.setVisibility(View.VISIBLE);
					((ImageView)findViewById(R.id.switch_btn)).setBackgroundResource(R.drawable.switch_list);
				} else {
					gridview.setVisibility(View.INVISIBLE);
					listview.setVisibility(View.VISIBLE);
					((ImageView)findViewById(R.id.switch_btn)).setBackgroundResource(R.drawable.switch_big);
				}
			}
		});
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				adapter1.notifyDataSetChanged();
				adapter2.notifyDataSetChanged();
			}
			if (msg.what == 2) { //搜索为空
				pBar.dismiss();
				((LinearLayout)findViewById(R.id.nocontent)).setVisibility(View.VISIBLE);
			}
			super.handleMessage(msg);
		}
		
	};
	//商品适配器
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
			new Thread() {
				@Override
				public void run() {
					Message message = Message.obtain();
					try {
						StringBuffer result = httpget.getArray1("/Api/GetGuidByProductNumber.ashx?ProNum="+ProNumVal+"&Method=GetProductGuid");	
						JSONObject resultObj = new JSONObject(result.toString());
						String IsSuccessVal = resultObj.getString("IsSuccess");
						if(IsSuccessVal != null && "true".equals(IsSuccessVal)){
							companyList = resultObj.getJSONArray("Data");		
							if (companyList != null && companyList.length() > 0) {
								message.what = 1;
							} else {
								message.what = 2;
							}
						}else{
							message.what = 2;
						}
					} catch (JSONException e) {
						message.what = 2;
						e.printStackTrace();
					}
					handler.sendMessage(message);
				}
			}.start();
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(pBar.isShowing())
				pBar.dismiss();
			if(convertView == null) {
				if(id == 1)
					convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.product_item, null);
				else 
					convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.product_item2, null);
				holder = new ViewHolder();
				holder.text1 = (TextView) convertView.findViewById(R.id.textView1);
				holder.text2 = (TextView) convertView.findViewById(R.id.textView2);
				holder.text3 = (TextView) convertView.findViewById(R.id.textView3);	
				holder.imageview = (ImageView) convertView.findViewById(R.id.imageView1);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			try {	
				holder.text1.setText(companyList.getJSONObject(position).getString("Name"));	
				holder.text2.setText("￥" + new DecimalFormat("0.00").format(companyList.getJSONObject(position).getDouble("ShopPrice")));			
				holder.text3.setHint("￥" + new DecimalFormat("0.00").format(companyList.getJSONObject(position).getDouble("MarketPrice")));
				
				String imgUrl = companyList.getJSONObject(position).getString("OriginalImge");
				
				if (HttpConn.showImage) {
					if (imgUrl != null && !("").equals(imgUrl)) {
						if (!imgUrl.startsWith("http:")) {
							imgUrl = HttpConn.urlName + imgUrl;
						}
						ImageLoader.getInstance().displayImage(imgUrl, holder.imageview, MyApplication.options);
					} else {
						holder.imageview.setImageResource(R.drawable.banner);
					}
				} else {
					holder.imageview.setImageResource(R.drawable.banner);
				}
				
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

}
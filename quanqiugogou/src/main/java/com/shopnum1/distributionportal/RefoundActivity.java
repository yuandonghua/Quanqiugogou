package com.shopnum1.distributionportal;

import java.text.DecimalFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zxing.view.MyListView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
public class RefoundActivity extends Activity implements OnClickListener {
	private JSONArray jsonArray;
	private TextView tv_refound;
	private RelativeLayout rl_cause1;
	private RelativeLayout rl_cause2;
	private RelativeLayout rl_cause3;
	private RelativeLayout rl_cause4;
	private RelativeLayout rl_cause5;
	private RelativeLayout rl_cause6;
	private String orderNumber;
	private EditText text_other;
	private HttpConn httpget = new HttpConn();
	private Double alreadPayPrice;
	private String ApplyReason = "买多了/买错了";
	String AlreadPayPrice;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(RefoundActivity.this,"申请失败", Toast.LENGTH_SHORT).show();
			case 1:
				if (msg.obj.equals("202")) {
					startActivity(new Intent(RefoundActivity.this, OrderStatusActivity.class));
					Toast.makeText(RefoundActivity.this,"申请退款成功", Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(RefoundActivity.this,"该订单正在配货中", Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}

		};
	};
	private JSONObject jsonObject_refound;
	private String content;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_refound);
		tv_refound = (TextView) findViewById(R.id.tv_refound);
		text_other = (EditText) findViewById(R.id.text_other);
		text_other.setEnabled(false);
		String orderObject = getIntent().getStringExtra("orderObject");
		try {
			jsonObject_refound = new JSONObject(orderObject);
			orderNumber = jsonObject_refound.getString("OrderNumber");
			alreadPayPrice = jsonObject_refound.getDouble("AlreadPayPrice");
			jsonArray = jsonObject_refound.getJSONArray("ProductList");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		initLayout();
	}
	private void initLayout() {
		rl_cause1 = (RelativeLayout) findViewById(R.id.rl_cause1);
		rl_cause2 = (RelativeLayout) findViewById(R.id.rl_cause2);
		rl_cause3 = (RelativeLayout) findViewById(R.id.rl_cause3);
		rl_cause4 = (RelativeLayout) findViewById(R.id.rl_cause4);
		rl_cause5 = (RelativeLayout) findViewById(R.id.rl_cause5);
		rl_cause6 = (RelativeLayout) findViewById(R.id.rl_cause6);
		rl_cause1.setOnClickListener(this);
		rl_cause2.setOnClickListener(this);
		rl_cause3.setOnClickListener(this);
		rl_cause4.setOnClickListener(this);
		rl_cause5.setOnClickListener(this);
		rl_cause6.setOnClickListener(this);
		findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				finish();
			}
		});
		//申请退款
		findViewById(R.id.refound_btn1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
					
				orderReturn();
			}
		});
		//先不退了
		findViewById(R.id.refound_btn2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		MyListView lv = (MyListView) findViewById(R.id.lv);
		lv.setAdapter(new MyAdapter(jsonArray));
	}
	private class MyAdapter extends BaseAdapter{
		private JSONArray jsonArray;
		public MyAdapter(JSONArray jsonArray) {
			this.jsonArray = jsonArray;
		}

		@Override
		public int getCount() {
			return jsonArray.length();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = View.inflate(getApplicationContext(), R.layout.refound_item, null);
			}try {
				JSONObject jsonObject = jsonArray.getJSONObject(position);
				((TextView) convertView.findViewById(R.id.text1)).setText(jsonObject.getString("NAME"));
		        ((TextView) convertView.findViewById(R.id.text2)).setText("￥"+ new DecimalFormat("0.00").format(jsonObject.getDouble("BuyPrice")));
		        AlreadPayPrice = new DecimalFormat("0.00").format(alreadPayPrice);
		        tv_refound.setText(AlreadPayPrice+"元");
		        ((TextView) convertView.findViewById(R.id.text3)).setText("x"+ jsonObject.getString("BuyNumber"));
				ImageView imageview = (ImageView) convertView.findViewById(R.id.refound_image);
				if (HttpConn.showImage)ImageLoader.getInstance().displayImage(jsonObject.getString("OriginalImge"), imageview,MyApplication.options);
				((TextView)convertView.findViewById(R.id.text)).setText(jsonObject.getString(""));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return convertView;
		}
		
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_cause1:
			content = text_other.getText().toString();
			if(!TextUtils.isEmpty(content)){
				content = null;
			}
			text_other.setEnabled(false);
			text_other.setFocusable(false);
			ApplyReason = "买多了/买错了";
			((ImageView)findViewById(R.id.iv1)).setImageResource(R.drawable.gouxuan_red);
			((ImageView)findViewById(R.id.iv2)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv3)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv4)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv5)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv6)).setImageResource(R.drawable.gouxuan_gay);
			break;
		case R.id.rl_cause2:
			if(!TextUtils.isEmpty(content)){
				content = null;
			}
			text_other.setEnabled(false);
			text_other.setFocusable(false);
			ApplyReason = "计划有变，没时间消费";
			((ImageView)findViewById(R.id.iv1)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv2)).setImageResource(R.drawable.gouxuan_red);
			((ImageView)findViewById(R.id.iv3)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv4)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv5)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv6)).setImageResource(R.drawable.gouxuan_gay);
			break;
		case R.id.rl_cause3:
			if(!TextUtils.isEmpty(content)){
				content = null;
			}
			text_other.setEnabled(false);
			text_other.setFocusable(false);
			ApplyReason = "预约不上";
			((ImageView)findViewById(R.id.iv1)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv2)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv3)).setImageResource(R.drawable.gouxuan_red);
			((ImageView)findViewById(R.id.iv4)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv5)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv6)).setImageResource(R.drawable.gouxuan_gay);
			break;
		case R.id.rl_cause4:
			if(!TextUtils.isEmpty(content)){
				content = null;
			}
			text_other.setEnabled(false);
			text_other.setFocusable(false);
			ApplyReason = "去过了，不太满意";
			((ImageView)findViewById(R.id.iv1)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv2)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv3)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv4)).setImageResource(R.drawable.gouxuan_red);
			((ImageView)findViewById(R.id.iv5)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv6)).setImageResource(R.drawable.gouxuan_gay);
			break;
		case R.id.rl_cause5:
			text_other.setEnabled(false);
			text_other.setFocusable(false);
			ApplyReason ="朋友/网上评价不好";
			((ImageView)findViewById(R.id.iv1)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv2)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv3)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv4)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv5)).setImageResource(R.drawable.gouxuan_red);
			((ImageView)findViewById(R.id.iv6)).setImageResource(R.drawable.gouxuan_gay);
			break;
		case R.id.rl_cause6:
			text_other.setEnabled(true);
			text_other.setFocusable(true);
			ApplyReason = text_other.getText().toString().trim();
			((ImageView)findViewById(R.id.iv1)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv2)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv3)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv4)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv5)).setImageResource(R.drawable.gouxuan_gay);
			((ImageView)findViewById(R.id.iv6)).setImageResource(R.drawable.gouxuan_red);
			break;

		default:
			break;
		}
		
	}
	/**
	 * 申请退款
	 * 
	 * @param guid
	 */
	public void orderReturn() {
		new Thread() {
			public void run() {
				Message msg = Message.obtain();
				try {
					JSONObject object = new JSONObject();
					object.put("OrderID", orderNumber);
					object.put("ReturnMoney", AlreadPayPrice);
					object.put("memLoginId", HttpConn.username);
					object.put("AppSign", HttpConn.AppSign);	 
					if(TextUtils.isEmpty(ApplyReason)){
						ApplyReason = "其他原因";
					}
					object.put("ApplyReason", ApplyReason);
					StringBuffer result = httpget.postJSON("/api/memberepairs/",object.toString());
					msg.obj = new JSONObject(result.toString()).getString("return");
					msg.what = 1;
				} catch (JSONException e) {
					msg.what = 0;
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
}

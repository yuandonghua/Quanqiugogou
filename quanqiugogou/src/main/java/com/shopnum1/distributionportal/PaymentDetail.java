package com.shopnum1.distributionportal;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
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
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.PullToRefreshView;
import com.shopnum1.distributionportal.util.PullToRefreshView.OnFooterRefreshListener;
import com.shopnum1.distributionportal.util.PullToRefreshView.OnHeaderRefreshListener;
/**
 * 预存款明细
 */
public class PaymentDetail extends Activity implements OnHeaderRefreshListener,OnFooterRefreshListener {
	private HttpConn httpget = new HttpConn();
	private Dialog pBar; //加载进度
	
	//明细列表
	private ListView listview;
	
	private List<HashMap<String, String>> mListItems = new LinkedList<HashMap<String,String>>();
	private PaymentDetailAdapter mAdapter;
	private PullToRefreshView mPullToRefreshView;
	
	/** 分页页码*/
	private int page = 1;
	private int pageSize = 10;
	private int totalCount = 0;
	
	//预存款的obj
	private JSONObject paymentObj;
	//用户当前的预存款
	private String currentPayment;
	//预存款明细的JSONArray
	private JSONArray paymentArray;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_detail);
		advancePayment = getIntent().getDoubleExtra("AdvancePayment", 0.00);
		//((TextView)findViewById(R.id.paymentValue)).setText(advancePayment+"");
		//通知adapter刷新
		if(null != mListItems || mListItems.size()>0){
			mListItems.clear();
		}
		if(null != mAdapter){
			mAdapter.notifyDataSetChanged();
		}
		
		//明细列表
		listview = (ListView) findViewById(R.id.listview);
		
		//初始化布局
		initLayout();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//获得预存款详细
		getPaymentDetail();
	}

	//初始化布局
	public void initLayout(){
		//返回
		((LinearLayout)findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		//我要充值
		((LinearLayout)findViewById(R.id.more)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//跳到我要充值界面
				Intent intent = new Intent(getApplicationContext(), RechargeActivity.class);
				intent.putExtra("currentPayment", currentPayment);
				startActivity(intent);
			}
		});
		
		mPullToRefreshView = (PullToRefreshView) findViewById(R.id.main_pull_refresh_view);
		mPullToRefreshView.setOnHeaderRefreshListener(this);
		mPullToRefreshView.setOnFooterRefreshListener(this);
		
	}
	
	//获得预存款详细
	public void getPaymentDetail(){
		
		if(pBar == null){
			pBar = new Dialog(this, R.style.dialog);
			pBar.setContentView(R.layout.progress);
		}
		if(pBar != null && !pBar.isShowing()){
			pBar.show();
		}
		
		new Thread() {
			@Override
			public void run() {
				Message message = Message.obtain();
				
				//获取预存款列表集合 
				try {
					StringBuffer result = httpget.getArray("/api/getAdvancePaymentModifyLog?MemLoginId="+HttpConn.username+"&AppSign="+HttpConn.AppSign);
					paymentObj = new JSONObject(result.toString());
					//当前用户预存款
					paymentArray = paymentObj.getJSONArray("data");
					
					BigDecimal b = new BigDecimal(paymentArray.getJSONObject(0).getDouble("LastOperateMoney"));
				
					
					currentPayment =String.format("%.2f", b);;
					if(true){
						if (paymentArray != null && paymentArray.length() == 0){
							message.what = 0;
						}else{
							if (paymentArray != null && paymentArray.length() > 0) {
								message.what = 1;
							}else{
								message.what = 0;
							}
						}
					}else{
						message.what = 0;
					}
				} catch (JSONException e) {
					currentPayment = "0.00";
					message.what = 0;
					e.printStackTrace();
				}
				handler.sendMessage(message);
			}
		}.start();
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (totalCount == 0) {
					((TextView)findViewById(R.id.nocontent)).setVisibility(View.VISIBLE);
					((ListView) findViewById(R.id.listview)).setVisibility(View.GONE);
				}
				((TextView)findViewById(R.id.paymentValue)).setText(currentPayment+"");
				if(pBar != null && pBar.isShowing()){
					pBar.dismiss();
				}
				break;
			case 1:
				((TextView)findViewById(R.id.nocontent)).setVisibility(View.GONE);
				((ListView) findViewById(R.id.listview)).setVisibility(View.VISIBLE);
				
				((TextView)findViewById(R.id.paymentValue)).setText(currentPayment+"");
				
				try {
					for (int i = 0; i <  paymentArray.length(); i++) {
						JSONObject payObj = paymentArray.getJSONObject(i);
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("Guid", payObj.getString("Guid"));
						map.put("OperateType", payObj.getString("OperateType"));
						
				
						map.put("CurrentAdvancePayment", new BigDecimal(payObj.getString("CurrentAdvancePayment")).toPlainString());
						map.put("OperateMoney",new BigDecimal(payObj.getString("OperateMoney")).toPlainString());
						map.put("LastOperateMoney", new BigDecimal(payObj.getString("LastOperateMoney")).toPlainString());
						map.put("Date", payObj.getString("Date"));
						mListItems.add(map);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if (totalCount == 0) {
					totalCount = mListItems.size();
					mAdapter = new PaymentDetailAdapter(mListItems);
					listview.setAdapter(mAdapter);
				} else if (totalCount > 0 && totalCount < mListItems.size()) {
					mAdapter.notifyDataSetChanged();
				}
				if(pBar != null && pBar.isShowing()){
					pBar.dismiss();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	private Double advancePayment;
	
	//预存款列表适配器
	class PaymentDetailAdapter extends BaseAdapter {

		private List<HashMap<String,String>> payListItems = new LinkedList<HashMap<String,String>>();
		
		public PaymentDetailAdapter(List<HashMap<String,String>> payListItems){
			this.payListItems = payListItems;
		}
		
		@Override
		public int getCount() {
			return payListItems.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.payment_detail_item, null);
					
				holder.timeTv = (TextView)convertView.findViewById(R.id.timeTv);
				holder.opertypeTv = (TextView)convertView.findViewById(R.id.opertypeTv);
				holder.opervalueTv = (TextView)convertView.findViewById(R.id.opervalueTv);
				holder.currentValue = (TextView)convertView.findViewById(R.id.currentValue);
				
				convertView.setTag(holder);
				
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			HashMap<String, String> map = payListItems.get(position);
			
	        //时间
	        String DateVal = map.get("Date");
	        if(null == DateVal || "null".equals(DateVal) || "".equals(DateVal)){
	        	holder.timeTv.setText("");
			}else{
				if(DateVal.contains("/Date(")){
					DateVal = DateVal.replace("/Date(", "").replace(")/", "");
					DateVal = DateVal.substring(0,13);
					Date date = new Date(Long.valueOf(DateVal));
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String showTimeVal = formatter.format(date);
					holder.timeTv.setText(showTimeVal);
				}else{
					holder.timeTv.setText(DateVal);
				}
			}
			
			
			//描述信息（0提现,1充值,2转账,3 提成,4扣款，5回款，预存款抵扣00）
			String OperateTypeVal = map.get("OperateType");
			if(null == OperateTypeVal || "null".equals(OperateTypeVal) || "".equals(OperateTypeVal)){
				holder.opertypeTv.setText("");
			}else{
				if("0".equals(OperateTypeVal)){
					holder.opertypeTv.setText("提现");
				}else if("1".equals(OperateTypeVal)){
					holder.opertypeTv.setText("充值");
				}else if("2".equals(OperateTypeVal)){
					holder.opertypeTv.setText("转账");
				}else if("3".equals(OperateTypeVal)){
					holder.opertypeTv.setText("提成");
				}else if("4".equals(OperateTypeVal)){
					holder.opertypeTv.setText("扣款");
				}else if("5".equals(OperateTypeVal)){
					holder.opertypeTv.setText("回款");
				}else{
					holder.opertypeTv.setText("");
				}
			}
			
			//改变的金额
			String opervalueTvVal = map.get("OperateMoney");
			if(null == opervalueTvVal || "null".equals(opervalueTvVal) || "".equals(opervalueTvVal)){
				holder.opervalueTv.setText("");
			}else{
				if("0".equals(OperateTypeVal)){
					holder.opervalueTv.setText("-"+opervalueTvVal);
					holder.opervalueTv.setTextColor(getResources().getColor(R.color.green));
				}else if("1".equals(OperateTypeVal)){
					holder.opervalueTv.setText("+"+opervalueTvVal);
					holder.opervalueTv.setTextColor(getResources().getColor(R.color.red));
				}else if("2".equals(OperateTypeVal)){
					holder.opervalueTv.setText("-"+opervalueTvVal);
					holder.opervalueTv.setTextColor(getResources().getColor(R.color.green));
				}else if("3".equals(OperateTypeVal)){
					holder.opervalueTv.setText("+"+opervalueTvVal);
					holder.opervalueTv.setTextColor(getResources().getColor(R.color.red));
				}else if("4".equals(OperateTypeVal)){
					holder.opervalueTv.setText("-"+opervalueTvVal);
					holder.opervalueTv.setTextColor(getResources().getColor(R.color.green));
				}else if("5".equals(OperateTypeVal)){
					holder.opervalueTv.setText("+"+opervalueTvVal);
					holder.opervalueTv.setTextColor(getResources().getColor(R.color.red));
				}else{
					holder.opervalueTv.setText("");
				}
			}
			
			//当前的预存款余额
			String LastOperateMoneyVal = map.get("LastOperateMoney");
			if(null == LastOperateMoneyVal || "null".equals(LastOperateMoneyVal) || "".equals(LastOperateMoneyVal)){
				holder.currentValue.setText("");
			}else{
				holder.currentValue.setText(LastOperateMoneyVal);
			}
			
			return convertView;
		}

		private class ViewHolder {
			TextView timeTv, opertypeTv, opervalueTv, currentValue;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		page = 1;
		totalCount = 0;
		mListItems.clear();
		//通知adapter刷新
		if(null != mAdapter){
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		page = 1;
		totalCount = 0;
		mListItems.clear();
		//通知adapter刷新
		if(null != mAdapter){
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onFooterRefresh(PullToRefreshView view) {
		page++;
		getPaymentDetail();
		
		mPullToRefreshView.postDelayed(new Runnable() {

			@Override
			public void run() {
				mPullToRefreshView.onFooterRefreshComplete();
			}
		}, 1000);
	}

	@Override
	public void onHeaderRefresh(PullToRefreshView view) {
		page = 1;
		totalCount = 0;
		mListItems.clear();
		//通知adapter刷新
		if(null != mAdapter){
			mAdapter.notifyDataSetChanged();
		}
		getPaymentDetail();
		
		mPullToRefreshView.postDelayed(new Runnable() {

			@Override
			public void run() {
				// 设置更新时间
				mPullToRefreshView.onHeaderRefreshComplete();
			}
		}, 1000);
		
	}

}

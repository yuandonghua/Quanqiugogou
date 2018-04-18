package com.shopnum1.distributionportal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.alipay.sdk.pay.PayUtil;

import com.shopnum1.distributionportal.util.HttpConn;
/**
 * 我要充值的Activity
 */
public class RechargeActivity extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private Dialog pBar; //加载进度
	//充值金额编辑
	private EditText et1;
	//用户填写的充值金额
	private String et1Val;
	//提交按钮
	private TextView commitTv;
	//支付类型的jsonArray
	private JSONArray paymentArray;
	//支付类型的名字
	private String typeName;
	//支付类型的guid
	private String typeGuid;
	//获得返回的订单号
	private String orderNum;
	private TextView payName;
	private ArrayList<Map<String, String>> PaymentList;
	
	private  String paynametype="";
	public static RechargeActivity rechargeActivity = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recharge);
		rechargeActivity = RechargeActivity.this;
		currentPayment = getIntent().getDoubleExtra("AdvancePayment",0.00);
		//充值金额编辑
		et1 = (EditText) findViewById(R.id.et1);
		et1.addTextChangedListener(mEtWatcher);
		//提交按钮
		commitTv = (TextView) findViewById(R.id.commitTv);
		//初始化界面
		initLayout();
	}
	
	TextWatcher mEtWatcher = new TextWatcher() {
	       
	    @Override 
	    public void onTextChanged(CharSequence s, int start, int before, int count) {

			if (s.toString().contains(".")) {
				if (s.length() - 1 - s.toString().indexOf(".") > 2) {
					s = s.toString().subSequence(0, s.toString().indexOf(".") + 3);
					et1.setText(s);
					et1.setSelection(s.length());
				}
			}
			if (s.toString().trim().substring(0).equals(".")) {
				s = "0" + s;
				et1.setText(s);
				et1.setSelection(2);
			}
			if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
				if (!s.toString().substring(1, 2).equals(".")) {
					et1.setText(s.subSequence(0, 1));
					et1.setSelection(1);
					return;
				}
			}
	    } 
	    @Override 
	    public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
	   
	    } 
	    @Override 
	    public void afterTextChanged(Editable s) { 
	    
	    }
	}; 
	
	//初始化界面
	public void initLayout(){
		getTypeGuid();
		//返回
		((ImageView)findViewById(R.id.iv_back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		((RelativeLayout)findViewById(R.id.rl_payment)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(RechargeActivity.this, R.style.MyDialog2);
				View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog2, null);
				ListView listview = (ListView) view.findViewById(R.id.listview);
				SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), PaymentList,
						R.layout.city_item, new String[] {"guid", "name"}, new int[] {R.id.guid, R.id.name});
				listview.setAdapter(adapter);
				dialog.setContentView(view);
				dialog.show();
				listview.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
						payName = (TextView) view.findViewById(R.id.name);
						paynametype=payName.getText().toString();
						tv_paymode.setText(paynametype);
						dialog.dismiss();
					}
				});
			}
		});
		//提交按钮
		commitTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//设置不可点击
				commitTv.setClickable(false);
				//获得用户填写的金额
				et1Val = et1.getText().toString().trim();
				if (et1Val == null || "".equals(et1Val)) {
					//提示用户输入充值金额
					Toast.makeText(getApplicationContext(), "请输入充值金额", Toast.LENGTH_SHORT).show();
					//设置可点击
					commitTv.setClickable(true);
				} else if ("0".equals(et1Val) || "0.0".equals(et1Val) || "0.00".equals(et1Val)) {
					//提示用户输入充值金额
					Toast.makeText(getApplicationContext(), "充值金额不能为0", Toast.LENGTH_SHORT).show();
					//设置可点击
					commitTv.setClickable(true);
				} else {
					//获取支付方式guid
					//getTypeGuid();
					if(typeGuid == null || "".equals(typeGuid) || "null".equals(typeGuid)){
						//提示用户获取数据失败
						Toast.makeText(getApplicationContext(), "生成充值订单失败", Toast.LENGTH_SHORT).show();
						//设置可点击
						commitTv.setClickable(true);
						if(pBar != null && pBar.isShowing()){
							pBar.dismiss();
						}
					}else{
						//插入充值记录，得到订单号
						insertValue();
					}
				}
			}
		});
	}
	
	//获取支付方式guid
	public void getTypeGuid(){
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
				//获取支付类型guid
				try {
					StringBuffer result = httpget.getArray("/api/payment/?AppSign=" + HttpConn.AppSign);
					JSONObject paymentTypeObj = new JSONObject(result.toString());
					paymentArray = paymentTypeObj.getJSONArray("data");
					PaymentList = new ArrayList<Map<String, String>>();
					for (int i = 0; i < paymentArray.length(); i++) {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("guid", paymentArray.getJSONObject(i).getString("Guid"));
						map.put("name", paymentArray.getJSONObject(i).getString("NAME"));
						if(!(paymentArray.getJSONObject(i).getString("NAME")).startsWith("预存款")){
							PaymentList.add(map);
						}
					
						
						if (paymentArray.getJSONObject(i).getString("NAME").startsWith("支付宝支付")) {
							if (paymentArray.getJSONObject(i).getString("Public_Key") != null
									&& !"".equals(paymentArray.getJSONObject(i)
											.getString("Public_Key"))) {
								PayUtil.RSA_PUBLIC = paymentArray.getJSONObject(i)
										.getString("Public_Key");
							}
							if (paymentArray.getJSONObject(i).getString("Private_Key") != null
									&& !"".equals(paymentArray.getJSONObject(i)
											.getString("Private_Key"))) {
								PayUtil.RSA_PRIVATE = paymentArray.getJSONObject(i)
										.getString("Private_Key");
							}
							if (paymentArray.getJSONObject(i).getString("MerchantCode") != null
									&& !"".equals(paymentArray.getJSONObject(i)
											.getString("Partner"))) {
								PayUtil.PARTNER = paymentArray.getJSONObject(i)
										.getString("MerchantCode");
							}
							if (paymentArray.getJSONObject(i).getString("Email") != null
									&& !"".equals(paymentArray.getJSONObject(i)
											.getString("Email"))) {
								PayUtil.SELLER = paymentArray.getJSONObject(i)
										.getString("Email");
							}
						}
						
						
					}
					if(PaymentList.size()>0){
						paynametype=PaymentList.get(0).get("name");
					}
					message.what = 1;
				} catch (JSONException e) {
					message.what = 0;
					e.printStackTrace();
				}
				handler.sendMessage(message);
			}
		}.start();
	}
	
	//插入充值记录,返回订单号
	public void insertValue(){
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
					StringBuffer result = null;
					try {
						result = httpget.getArray("/api/insertAdvancePaymentApplyLog?MemLoginID=" + HttpConn.username + "&CurrentAdvancePayment="+currentPayment+
								"&OperateMoney="+ et1Val + "&PaymentGuid=" + typeGuid + "&PaymentName=" + URLEncoder.encode(typeName,"utf-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					JSONObject resultObj = new JSONObject(result.toString());
					String IsSuccessVal = resultObj.getString("return");
					if("202".equals(IsSuccessVal)){
						String OrderNumber = resultObj.getString("OrderNumber");
							message.obj = OrderNumber;
							message.what = 3;
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
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				//提示用户获取数据失败
				Toast.makeText(getApplicationContext(), "获取支付数据失败", Toast.LENGTH_SHORT).show();
				//设置可点击
				commitTv.setClickable(true);
				if(pBar != null && pBar.isShowing()){
					pBar.dismiss();
				}
				break;
			
			case 1:
				if(pBar != null && pBar.isShowing()){
					pBar.dismiss();
				}
				//得到支付宝支付的guid
				tv_paymode = (TextView) findViewById(R.id.tv_paymode);
				tv_paymode.setText(PaymentList.get(0).get("name"));
				try {
					for (int i = 0; i < paymentArray.length(); i++) {
						JSONObject payObj = paymentArray.getJSONObject(i);
						String payname = payObj.getString("NAME");
						String PaymentType = payObj.getString("PaymentType");
						if("Alipay.aspx".equals(PaymentType)){
							typeName = payname;
							typeGuid = payObj.getString("Guid");
						}else if("Tenpay.aspx".equals(PaymentType)){
							typeName = payname;
							typeGuid = payObj.getString("Guid");
						}else if ("Weixin.aspx".equals(PaymentType)) {
							typeName = payname;
							typeGuid = payObj.getString("Guid");
						} else if ("AlipaySDK.aspx".equals(PaymentType)) {
							typeName = payname;
							typeGuid = payObj.getString("Guid");
						}
					}
				} catch (JSONException e) {
					typeGuid = "";
					e.printStackTrace();
				}
				break;
			case 2:
				//提交失败
				Toast.makeText(getApplicationContext(), "提交失败", Toast.LENGTH_SHORT).show();
				//设置可点击
				commitTv.setClickable(true);
				if(pBar != null && pBar.isShowing()){
					pBar.dismiss();
				}
				break;
			case 3:
				//设置可点击
				pBar.dismiss();
				commitTv.setClickable(true);
				orderNum = (String) msg.obj;
				if(paynametype.startsWith("微信")){
					Toast.makeText(RechargeActivity.this, "请绑定商户ID", 0).show();
				}else if(paynametype.startsWith("财付通")){
					Intent intent = new Intent(new Intent(getApplicationContext(),TenpayActivity.class));
					intent.putExtra("order_no", orderNum);
					intent.putExtra("source", "RechargeActivity");
					
					intent.putExtra("order_price",et1Val );
					startActivity(intent);
					finish();
				}else if(paynametype.startsWith("支付宝手机网站")){
					Intent intent = new Intent(new Intent(getApplicationContext(),AlipayActivity.class));
					intent.putExtra("order", orderNum);
					intent.putExtra("total",et1Val );
					startActivity(intent);
					finish();
				}else if(paynametype.startsWith("支付宝SDK支付")){
						PayUtil PayUtil = new PayUtil(
								RechargeActivity.this, "分销门户", orderNum,
								et1Val,
								new PayUtil.CallbackListener() {

									@Override
									public void updateOrderState() {
										updateState(orderNum);
									}
								});
						PayUtil.pay();
					}else if(paynametype.startsWith("京东支付")){
						Intent intent = new Intent(new Intent(getApplicationContext(),JingDongActivity.class));
						intent.putExtra("order", orderNum);
						intent.putExtra("total",et1Val );
						intent.putExtra("source","RechargeActivity" );
						startActivity(intent);
						finish();
					}
				
				break;
			case 4:
				if(pBar != null && pBar.isShowing()){
					pBar.dismiss();
				}
				Intent intent = new Intent(RechargeActivity.this, MemberActivity.class);
				startActivity(intent);
				finish();
				if (msg.obj.equals("true")) {
					Toast.makeText(getApplicationContext(), "充值成功",Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "充值失败",Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	private Double currentPayment;
	private TextView tv_paymode;
	/**
	 * 充值结果
	 * @param orderNumber
	 */
		public void updateState(final String orderNumber) {
			new Thread() {
				public void run() {
					JSONObject object = new JSONObject();
					try {
						object.put("OrderNumber", orderNumber);
						StringBuffer stateList= httpget.postJSON("/api/UpdateAdvancePayMentLog/", object.toString());
						Message msg = Message.obtain();
						msg.obj = new JSONObject(stateList.toString()).getString("Data");
						msg.what = 4;
						handler.sendMessage(msg);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

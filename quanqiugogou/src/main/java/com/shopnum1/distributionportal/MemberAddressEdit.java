package com.shopnum1.distributionportal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
public class MemberAddressEdit extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private String Guid, Code;
	private String detialAddress = "";
	private String AreaCodeName = "";
	private Boolean newAddress = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_address_edit);
		
		initLayout();
	}
	//初始化
	public void initLayout() {
		if(getIntent().getStringExtra("addressList") != null){
			newAddress = false;
			try {
				JSONObject addressList = new JSONObject(getIntent().getStringExtra("addressList"));
				Guid = addressList.getString("Guid");
				Code = addressList.getString("Code");
				detialAddress = addressList.getString("Address");
				getCodeAddress(Code);
				((EditText)findViewById(R.id.name)).setText(addressList.getString("NAME"));
				((EditText)findViewById(R.id.address)).setText(addressList.getString("Address"));
				((EditText)findViewById(R.id.mobile)).setText(addressList.getString("Mobile"));
				((EditText)findViewById(R.id.mail)).setText(addressList.getString("Email"));
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			//删除
			((TextView)findViewById(R.id.title)).setText("编辑收货地址");
			((Button)findViewById(R.id.delete)).setVisibility(View.VISIBLE);
	        ((Button)findViewById(R.id.delete)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					final Dialog dialog = new Dialog(MemberAddressEdit.this, R.style.MyDialog);
					View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.dialog, null);
					((TextView)view.findViewById(R.id.dialog_text)).setText("是否删除地址");
					dialog.setContentView(view);
					dialog.show();
					
					((Button)view.findViewById(R.id.no)).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							dialog.dismiss();
						}
					});
					
					((Button)view.findViewById(R.id.yes)).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							new Thread(){
								public void run(){
									StringBuffer result = httpget.getArray("/api/addressdelete/?Guid=" + Guid + "&AppSign=" + HttpConn.AppSign);
									
									Log.i("fly", result.toString());
									try {
										Message msg = Message.obtain();
										msg.obj = new JSONObject(result.toString()).getString("return");
										msg.what = 2;
										handler.sendMessage(msg);
									} catch (JSONException e) {
										e.printStackTrace();
									}
									
								}
							}.start();
						}
					});
				}
			});
		}
		
		//返回
        ((LinearLayout)findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});    
        
        //省市区
        ((EditText)findViewById(R.id.city)).setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View arg0) {
				startActivityForResult(new Intent(getBaseContext(), CityList.class), 0);
        	}
        });
        
        //姓名
        ((EditText)findViewById(R.id.name)).setOnFocusChangeListener(new OnFocusChangeListener() {
  			
  			@Override
  			public void onFocusChange(View arg0, boolean isFocused) {
  				if(isFocused)
  					((ImageView)findViewById(R.id.icon1)).setBackgroundResource(R.drawable.username1);
  				else
  					((ImageView)findViewById(R.id.icon1)).setBackgroundResource(R.drawable.username);
  			}
  		});
  		
  		//地址
  		((EditText)findViewById(R.id.address)).setOnFocusChangeListener(new OnFocusChangeListener() {
  			
  			@Override
  			public void onFocusChange(View arg0, boolean isFocused) {
  				if(isFocused)
  					((ImageView)findViewById(R.id.icon2)).setBackgroundResource(R.drawable.address1);
  				else
  					((ImageView)findViewById(R.id.icon2)).setBackgroundResource(R.drawable.address);
  			}
  		});
  		//手机
		((EditText)findViewById(R.id.mobile)).setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean isFocused) {
				if(isFocused)
					((ImageView)findViewById(R.id.icon3)).setBackgroundResource(R.drawable.mobile1);
				else
					((ImageView)findViewById(R.id.icon3)).setBackgroundResource(R.drawable.mobile);
			}
		});
		
		//邮箱
		((EditText)findViewById(R.id.mail)).setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean isFocused) {
				if(isFocused)
					((ImageView)findViewById(R.id.icon4)).setBackgroundResource(R.drawable.email1);
				else
					((ImageView)findViewById(R.id.icon4)).setBackgroundResource(R.drawable.email);
			}
		});	
        
        //完成
        ((LinearLayout)findViewById(R.id.finished)).setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View arg0) {
        		final String name = ((EditText)findViewById(R.id.name)).getText().toString();
        		final String city = ((EditText)findViewById(R.id.city)).getText().toString();
        		final String address = ((EditText)findViewById(R.id.address)).getText().toString();
        		final String mobile = ((EditText)findViewById(R.id.mobile)).getText().toString();
        		final String mail = ((EditText)findViewById(R.id.mail)).getText().toString();
        		if(name.equals("") || city.equals("") || address.equals("") || mobile.equals("")){
        			Toast.makeText(getBaseContext(), "请输入完整信息", Toast.LENGTH_SHORT).show();
        		} else if(!isMobile(mobile)){
        			Toast.makeText(getBaseContext(), "请输入正确的手机号", Toast.LENGTH_SHORT).show();
        		} else if(!mail.equals("") && !isEmail(mail)){
        			Toast.makeText(getBaseContext(), "请输入正确的邮箱", Toast.LENGTH_SHORT).show();
        		} else {
        			new Thread(){
        				public void run(){	
        					try {
        						JSONObject json = new JSONObject();
								json.put("NAME", name);
								json.put("Email", mail);
								json.put("Address", city+address);
								json.put("Postalcode", "100000");
								json.put("Mobile", mobile);
								json.put("Tel", "");
								json.put("Code", Code);
								json.put("MemLoginID", HttpConn.UserName);
								json.put("Guid", Guid);
								json.put("AppSign", HttpConn.AppSign);
								
								Message msg = Message.obtain();
								StringBuffer result = null;
								if(newAddress){
									result = httpget.postJSON("/api/addressadd/", json.toString());
									msg.what = 1;
								} else {
									result = httpget.postJSON("/api/addressupdate/", json.toString());
									msg.what = 3;
								}	
								Log.i("fly", result.toString());
								msg.obj = new JSONObject(result.toString()).getString("return");
								handler.sendMessage(msg);
							} catch (JSONException e) {
								e.printStackTrace();
							}
        				}
        			}.start();
        		}
        	}
        });

	}
	
	private void getCodeAddress(final String code) {
		new Thread(){
			public void run(){	
				Message message = Message.obtain();
				try {
					StringBuffer result = httpget.getArray("/api/GetAreaByCode/?"+"code="+code);
					JSONObject arrayVal = new JSONObject(result.toString());
					if (result != null && !("").equals(result)) {
							if (arrayVal != null && arrayVal.length() > 0) {
								String ProvinceName = arrayVal.getJSONObject("Data").getString("ProvinceName");
								String CityName = arrayVal.getJSONObject("Data").getString("CityName");
								String AreaName = arrayVal.getJSONObject("Data").getString("AreaName");
								String Address2 = ProvinceName+CityName+AreaName;
								message.obj = Address2;
								message.what = 4;
								
							} else {
								message.what = 5;
							}
					}
				} catch (JSONException e) {
					message.what = 5;
					e.printStackTrace();
				}
				handler.sendMessage(message);
			}
		}.start();
	}
	
	//判断收货人
	public static boolean isChinese(String str) {
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if(m.find()){
			return true;
		}
		return false;
	}
	
	// 判断手机号
    public static boolean isMobile(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(17[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    // 判断邮箱
    public static boolean isEmail(String email) {
    	String str = "^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if(msg.obj.toString().equals("202")){
					Toast.makeText(getBaseContext(), "添加成功", Toast.LENGTH_SHORT).show();
					setResult(1, getIntent());
					finish();
				} else {
					Toast.makeText(getBaseContext(), "添加失败", Toast.LENGTH_SHORT).show();
				}	
				break;
			case 2:
				if(msg.obj.toString().equals("202")){
					Toast.makeText(getBaseContext(), "删除成功", Toast.LENGTH_SHORT).show();
					setResult(1, getIntent());
					finish();
				} else {
					Toast.makeText(getBaseContext(), "删除失败", Toast.LENGTH_SHORT).show();
				}	
				break;
			case 3:
				if(msg.obj.toString().equals("202")){
					Toast.makeText(getBaseContext(), "修改成功", Toast.LENGTH_SHORT).show();
					setResult(1, getIntent());
					finish();
				} else {
					Toast.makeText(getBaseContext(), "修改失败", Toast.LENGTH_SHORT).show();
				}	
				break;
			case 4:
				String address = (String) msg.obj;
				((EditText)findViewById(R.id.city)).setText(address);
//				if (detialAddress.startsWith(AreaCodeName)) {
//					String str = detialAddress.replace(AreaCodeName, "");
//					((EditText)findViewById(R.id.city)).setText(AreaCodeName);
//					((EditText)findViewById(R.id.address)).setText(str);
//				} else {
//					((EditText)findViewById(R.id.city)).setText(AreaCodeName);
//					((EditText)findViewById(R.id.address)).setText(detialAddress);
//				}
				break;
			case 5:
				Toast.makeText(getApplicationContext(), "获取地址详细信息失败", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1){
			Code = data.getStringExtra("code");
			((EditText)findViewById(R.id.address)).setText("");
			((EditText)findViewById(R.id.city)).setText(HttpConn.cityName);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.TimeCountUtil;
//注册
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UserRegister extends Activity {

	private HttpConn httpget = new HttpConn();
	private String username, password; // 用户名和密码
	private int time1;
	private Button get_check;

	private TimeCountUtil timeCountUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_register);
		initLayout();
	}

	// 初始化
	public void initLayout() {
		get_check = (Button) this.findViewById(R.id.get_check);
		get_check.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				username = ((EditText) findViewById(R.id.name)).getText().toString().trim().replaceAll(" ", "");
				exists();

			}
		});
		// 返回
		((LinearLayout) findViewById(R.id.back))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
		// 用户协议
		((TextView) findViewById(R.id.agree))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),UserAgree.class));
					}
				});
		// 下一步
		((Button) findViewById(R.id.register))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						username = ((EditText) findViewById(R.id.name)).getText().toString().trim().replaceAll(" ", "");
						password = ((EditText) findViewById(R.id.pwd))
								.getText().toString().trim()
								.replaceAll(" ", "");
						if (!TextUtils.isEmpty(username)
								&& !TextUtils.isEmpty(password)) {
							register();
						} else {
							Toast.makeText(getApplicationContext(),
									"用户名或验证码不能为空", Toast.LENGTH_SHORT).show();
						}
					}
				});

		timeCountUtil = new TimeCountUtil(this, get_check, 60000, 1000);

	}

	// 电话号码
	public static boolean isName(String str) {
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

	// 判断用户是否存在
	public void exists() {
		new Thread() {
			@Override
			public void run() {
				try {
					StringBuffer result = httpget.getArray("/api/accountuserexist/?MemLoginID="+ username + "&AppSign=" + HttpConn.AppSign);
					String txt = result.toString();
//					StringBuffer result = httpget.getArray("/api/accountuserexist/?MemLoginID=sunbin1900&AppSign=" + HttpConn.AppSign);
					Message msg = Message.obtain();
					
					msg.obj = new JSONObject(txt).getString("return");
					msg.what = 1;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	// 下一步
	public void register() {
		CheckBox check = (CheckBox) findViewById(R.id.check);
		if (check.isChecked()) {
//			Intent i2 = new Intent(UserRegister.this, RegistActivity2.class);  //Todo1  删
//			i2.putExtra("phoneNum", ((EditText) findViewById(R.id.name)).getText().toString());//Todo1  删
//			startActivity(i2);
			if (checkNum.equals(((EditText) findViewById(R.id.pwd)).getText().toString())) {
			
				Intent i = new Intent(UserRegister.this, RegistActivity2.class);
				i.putExtra("phoneNum", ((EditText) findViewById(R.id.name)).getText().toString());
		
				startActivity(i);
			} else {
				Toast.makeText(getApplicationContext(), "验证码错误", Toast.LENGTH_SHORT).show();
				((EditText) findViewById(R.id.pwd)).setText("");
			}
		} else {
			Toast.makeText(UserRegister.this, "请阅读用户注册协议", Toast.LENGTH_SHORT).show();
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (msg.obj.equals("true")) {
					int test = time1;
					if (time1 == 0) {
						Toast.makeText(UserRegister.this, "用户已存在",
								Toast.LENGTH_SHORT).show();
						time1++;
					}
				} else if (msg.obj.equals("false")) {
					getcheck();
				}
				break;
			case 2:
				break;
			case 11:
				if (checkNum.equals("手机号码不合法")) {
					Toast.makeText(getApplicationContext(), "手机号码不合法", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "获取验证成功，请耐心等待",1000).show();
					timeCountUtil.start();
				}
				break;
			case 12:
				Toast.makeText(getApplicationContext(), "获取验证码失败", 1000).show();
				break;
			}
			super.handleMessage(msg);
		}
	};
	private String checkNum;

	private void getcheck() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = handler.obtainMessage();
				StringBuffer check = httpget.getArray("/api/GetMobileCode/?Mobile="+ ((EditText) findViewById(R.id.name)).getText().toString());
				try {
					JSONObject jo = new JSONObject(check.toString());
					checkNum = jo.getString("Data");
					msg.what = 11;
				} catch (JSONException e) {
					msg.what = 12;
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}).start();
	}
}
package com.shopnum1.distributionportal;
//搜索
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.zxing.activity.CaptureActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.shopnum1.distributionportal.R;
public class SearchMain extends Activity {
	
	private AutoCompleteTextView edittext; //搜索框
	private Boolean showHistory = false; //是否显示搜索历史

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		((LinearLayout) findViewById(R.id.mainview)).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.qrcode)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getBaseContext(), CaptureActivity.class), 0);
			}
			
		});
		
		search();
	    initAutoComplete();

	}

	//点击搜索
	public void search(){
		edittext = (AutoCompleteTextView) findViewById(R.id.search_edit);

		((Button) findViewById(R.id.search_btn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(((Button) findViewById(R.id.search_btn)).getWindowToken(), 0);

				String text = edittext.getText().toString();
				if(TextUtils.isEmpty(text)){
					Toast.makeText(getApplicationContext(), "请输入搜索的关键字", 0).show();
				}else{
					SharedPreferences sp = getSharedPreferences("search_name", 0);
					String longhistory = sp.getString("product_history", "");
					if (!longhistory.contains(text + ",")) {
						StringBuilder builder = new StringBuilder(longhistory);
						builder.insert(0, text + ",");
						sp.edit().putString("product_history", builder.toString()).commit();
					}		
					edittext.setText("");
						Intent intent = new Intent(getApplicationContext(), SearchResult.class);
						intent.putExtra("title", "搜索结果");
						intent.putExtra("type", "search");
						intent.putExtra("typeid", "0");
						intent.putExtra("searchstr", text.trim());
						startActivity(intent);
				}
				
			}
		});
	}
	//初始化历史记录
	private void initAutoComplete() {
		SharedPreferences sp = getSharedPreferences("search_name", 0);
		String longhistory = sp.getString("product_history", "");
		final String[] histories = longhistory.split(",");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.search_history, histories);
		for (int i = 0; i < histories.length; i++) {
			if(histories[i] != "")
				showHistory = true;
		}
		// 只保留最近的10条的记录
		if (histories.length > 10) {
			String[] newHistories = new String[5];
			System.arraycopy(histories, 0, newHistories, 0, 5);
			adapter = new ArrayAdapter<String>(this, R.layout.search_history, newHistories);
		}
		edittext.setAdapter(adapter);
		
		edittext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AutoCompleteTextView view = (AutoCompleteTextView) v;
				if (showHistory) {
					view.showDropDown();
				}
			}
		});
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1){
			String mresult = data.getExtras().getString("mresult");
			Log.i("fly", mresult);
			if(mresult.contains("html")){
				Intent intent = new Intent(getApplicationContext(), ProductDetails.class);
				intent.putExtra("guid", mresult.split("html")[1].substring(1));
				startActivity(intent);
			}else{
				Toast.makeText(getApplicationContext(), "没有找到该商品", Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
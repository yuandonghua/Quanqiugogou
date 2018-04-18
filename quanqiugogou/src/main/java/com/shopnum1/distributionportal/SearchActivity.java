package com.shopnum1.distributionportal;
//搜索
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zxing.activity.CaptureActivity;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;
public class SearchActivity extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private JSONArray CatagoryList1; //一级分类列表
	private JSONArray CatagoryList2; //二级分类列表
	private JSONArray CatagoryList3; //二级分类列表
	private AutoCompleteTextView edittext; //搜索框
	private Boolean showHistory = false; //是否显示搜索历史
	private DrawerLayout mDrawerLayout; //抽屉布局
	private Dialog pBar; //加载进度
	private int ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		initLayout();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		httpget.getNetwork(this); //判断网络
		if(HttpConn.isNetwork) {
			if(HttpConn.isLogin)
				if(HttpConn.cartNum > 0){
					((TextView)findViewById(R.id.num)).setVisibility(View.VISIBLE);
					((TextView)findViewById(R.id.num)).setText(HttpConn.cartNum+"");
				} else {
					((TextView)findViewById(R.id.num)).setVisibility(View.GONE);
				}
			search();
		    initAutoComplete();
		    getCatagory(0, 1);
		} else {
			httpget.setNetwork(this); //设置网络
		}
	}
	//初始化
	public void initLayout() {
		//主页
		((RelativeLayout) findViewById(R.id.imageButton1)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getBaseContext(), MainActivity.class));
				overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}

		});
		//购物车
		((RelativeLayout) findViewById(R.id.imageButton3)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(HttpConn.isLogin){
					startActivity(new Intent(getBaseContext(), CartActivity.class));
					overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				} else {
					Intent intent = new Intent(getApplicationContext(), UserLogin.class);
					intent.putExtra("cart", "");
					startActivity(intent);
				}	
			}

		});
		//发现
		((RelativeLayout) findViewById(R.id.find_image)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(HttpConn.isLogin){
					startActivity(new Intent(getBaseContext(), FindActivity.class));
					overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				} else {
					Intent intent = new Intent(getApplicationContext(), UserLogin.class);
					intent.putExtra("cart", "");
					startActivity(intent);
				}	
			}
			
		});
		//个人中心
		((RelativeLayout) findViewById(R.id.imageButton4)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(HttpConn.isLogin){
					startActivity(new Intent(getBaseContext(), MemberActivity.class));
					overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				} else {
					Intent intent = new Intent(getApplicationContext(), UserLogin.class);
					intent.putExtra("person", "");
					startActivity(intent);
				}	
			}

		});
		//二维码
		((LinearLayout) findViewById(R.id.qrcode)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(getBaseContext(), CaptureActivity.class), 0);
			}
			
		});
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
					Toast.makeText(getApplicationContext(), "请输入搜索的关键字", Toast.LENGTH_SHORT).show();
				}else{
					SharedPreferences sp = getSharedPreferences("search_name", 0);
					String longhistory = sp.getString("product_history", "");
					if (!longhistory.contains(text + ",")) {
						StringBuilder builder = new StringBuilder(longhistory);
						builder.insert(0, text + ",");
						sp.edit().putString("product_history", builder.toString()).commit();
					}		
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
	//获取分类
	public void getCatagory(final int id, final int index){
		pBar = new Dialog(this, R.style.dialog);
		pBar.setContentView(R.layout.progress);
		pBar.show();
		new Thread(){
			public void run(){
				String productCatagoryUrl =  "/api/productcatagory/?id=" + id + "&AppSign=" + HttpConn.AppSign
		    			+"&AgentID="+MyApplication.agentId +"&sbool=true";
				StringBuffer result= httpget.getArray(productCatagoryUrl);
				try {
					if(id == 0){
						JSONObject object = new JSONObject();
						object.put("Name", "品牌中心");
						object.put("Description", "时尚/大牌");
						object.put("BackgroundImage", "assets://brand.png");
						CatagoryList1 = new JSONArray();
						CatagoryList1.put(object);
						JSONArray array = new JSONObject(result.toString()).getJSONArray("Data");
						for (int i = 0; i < array.length(); i++) {
							CatagoryList1.put(array.getJSONObject(i));
						}
					} else {
						CatagoryList2 = new JSONObject(result.toString()).getJSONArray("Data");
					}	
					Message msg = Message.obtain();
					msg.what = index;
					handler.sendMessage(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public void getCatagory3(final int id, final String name){
		new Thread(){
			public void run(){
				String productCatagoryUrl =  "/api/productcatagory/?id=" + id + "&AppSign=" + HttpConn.AppSign
		    			+"&AgentID="+MyApplication.agentId +"&sbool=true";
				StringBuffer result= httpget.getArray(productCatagoryUrl);
				try {
					CatagoryList3 = new JSONObject(result.toString()).getJSONArray("Data");
					Intent intent = new Intent();
					intent.putExtra("title", name);
					intent.putExtra("CatagoryList3", CatagoryList3.toString());
					intent.putExtra("typeid", id+"");
					intent.setClass(getBaseContext(), SearchActivity2.class);
					startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			pBar.dismiss();
			switch (msg.what) {
			case 1:
				addList1();
				break;
			case 2:
				addList2();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	//一级分类列表
	public void addList1(){
		ListView listview1 = (ListView)findViewById(R.id.listview1);
		listview1.setSelector(new ColorDrawable(Color.TRANSPARENT));
		listview1.setAdapter(new CatagoryAdapter(CatagoryList1, 0));
		listview1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				if(position == 0){
					startActivity(new Intent(getApplicationContext(), BrandCenter.class));
				} else {
					TextView catagory_name = (TextView)v.findViewById(R.id.name1);
					((TextView)findViewById(R.id.catagory_name)).setText(catagory_name.getText().toString());
					try {
						ID = CatagoryList1.getJSONObject(position).getInt("ID");
						getCatagory(ID, 2);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	//二级分类列表
	public void addList2(){
		final TextView catagoryName = (TextView)findViewById(R.id.catagory_name);
		catagoryName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(), SearchResult.class);
				intent.putExtra("title", catagoryName.getText().toString());
				intent.putExtra("type", "list");
				intent.putExtra("typeid", ID+"");
				intent.putExtra("searchstr", "");
				startActivity(intent);
			}
		});
		
		ListView listview2 = (ListView)findViewById(R.id.listview2);
		listview2.setSelector(new ColorDrawable(Color.TRANSPARENT));
		listview2.setAdapter(new CatagoryAdapter(CatagoryList2, 1));
		mDrawerLayout.openDrawer(Gravity.RIGHT);
		listview2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				try {
					String name = CatagoryList2.getJSONObject(position).getString("Name");
					int id = CatagoryList2.getJSONObject(position).getInt("ID");
					getCatagory3(id, name);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	//列表适配器
	class CatagoryAdapter extends BaseAdapter{
		
		JSONArray CatagoryList;
		int id;
		public CatagoryAdapter(JSONArray CatagoryList, int id){
			this.CatagoryList = CatagoryList;
			this.id = id;
		}

		@Override
		public int getCount() {

			return CatagoryList.length();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null) {
				if(id == 0) {
					convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.catagory_item, null);
					convertView.setBackgroundResource(R.drawable.searchbg);
				} else {
					convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.catagory_item2, null);
					convertView.setBackgroundResource(R.drawable.searchbg2);
				}	
			}
			try {
				((TextView)convertView.findViewById(R.id.name1)).setText(CatagoryList.getJSONObject(position).getString("Name"));
				((TextView)convertView.findViewById(R.id.name2)).setText(CatagoryList.getJSONObject(position).getString("Description").replace("1", ""));
				
				if(id == 0){
					ImageView imageview = (ImageView)convertView.findViewById(R.id.icon);
					
					if(HttpConn.showImage){
						Log.i("fly", CatagoryList.getJSONObject(position).getString("BackgroundImage").replace("..", ""));
						ImageLoader.getInstance().displayImage(CatagoryList.getJSONObject(position).getString("BackgroundImage").replace("..", ""), imageview, MyApplication.options);
					}	
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return convertView;
		}
		
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
	
	//返回主页
	@Override
	public void onBackPressed() {
		if(mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
			mDrawerLayout.closeDrawer(Gravity.RIGHT);
		} else {
			startActivity(new Intent(getBaseContext(), MainActivity.class));
		}
	}

}
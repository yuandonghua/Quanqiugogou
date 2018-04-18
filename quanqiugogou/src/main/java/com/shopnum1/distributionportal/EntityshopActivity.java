package com.shopnum1.distributionportal;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class EntityshopActivity extends Activity {
	private DisplayImageOptions options;
	static EntityshopActivity instance;
	private float SCREEN_WIDTH = 0;//屏幕的宽度
	private LinearLayout group;
	private ListView adresslistView;
	private ListView StorelistView;
	private MyShopadapter shopadapter;
	private MyCityadapter adressadapter;
	private List<Map<String, String>> leftdata=new ArrayList<Map<String,String>>();
	private List<Map<String, String>> rightdata=new ArrayList<Map<String,String>>();
	private HttpConn httpget = new HttpConn();
	private Boolean isfirst=true;
	private EditText rl_search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entityshopactivity);
        DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		SCREEN_WIDTH = dm.widthPixels;
        instance = this;
        initLayout();
      
	}
    @Override
	protected void onResume() {
		if (HttpConn.isNetwork) {
			if (HttpConn.isLogin)
				if (HttpConn.cartNum > 0) {
					((TextView) findViewById(R.id.num)).setVisibility(View.VISIBLE);
					((TextView) findViewById(R.id.num)).setText(HttpConn.cartNum + "");
				} else {
					((TextView) findViewById(R.id.num)).setVisibility(View.GONE);
				}
		} else {
			httpget.setNetwork(this); // 设置网络
		}
		super.onResume();
	}

    //初始化
	public void initLayout(){
		
		rl_search=(EditText) findViewById(R.id.rl_search);
		rl_search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		rl_search.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if (arg1==EditorInfo.IME_ACTION_SEARCH ||(arg2!=null&&arg2.getKeyCode()== KeyEvent.KEYCODE_ENTER)) {                
					// 先隐藏键盘
					((InputMethodManager) rl_search.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(EntityshopActivity.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
					Intent intent = new Intent(getApplicationContext(), SearchResult.class);
					intent.putExtra("title", "搜索结果");
					intent.putExtra("type", "search");
					intent.putExtra("typeid", "0");
					intent.putExtra("searchstr", rl_search.getText().toString().trim());
					startActivity(intent);			
					return true;             
				}  
					return false;
			}
		});
		// 主界面
		((RelativeLayout) findViewById(R.id.imageButton1))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(getBaseContext(),MainActivity.class));
						overridePendingTransition(android.R.anim.fade_in,
								android.R.anim.fade_out);
						if (!HttpConn.isNetwork)
							finish();
					}

				});
//		((RelativeLayout) findViewById(R.id.sousuo))
//		.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				startActivity(new Intent(getBaseContext(),
//						SearchMain.class));
//				overridePendingTransition(android.R.anim.fade_in,
//						android.R.anim.fade_out);
//				if (!HttpConn.isNetwork)
//					finish();
//			}
//
//		});

		

		// 购物车
		((RelativeLayout) findViewById(R.id.imageButton3))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (HttpConn.isLogin) {
							startActivity(new Intent(getBaseContext(),CartActivity.class));
							overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
						} else {
							Intent intent = new Intent(getApplicationContext(),UserLogin.class);
							intent.putExtra("cart", "");
							startActivity(intent);
						}
						if (!HttpConn.isNetwork)
							finish();
					}

				});
	

		// 个人中心
		((RelativeLayout) findViewById(R.id.imageButton4))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (HttpConn.isLogin) {
							startActivity(new Intent(getBaseContext(),
									MemberActivity.class));
							overridePendingTransition(android.R.anim.fade_in,
									android.R.anim.fade_out);
						} else {
							Intent intent = new Intent(getApplicationContext(),UserLogin.class);
							intent.putExtra("person", "");
							startActivity(intent);
						}
						if (!HttpConn.isNetwork)
							finish();
					}

				});
		getdata();
		adresslistView=(ListView)findViewById(R.id.adresslistView);	
	
		group=(LinearLayout) findViewById(R.id.viewGroup);
		adresslistView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);		
		adressadapter=new MyCityadapter(leftdata); 
		adresslistView.setAdapter(adressadapter);
		adresslistView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				adresslistView.setItemChecked(arg2, true);
				isfirst=false;
					adresslistView.setBackgroundColor(Color.parseColor("#f6f6f6"));
					StorelistView.setVisibility(View.VISIBLE);
					
				
				getdata1(leftdata.get(arg2).get("ID"));	
			}
		});
		StorelistView=(ListView)findViewById(R.id.storelistView);
		
		
		shopadapter=new MyShopadapter(rightdata); 
		StorelistView.setAdapter(shopadapter);
		StorelistView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(), SearchResult.class);
				intent.putExtra("title", rightdata.get(arg2).get("Name"));
				intent.putExtra("type", "list");
				intent.putExtra("typeid", Integer.parseInt( rightdata.get(arg2).get("ID")));
				intent.putExtra("searchstr", "");
				startActivity(intent);
				
				
			}
		});
		
     
		//参数配置
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).threadPoolSize(3)
        		.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().build();
        ImageLoader.getInstance().init(config);
        options = new DisplayImageOptions.Builder()
        	.imageScaleType(ImageScaleType.EXACTLY).showImageOnLoading(R.drawable.pic1).showImageForEmptyUri(R.drawable.pic1)
        	.showImageOnFail(R.drawable.pic1).cacheInMemory(true).cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();
  
  		
	}
	
    private void getdata() {
    	HttpUtils hu = new HttpUtils();
    	String productCatagoryUrl = HttpConn.hostName+ "/api/productcatagory/?id=0&AppSign="+HttpConn.AppSign+"&AgentID="+MyApplication.agentId +"&sbool=true";
		hu.send(HttpMethod.GET,productCatagoryUrl ,
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
					}

					@Override
					public void onSuccess(ResponseInfo<String> data) {
					  if(!"".equals(data.result.toString())){
						  try {
							JSONObject object=new JSONObject(data.result.toString());
							leftdata.clear();
							JSONArray array=object.optJSONArray("Data");
							for(int i=0;i<array.length();i++){
								Map<String, String> map=new HashMap<String, String>();
								JSONObject mObject=array.optJSONObject(i);
								map.put("Name", mObject.optString("Name"));
								map.put("ID", mObject.optString("ID"));
								leftdata.add(map);
							}
							adressadapter.notifyDataSetChanged();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						  
					  }
						
					}
				});

	}
    private void getdata1(String id) {
        
		// TODO Auto-generated method stub   
    	
    	HttpUtils hu = new HttpUtils();
    	String productCatagoryUrl =  HttpConn.hostName+ "/api/productcatagory/?id="+id+"&AppSign="+HttpConn.AppSign
    			+"&AgentID="+MyApplication.agentId +"&sbool=true";
		hu.send(HttpMethod.GET, productCatagoryUrl,
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
					}

					@Override
					public void onSuccess(ResponseInfo<String> data) {
					  if(!"".equals(data.result.toString())){
						  try {
							JSONObject object=new JSONObject(data.result.toString());
							rightdata.clear();
							JSONArray array=object.optJSONArray("Data");
							for(int i=0;i<array.length();i++){
								Map<String, String> map=new HashMap<String, String>();
								JSONObject mObject=array.optJSONObject(i);
								map.put("Name", mObject.optString("Name"));
								map.put("ID", mObject.optString("ID"));
								map.put("type", "0");//第一层
								rightdata.add(map);
								
								JSONArray array2= mObject.optJSONArray("Description");
								for (int j = 0; j < array2.length(); j++) {
									Map<String, String> map2=new HashMap<String, String>();
									JSONObject mObject2=array2.optJSONObject(j);
									map2.put("Name", mObject2.optString("Name"));
									map2.put("ID", mObject2.optString("ID"));
									map2.put("type", "1");//第二层
									rightdata.add(map2);
								}
								
								
							}
							 List<Map<String, String>> temp = rightdata;
							shopadapter.notifyDataSetChanged();
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						  
					  }
						
					}
				});

	}
	public class MyShopadapter extends BaseAdapter {
		private List<Map<String, String>> data;
		private LayoutInflater inflate;
		Context context;
		private final int FISRT_TYPE = 0 ;
		private final int SENCOND_TYPE = 1 ;
		public MyShopadapter( List<Map<String, String>> data) {
			this.data = data;
			inflate = LayoutInflater.from(EntityshopActivity.this);
		
			
		}

		
		@Override
		public int getItemViewType(int position) {
			int type = Integer.valueOf(data.get(position).get("type"));
			return type;
		}


		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 2;
		}


		public int getCount() {
			
			if(data==null)
				return 0;
			return data.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflate.inflate(R.layout.entryshopitem, null);
				holder.name = (TextView) convertView.findViewById(R.id.shopname);
			
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
		
			holder.name.setText(data.get(position).get("Name"));
			
			int type = getItemViewType(position);
			switch (type) {
			case FISRT_TYPE:
				holder.name.setBackgroundResource(R.drawable.round_gray_line);
				holder.name.setGravity(Gravity.CENTER_HORIZONTAL);
				break;
			case SENCOND_TYPE:
				
				break;

			default:
				break;
			}
			
			return convertView;
		}

	}
	public class MyCityadapter extends BaseAdapter {
		private List<Map<String, String>> data;
		private LayoutInflater inflate;
		Context context;
		
		public MyCityadapter(List<Map<String, String>> data) {
			this.data = data;
			inflate = LayoutInflater.from(EntityshopActivity.this);
		
			
		}

		public int getCount() {
			if(data==null)
				return 0;
			return data.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflate.inflate(R.layout.entryshopitem1, null);
				holder.city = (TextView) convertView.findViewById(R.id.city);
			//	holder.view_left=(View)convertView.findViewById(R.id.view_left);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			SparseBooleanArray booleanArray = adresslistView.getCheckedItemPositions();
			if (booleanArray != null && booleanArray.size() > 0) {
				boolean isChecked = booleanArray.get(position);
			//	holder.view_left.setBackgroundColor(Color.parseColor(isChecked ? "#eb0028" : "#BFBFBF"));
				
				if(isfirst){
					convertView.findViewById(R.id.rl_item).setBackgroundColor(Color.parseColor("#ffffff"));
				}else{
					if (isChecked){
						convertView.findViewById(R.id.rl_item).setBackgroundColor(Color.parseColor("#ffffff"));
				     	holder.city.setTextColor(Color.parseColor("#e9544d"));}
					else{
						holder.city.setTextColor(Color.parseColor("#888888"));	
						convertView.findViewById(R.id.rl_item).setBackgroundResource(R.drawable.round_gray_line);
					}
				}
				
			}
		
			holder.city.setText(data.get(position).get("Name"));
			
			
			return convertView;
		}

	}


	private class ViewHolder
	{
	
		TextView name,distance,city;
		View view_left,view_right;

	}

	
}

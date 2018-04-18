package com.shopnum1.distributionportal;
//订单详情
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
public class OrderShow extends Activity {
	
	private HttpConn httpget = new HttpConn();
	private static List<Bitmap> bmpList = new ArrayList<Bitmap>();
	private GridAdapter adapter;
	private Dialog pBar; //加载进度
	private ImageView iv_delete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_show);
		initLayout();
	}
	//初始化
	public void initLayout() {
		//返回
		((LinearLayout) findViewById(R.id.back)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bmpList = new ArrayList<Bitmap>();
				finish();
			}
		});

		GridView gridview = (GridView)findViewById(R.id.gridview);
		adapter = new GridAdapter();
		gridview.setAdapter(adapter);
		
		((Button)findViewById(R.id.add)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(bmpList.size() == 3){
					Toast.makeText(OrderShow.this, "最多只能添加3张", Toast.LENGTH_LONG).show();
				} else {
					final String[] items = { "相机拍摄", "手机相册" };
					new AlertDialog.Builder(OrderShow.this).setTitle("设置").setItems(items, new DialogInterface.OnClickListener() {
						@Override
						 public void onClick(DialogInterface dialog, int which) {
							Log.i("fly", bmpList.toString());
							if (which == 0) {
				    			 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				    			 startActivityForResult(intent, 1);
			    			 } else {
			    				 try {
			    					 Intent intent = new Intent(Intent.ACTION_PICK, 
			    							 android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			    					 startActivityForResult(intent, 2);
			    				 } catch (ActivityNotFoundException e) {
			    					 Toast.makeText(OrderShow.this, "未能找到照片", Toast.LENGTH_LONG).show();
			    				 }
			    			 }
			    		}
					}).show();
				}
			}
		});
		
		((LinearLayout)findViewById(R.id.more)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(iv_delete != null){
					iv_delete.setVisibility(View.GONE);
				}
				final String text1 = ((EditText)findViewById(R.id.text1)).getText().toString();
				final String text2 = ((EditText)findViewById(R.id.text2)).getText().toString();
				if(text1.equals("")){
					Toast.makeText(OrderShow.this, "请输入标题", Toast.LENGTH_LONG).show();
				} else if(text2.equals("")){
					Toast.makeText(OrderShow.this, "请输入购物心得", Toast.LENGTH_LONG).show();
				} else {
					pBar = new Dialog(OrderShow.this, R.style.dialog);
					pBar.setContentView(R.layout.progress);
					pBar.show();
					
					new Thread(){
						public void run(){
							String urlStr = "";
							if(bmpList.size() > 0){
								for (int i = 0; i < bmpList.size(); i++) {
									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									bmpList.get(i).compress(Bitmap.CompressFormat.JPEG, 100, baos);
									StringBuffer result = httpget.postData2("/Api/uploadpic.ashx", "filedata=" + Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT).replace("+", "%2B").trim() + "&memloginid=" + HttpConn.UserName);
									try {
										if(i == bmpList.size()-1)
											urlStr += new JSONObject(result.toString()).getString("success");
										else
											urlStr += new JSONObject(result.toString()).getString("success")+"|";
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
							Log.i("fly", urlStr);
							Message msg = Message.obtain();
							try {
								JSONObject product = new JSONObject(getIntent().getStringExtra("ProductList"));
								JSONObject object = new JSONObject();
								object.put("MemLoginID", HttpConn.UserName);
								object.put("ProductGuid", product.getString("ProductGuid"));
								object.put("OrderNumber", product.getString("OrderNumber"));
								object.put("Name", product.getString("NAME"));
								object.put("Title", text1);
								object.put("Content", text2);
								object.put("Image", urlStr);
								object.put("AppSign", HttpConn.AppSign);
								object.put("IsAudit", 0);
								object.put("CreatUser", HttpConn.username);
								object.put("ModifyUser", HttpConn.username);
								object.put("IsDeleted", 0);
								object.put("IsAgentId", null);
								Log.i("fly", object.toString());
								StringBuffer result = httpget.postJSON("/api/addBaskorderlog/", object.toString());
//								StringBuffer result =httpget.getArray("/api/addBaskorderlog/?MemLoginID="+HttpConn.username+"&Title="+text1+"&Content="+text2+"&IsAgentId="+null);
								Log.i("fly", result.toString());
								msg.obj = new JSONObject(result.toString()).getString("return");
								msg.what = 1;
								handler.sendMessage(msg);
							} catch (JSONException e) {
								msg.what = 0;
								e.printStackTrace();
							}	
						}
					}.start();
				}
			}
		});
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				pBar.dismiss();
				break;
			case 1:
				pBar.dismiss();
				if(msg.obj.equals("202")){
					Toast.makeText(getApplicationContext(), "发表成功", Toast.LENGTH_SHORT).show();	
				} else if(msg.obj.equals("909")){
					Toast.makeText(getApplicationContext(), "已有一条晒单记录", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "发表失败", Toast.LENGTH_SHORT).show();
				}
				setResult(1, getIntent());
				finish();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Bitmap bitmap = null;
			if(requestCode == 1){
				bitmap = data.getParcelableExtra("data");
			} else {
				Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null); 
				cursor.moveToFirst(); 
				String imgPath = cursor.getString(1); 
				Options options = new BitmapFactory.Options(); 
				options.inJustDecodeBounds = false; 
				options.inSampleSize = 10; 
				bitmap = BitmapFactory.decodeFile(imgPath, options);
			}
			bmpList.add(bitmap);
			adapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	class GridAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			
			return bmpList.size();
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
		public View getView(final int arg0, View convertView, ViewGroup arg2) {
			if(convertView == null) {
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_show2, null);
			}
			ImageView img = (ImageView)convertView.findViewById(R.id.img);
			img.setImageBitmap(bmpList.get(arg0));
			iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
			iv_delete.setVisibility(View.VISIBLE);
			convertView.findViewById(R.id.iv_delete).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					bmpList.remove(arg0);
					notifyDataSetChanged();
				}
			});
			return convertView;
		}
		
	}

	@Override
	public void onBackPressed() {
		bmpList = new ArrayList<Bitmap>();
		super.onBackPressed();
	}
	
}
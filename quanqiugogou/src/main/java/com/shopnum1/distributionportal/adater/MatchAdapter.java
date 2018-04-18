package com.shopnum1.distributionportal.adater;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.MyApplication;

public class MatchAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<JSONObject> list_data;
	private int width;
	DisplayImageOptions options;
	public MatchAdapter(Context context, ArrayList<JSONObject> list_data,
			int width) {
		this.context = context;
		this.list_data = list_data;
		this.width = width;
		

		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.banner) // 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.banner) // 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.banner) // 设置图片加载或解码过程中发生错误显示的图片
				.cacheInMemory(true) // default 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true) // default 设置下载的图片是否缓存在SD卡中
				.considerExifParams(false) // default
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) // default
				.bitmapConfig(Bitmap.Config.ARGB_8888) // default 设置图片的解码类型
				.handler(new Handler()) // default
				.build();
	}

	@Override
	public int getCount() {
		return list_data.size();
	}

	@Override
	public Object getItem(int position) {
		return list_data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if (convertView == null) {
			vh = new ViewHolder();
			convertView = View.inflate(context, R.layout.match_item, null);
			vh.iv_img = (ImageView) convertView.findViewById(R.id.iv_img);
			vh.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
			vh.tv_price = (TextView) convertView.findViewById(R.id.tv_price);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		try {
			vh.tv_price.setText("￥"
					+ list_data.get(position).getDouble("ShopPrice"));
			vh.tv_name.setText(list_data.get(position).getString("Name"));
			LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) vh.iv_img
					.getLayoutParams();
			linearParams.height = (width - dip2px(context, 2)) / 2;
			vh.iv_img.setLayoutParams(linearParams);
//			ImageLoader.getInstance().
			String imgeUrl = list_data.get(position).getString("OriginalImge");
			imgeUrl = imgeUrl.replace("90x90", "300x300");
			ImageLoader.getInstance().displayImage(
					imgeUrl,
					vh.iv_img,options);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}

	static class ViewHolder {
		private ImageView iv_img;
		private TextView tv_name;
		private TextView tv_price;
	}

	public int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
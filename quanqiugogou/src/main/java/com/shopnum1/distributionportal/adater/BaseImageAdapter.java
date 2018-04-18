package com.shopnum1.distributionportal.adater;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;

public class BaseImageAdapter extends BaseAdapter {
	private String[] list_data;
	private Context context;
	private DisplayImageOptions options;
	private int width;

	public BaseImageAdapter(String[] list_data, Context context, int width) {
		this.list_data = list_data;
		this.context = context;
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.pic1) // 设置图片下载期间显示的图片
				.showImageForEmptyUri(R.drawable.pic1) // 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.pic1) // 设置图片加载或解码过程中发生错误显示的图片
				.cacheInMemory(true) // default 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true) // default 设置下载的图片是否缓存在SD卡中
				.considerExifParams(false) // default
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED) // default
				.bitmapConfig(Bitmap.Config.ARGB_8888) // default 设置图片的解码类型
				.handler(new Handler()) // default
				.build();
	}
	
	String[] getImagePath(){
		return list_data;
	}

	@Override
	public int getCount() {
		return list_data.length;
	}

	@Override
	public Object getItem(int position) {
		return list_data[position];
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
			convertView = View.inflate(context, R.layout.comment_image_item,
					null);
			vh.type_image = (ImageView) convertView
					.findViewById(R.id.type_image);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		ImageLoader.getInstance().displayImage(
				HttpConn.urlName + list_data[position] + "", vh.type_image,
				options);
		return convertView;
	}

	static class ViewHolder {
		ImageView type_image;
	}

	public int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

}

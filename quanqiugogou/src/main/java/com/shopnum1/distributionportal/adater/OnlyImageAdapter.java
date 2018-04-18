package com.shopnum1.distributionportal.adater;

import java.util.ArrayList;

import com.shopnum1.distributionportal.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnCreateContextMenuListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OnlyImageAdapter extends BaseAdapter {
	private ArrayList<String> list_data;
	private Context context;

	public OnlyImageAdapter(ArrayList<String> list_data, Context context) {
		// TODO Auto-generated constructor stub
		this.list_data = list_data;
		this.context = context;
	}

	@Override
	public int getCount() {
		return 8;
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
			convertView = View.inflate(context, R.layout.type_item, null);
			vh.type_image = (ImageView) convertView
					.findViewById(R.id.type_image);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		return convertView;
	}
	static class ViewHolder {
		ImageView type_image;
	}
}

package com.shopnum1.distributionportal.adater;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shopnum1.distributionportal.PhotoshowActivity;
import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.NoScrollGridView;
import com.shopnum1.distributionportal.util.RoundImageView;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

public class CommentAdapter extends BaseAdapter {
	private ArrayList<JSONObject> list_data;
	private Context context;
	private ArrayList<BaseAdapter> adapter;

	public CommentAdapter(ArrayList<JSONObject> list_data, Context context,
			ArrayList<BaseAdapter> adapter) {
		this.list_data = list_data;
		this.context = context;
		this.adapter = adapter;
	}

	@Override
	public int getCount() {
		return list_data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if (convertView == null) {
			vh = new ViewHolder();
			convertView = View.inflate(context, R.layout.comment_item, null);
			vh.user_name = (TextView) convertView.findViewById(R.id.user_name);
			vh.user_comment = (TextView) convertView.findViewById(R.id.user_comment);
			vh.user_head = (RoundImageView) convertView.findViewById(R.id.user_head);
			vh.user_time = (TextView) convertView.findViewById(R.id.user_time);
			vh.user_params = (TextView) convertView.findViewById(R.id.user_params);
			vh.user_rating = (RatingBar) convertView.findViewById(R.id.user_rating);
			vh.image_grid = (NoScrollGridView) convertView.findViewById(R.id.image_grid);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		try {
			vh.user_rating.setRating(list_data.get(position).getInt("rank"));
			vh.image_grid.setAdapter(adapter.get(position));
			vh.image_grid.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
					Intent x=new Intent(context, PhotoshowActivity.class);
					BaseImageAdapter madapter=(BaseImageAdapter) adapter.get(position);
					x.putExtra("icon", madapter.getImagePath());
					x.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
					x.putExtra("selection", 0);
					context.startActivity(x);
			//	(()context).overridePendingTransition(R.anim.wave_scale,R.anim.my_alpha_action);
				}
			});
			ImageLoader.getInstance().displayImage(HttpConn.hostName+ list_data.get(position).getString("memphoto"),vh.user_head);
			vh.user_comment.setText(list_data.get(position).getString("content"));
			vh.user_params.setText(list_data.get(position).getString("attributes"));
			vh.user_name.setText(list_data.get(position).getString("memname"));
			vh.user_time.setText(list_data.get(position).getString("sendtime"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return convertView;
	}

	static class ViewHolder {
		private TextView user_name;
		private TextView user_comment;
		private TextView user_params;
		private TextView user_time;
		private RatingBar user_rating;
		private RoundImageView user_head;
		private NoScrollGridView image_grid;
	}
}

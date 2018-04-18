package com.shopnum1.distributionportal.adater;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.shopnum1.distributionportal.BannerWeb;
import com.shopnum1.distributionportal.MainActivity;

public class GGPagerAdapter extends PagerAdapter {
	private ArrayList<View> viewList;
	JSONArray gg_list;
	private Context context;

	public GGPagerAdapter(ArrayList<View> viewList, JSONArray gg_list,
			Context context) {
		this.viewList = viewList;
		this.gg_list = gg_list;
		this.context = context;
	}


	@Override
	public int getCount() {
		if (viewList.size() > 3) {
			return Integer.MAX_VALUE;
		}
		return viewList.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		if (viewList.size() > 3) {
			container.addView(viewList.get(position % viewList.size()));
			return viewList.get(position % viewList.size());
		}
		container.addView(viewList.get(position));

		viewList.get(position % viewList.size()).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(context, BannerWeb.class);
						try {
							intent.putExtra(
									"Url",
									(gg_list).getJSONObject(
											position % viewList.size())
											.getString("Url"));
						} catch (JSONException e) {
							e.printStackTrace();
						}
						context.startActivity(intent);
					}
				});
		return viewList.get(position);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (viewList.size() > 3) {
			container.removeView(viewList.get(position % viewList.size()));
		} else {
			container.removeView(viewList.get(position));
		}
	}
}
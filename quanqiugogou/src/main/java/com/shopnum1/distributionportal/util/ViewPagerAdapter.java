package com.shopnum1.distributionportal.util;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class ViewPagerAdapter extends PagerAdapter {
    List<View> pag = null;
    
    public ViewPagerAdapter(List<View> pag){
    	this.pag = pag;
    }
    
	@Override
	public int getCount() {
		return pag.size();
	}

	@Override
	public Object instantiateItem(View container, int position) {
		((ViewPager) container).addView(pag.get(position), position);
		return pag.get(position);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return (Object)arg0 == arg1;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((View)object);
	}

}
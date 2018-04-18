package com.shopnum1.distributionportal.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {
	private float xDistance, yDistance, xLast, yLast;
	private boolean mIsOverScrollEnabled = true;

	public void setOverScrollEnabled(boolean enabled) {
	    mIsOverScrollEnabled = enabled;
	}
	 
	public boolean isOverScrollEnabled() {
	    return mIsOverScrollEnabled;
	}

	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY,
	                               int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
	    return super.overScrollBy(
	            deltaX,
	            deltaY,
	            scrollX,
	            scrollY,
	            scrollRangeX,
	            scrollRangeY,
	            mIsOverScrollEnabled ? maxOverScrollX : 0,
	            mIsOverScrollEnabled ? maxOverScrollY : 0,
	            isTouchEvent);
	}

	 public interface OnScrollChangedListener {
	        void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt);
	    }
	 
	    private OnScrollChangedListener mOnScrollChangedListener;

	    @Override
	    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
	        super.onScrollChanged(l, t, oldl, oldt);
	        if (mOnScrollChangedListener != null) {
	            mOnScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
	        }
	    }
	 
	    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
	        mOnScrollChangedListener = listener;
	    }
	 



	public MyScrollView(Context context) {
		super(context);
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			xLast = ev.getX();
			yLast = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();

			xDistance += Math.abs(curX - xLast);
			yDistance += Math.abs(curY - yLast);
			xLast = curX;
			yLast = curY;

			if (xDistance > yDistance) {
				return false;
			}
		}

		return super.onInterceptTouchEvent(ev);
	}
}
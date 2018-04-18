package com.shopnum1.distributionportal.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

@SuppressWarnings("deprecation")
public class MyGallery extends Gallery {

	public MyGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		int keyCode;
		if (e2.getX() > e1.getX()) 
			keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
		else  
			keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
		onKeyDown(keyCode, null); 	
		return true;
	}
	
}

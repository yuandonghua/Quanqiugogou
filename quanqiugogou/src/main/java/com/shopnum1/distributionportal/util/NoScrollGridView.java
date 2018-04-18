package com.shopnum1.distributionportal.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class NoScrollGridView extends GridView{  
	  
    public NoScrollGridView(Context context, AttributeSet attrs){  
         super(context, attrs);  
    }  
 
    @Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){  
         int mExpandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);  
         super.onMeasure(widthMeasureSpec, mExpandSpec);  
    }  
} 

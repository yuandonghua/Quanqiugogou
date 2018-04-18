package com.shopnum1.distributionportal;

import com.shopnum1.distributionportal.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;

public class OrderActivity extends FragmentActivity implements OnClickListener {
	private MyPagerAdapter pagerAda;
	private ViewPager viewpager;
	private RadioButton rb_quanbu;
	private RadioButton rb_fukuan;
	private RadioButton rb_fahuo;
	private RadioButton rb_shouhuo;
	private RadioButton rb_pingjia;
	private ImageView back;
	private ImageView cursor;
	public int index;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_order);
		initLayout();
		index = getIntent().getIntExtra("type", 0);
		findViewById(R.id.order_back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void initLayout() {
		viewpager = (ViewPager) this.findViewById(R.id.viewPager);
		cursor = (ImageView) this.findViewById(R.id.cursor);
		rb_quanbu = (RadioButton) this.findViewById(R.id.rb_quanbu);
		rb_fahuo = (RadioButton) this.findViewById(R.id.rb_fahuo);
		rb_fukuan = (RadioButton) this.findViewById(R.id.rb_fukuan);
		rb_shouhuo = (RadioButton) this.findViewById(R.id.rb_shouhuo);
		rb_pingjia = (RadioButton) this.findViewById(R.id.rb_pingjia);
		back = (ImageView) this.findViewById(R.id.order_back);
		rb_quanbu.setOnClickListener(this);
		rb_fahuo.setOnClickListener(this);
		rb_fukuan.setOnClickListener(this);
		rb_shouhuo.setOnClickListener(this);
		rb_pingjia.setOnClickListener(this);
		pagerAda = new MyPagerAdapter(getSupportFragmentManager());
		viewpager.setAdapter(pagerAda);
		viewpager.setOffscreenPageLimit(0);
		viewpager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				switch (arg0) {
				case 0:
					rb_quanbu.setChecked(true);
					break;
				case 1:
					rb_fukuan.setChecked(true);
					break;
				case 2:
					rb_fahuo.setChecked(true);
					break;
				case 3:
					rb_shouhuo.setChecked(true);
					break;
				case 4:
					rb_pingjia.setChecked(true);
					break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		if (getIntent().getIntExtra("type", 0) == 8) {
			viewpager.setCurrentItem(4);
		} else if (getIntent().getIntExtra("type", 0) == 1) {
			viewpager.setCurrentItem(1);
		} else if (getIntent().getIntExtra("type", 0) == 2) {
			viewpager.setCurrentItem(2);
		} else if (getIntent().getIntExtra("type", 0) == 3) {
			viewpager.setCurrentItem(3);
		} else {
			viewpager.setCurrentItem(0);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rb_quanbu:
			viewpager.setCurrentItem(0);
			break;
		case R.id.rb_fukuan:
			viewpager.setCurrentItem(1);
			break;
		case R.id.rb_fahuo:
			index = 2;
			viewpager.setCurrentItem(2);
			break;
		case R.id.rb_shouhuo:
			index = 3;
			viewpager.setCurrentItem(3);
			break;
		case R.id.rb_pingjia:
			index = 8;
			viewpager.setCurrentItem(4);
			break;
		case R.id.back:
			this.finish();
			break;
		}
	}

	class MyPagerAdapter extends FragmentPagerAdapter {
		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int index) {
			Fragment f = null;
			switch (index) {
			case 0:
				index = 0;
				f = new OrderAllFragment(0);
				break;
			case 1:
				index = 1;
				f = new OrderAllFragment(1);
				break;
			case 2:
				index = 2;
				f = new OrderAllFragment(2);
				break;
			case 3:
				index = 3;
				f = new OrderAllFragment(3);
				break;
			case 4:
				index = 8;
				f = new AssessFragment();
				break;
			}
			return f;
		}

		@Override
		public int getCount() {
			return 5;
		}
	}
}

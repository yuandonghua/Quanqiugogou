/**
 * 
 */
package com.shopnum1.distributionportal;

import com.zxing.bean.Announcement;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

/**
 * @author brian
 *
 */
public class AnnouncementActivity extends Activity {
	Announcement announcement;
	TextView tileTv  ;
	TextView remarkTv  ;
	TextView createTimeTv  ;
	TextView modifyTimeTv  ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_announcement);
		 
		initView();
		initdata();
	}
	private void initView() {
		tileTv = (TextView) findViewById(R.id.tile_tv);
		remarkTv = (TextView) findViewById(R.id.remark_tv);
		createTimeTv = (TextView) findViewById(R.id.create_time_tv);
		modifyTimeTv = (TextView) findViewById(R.id.modify_time_tv);
		
	}
	private void initdata() {
	
		announcement =(Announcement) getIntent().getSerializableExtra("announcement");
		if(announcement == null ){
			this.finish();
		}
		tileTv.setText(announcement.getTitle());
		Spanned remark= Html.fromHtml(announcement.getRemark());
		remarkTv.setText(Html.fromHtml(remark.toString()));
		remarkTv.setMovementMethod(ScrollingMovementMethod.getInstance());
		createTimeTv.setText(announcement.getCreateTime());
		modifyTimeTv.setText(announcement.getModifyTime());
	}
}

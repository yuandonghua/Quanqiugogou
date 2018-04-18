package com.shopnum1.distributionportal;

import com.shopnum1.distributionportal.R;
import com.shopnum1.distributionportal.util.HttpConn;
import com.shopnum1.distributionportal.util.MyApplication;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class DevelopVipActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_developvip);
		initLayout();
	}
	private void initLayout() {
		findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showShare();
			}
		});
		findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		ImageView iv =  (ImageView) findViewById(R.id.iv);
		Bitmap bitmap;
		try {
			bitmap = Create2DCode(HttpConn.shareURL+"/Register.aspx?CommendPeople="+HttpConn.username+"&AgentID="+MyApplication.agentId);
			iv.setImageBitmap(bitmap);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}
	/** 
     * 用字符串生成二维码 
     */  
    public Bitmap Create2DCode(String str) throws WriterException {  
        BitMatrix matrix = new MultiFormatWriter().encode(str,BarcodeFormat.QR_CODE, 400, 400);  
        int width = matrix.getWidth();  
        int height = matrix.getHeight();  
        int[] pixels = new int[width * height];  
        for (int y = 0; y < height; y++) {  
            for (int x = 0; x < width; x++) {  
                if(matrix.get(x, y)){  
                    pixels[y * width + x] = 0xff000000;  
                }  
            }  
        }  
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);  
        return bitmap;  
    }
    /**
     * 分享
     */
    private void showShare() {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		oks.disableSSOWhenAuthorize();
		oks.setTitle(getString(R.string.share));
		oks.setTitleUrl(HttpConn.shareURL+"/Register.aspx?CommendPeople="+HttpConn.username+"&AgentID="+MyApplication.agentId);
		oks.setText(HttpConn.shareURL+"/Register.aspx?CommendPeople="+HttpConn.username+"&AgentID="+MyApplication.agentId);
		oks.setComment("我是测试评论文本");
		oks.setSite(getString(R.string.app_name));
		oks.setSiteUrl("http://sharesdk.cn");
		oks.show(this);
	}
}


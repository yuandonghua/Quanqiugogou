package com.shopnum1.distributionportal.util;

import android.app.Activity;
import android.os.CountDownTimer;
import android.widget.Button;

import com.shopnum1.distributionportal.R;

public class TimeCountUtil extends CountDownTimer {
    private Activity activity;
    private Button button;
    public TimeCountUtil(Activity activity, Button button, long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        this.activity=activity;
        this.button=button;
    }

    @Override
    public void onTick(long l) {
        //设置不能点击
        button.setClickable(false);
        //设置倒计时
        button.setText(l/1000+"S");
    }

    @Override
    public void onFinish() {
        button.setText("重新获取");
        button.setClickable(true);
    }
}

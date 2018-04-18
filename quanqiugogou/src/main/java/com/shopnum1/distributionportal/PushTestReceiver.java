package com.shopnum1.distributionportal;
import com.shopnum1.distributionportal.R;
import java.util.List;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.baidu.android.pushservice.PushMessageReceiver;

public class PushTestReceiver extends PushMessageReceiver{
	
	@Override
	public void onBind(Context arg0, int arg1, String arg2, String arg3,
			String arg4, String arg5) {
		
	}

	@Override
	public void onDelTags(Context arg0, int arg1, List<String> arg2,
			List<String> arg3, String arg4) {
		
	}

	@Override
	public void onListTags(Context arg0, int arg1, List<String> arg2,
			String arg3) {
		
	}

	@Override
	public void onMessage(Context arg0, String arg1, String arg2) {
		
	}

	@Override
	public void onNotificationArrived(Context arg0, String arg1, String arg2,
			String arg3) {
		
	}

	@Override
	public void onNotificationClicked(Context context, String arg1, String arg2,
			String arg3) {
		Log.i(TAG, "arg1="+arg1+"arg2="+arg2+"arg3="+arg3);
		  NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
          Notification notification = new Notification(R.drawable.app_logo, "您有新消息", System.currentTimeMillis());
          Intent i = new Intent();
          i.setAction(Intent.ACTION_VIEW);
          i.setData(Uri.parse("http://fxv811.groupfly.cn/MobileAndroid.aspx"));
          PendingIntent contentIntent = PendingIntent.getActivity(context, 100, i, 0);
          notification.setLatestEventInfo(context, arg1, arg2, contentIntent);
          notification.flags = Notification.FLAG_AUTO_CANCEL;
          nm.notify(100, notification);
	}

	@Override
	public void onSetTags(Context arg0, int arg1, List<String> arg2,
			List<String> arg3, String arg4) {
	}

	@Override
	public void onUnbind(Context arg0, int arg1, String arg2) {
		
	}

}

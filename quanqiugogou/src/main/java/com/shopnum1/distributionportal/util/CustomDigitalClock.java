package com.shopnum1.distributionportal.util;

import java.util.Calendar;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.DigitalClock;

@SuppressWarnings("deprecation")
public class CustomDigitalClock extends DigitalClock {
	Calendar mCalendar;
	private final static String m12 = "h:mm aa";
	private final static String m24 = "k:mm";
	private FormatChangeObserver mFormatChangeObserver;
	private Runnable mTicker;
	private Handler mHandler;
	private long endTime;
	public static long distanceTime;
	private ClockListener mClockListener;
	private boolean mTickerStopped;
	@SuppressWarnings("unused")
	private String mFormat;

	public CustomDigitalClock(Context context) {
		super(context);
		initClock(context);
	}

	public CustomDigitalClock(Context context, AttributeSet attrs) {
		super(context, attrs);
		initClock(context);
	}

	private void initClock(Context context) {
		if (mCalendar == null) {
			mCalendar = Calendar.getInstance();
		}
		mFormatChangeObserver = new FormatChangeObserver();
		getContext().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mFormatChangeObserver);
		setFormat();
	}

	@Override
	protected void onAttachedToWindow() {
		mTickerStopped = false;
		super.onAttachedToWindow();
		mHandler = new Handler();
		mTicker = new Runnable() {
			public void run() {
				if (mTickerStopped)
					return;
				long currentTime = System.currentTimeMillis();
				if (currentTime / 1000 == endTime / 1000 - 5 * 60) {
					mClockListener.remainFiveMinutes();
				}
				distanceTime = endTime - currentTime;
				distanceTime /= 1000;
				if (distanceTime <= 0) {
					setText("活动已结束");
					onDetachedFromWindow();
				} else {
					setText(dealTime(distanceTime));
				}
				invalidate();
				long now = SystemClock.uptimeMillis();
				long next = now + (1000 - now % 1000);
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();
	}

	public static Spanned dealTime(long time) {
		Spanned str;
		StringBuffer returnString = new StringBuffer();
		long day = time / (24 * 60 * 60);
		long hours = (time % (24 * 60 * 60)) / (60 * 60);
		long minutes = ((time % (24 * 60 * 60)) % (60 * 60)) / 60;
		long second = ((time % (24 * 60 * 60)) % (60 * 60)) % 60;
		String dayStr = String.valueOf(day);
		String hoursStr = String.valueOf(hours);
		String minutesStr = String.valueOf(minutes);
		String secondStr = timeStrFormat(String.valueOf(second));
		returnString.append(dayStr).append("天").append(hoursStr).append("时")
				.append(minutesStr).append("分").append(secondStr).append("秒");
		str = Html.fromHtml(returnString.toString());
		return str;
	}

	private static String timeStrFormat(String timeStr) {
		switch (timeStr.length()) {
		case 1:
			timeStr = "0" + timeStr;
			break;
		}
		return timeStr;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mTickerStopped = true;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	private boolean get24HourMode() {
		return android.text.format.DateFormat.is24HourFormat(getContext());
	}

	private void setFormat() {
		if (get24HourMode()) {
			mFormat = m24;
		} else {
			mFormat = m12;
		}
	}

	private class FormatChangeObserver extends ContentObserver {
		public FormatChangeObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {
			setFormat();
		}
	}

	public void setClockListener(ClockListener clockListener) {
		this.mClockListener = clockListener;
	}

	public interface ClockListener {
		void timeEnd();
		void remainFiveMinutes();
	}
	
}
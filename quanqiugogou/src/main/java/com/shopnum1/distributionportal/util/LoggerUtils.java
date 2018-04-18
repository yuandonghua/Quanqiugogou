package com.shopnum1.distributionportal.util;
public class LoggerUtils {

	private final static int VERBOSE = 0;
	private final static int DEBUG = 1;
	private final static int INFO = 2;
	private final static int WARN = 3;
	private final static int ERROR = 4;
	private final static int DEFAULT_LEVEL = -1;

	/**使用boolean值控制是否打印log**/
	private final static boolean isDebug = true;
	private int level;

	private final String clazz;

	private static final String TAG = "LogUtils";

	public static LoggerUtils getDebugLog(Class<?> clazz, int l) {
		LoggerUtils log = new LoggerUtils(clazz);
		log.level = l;
		return log;
	}

	public static LoggerUtils getLog(Class<?> clazz) {
		return new LoggerUtils(clazz);
	}

	public LoggerUtils(Class<?> clazz) {
		this.clazz = "[" + clazz.getSimpleName() + "] ";
		level = DEFAULT_LEVEL;
	}

	public void verbose(String message) {
		if(isDebug)
			verbose(message, null);
	}

	public void debug(String message) {
		if(isDebug)
			debug(message, null);
	}

	public void info(String message) {
		if(isDebug)
			info(message, null);
	}

	public void warn(String message) {
		if(isDebug)
			warn(message, null);
	}

	public void error(String message) {
		if(isDebug)
			error(message, null);
	}

	public void verbose(String message, Throwable t) {
		if (VERBOSE < level)
			return;
		if (message != null)
			android.util.Log.v(TAG, clazz + " 第" + getLineNumber() + "行 : " + message);
		if (t != null)
			android.util.Log.v(TAG, clazz + " 第" + getLineNumber() + "行 : " + t.toString());
	}

	public void debug(String message, Throwable t) {
		if (DEBUG < level)
			return;
		if (message != null)
			android.util.Log.d(clazz, clazz + " 第" + getLineNumber() + "行 : " + message);
		if (t != null)
			android.util.Log.d(clazz, clazz + " 第" + getLineNumber() + "行 : " + t.toString());
	}

	public void info(String message, Throwable t) {
		if (INFO < level)
			return;
		if (message != null)
			android.util.Log.i(TAG, clazz + " 第" + getLineNumber() + "行 : " + message);
		if (t != null)
			android.util.Log.i(TAG, clazz + " 第" + getLineNumber() + "行 : " + t.toString());
	}

	public void warn(String message, Throwable t) {
		if (WARN < level)
			return;
		if (message != null)
			android.util.Log.w(TAG, clazz + " 第" + getLineNumber() + "行 : " + message);
		if (t != null)
			android.util.Log.w(TAG, clazz + " 第" + getLineNumber() + "行 : " + t.toString());
	}

	public void error(String message, Throwable t) {
		if (ERROR < level)
			return;
		if (message != null)
			android.util.Log.e(TAG, clazz + " 第" + getLineNumber() + "行 : " + message);
		if (t != null)
			android.util.Log.e(TAG, clazz + " 第" + getLineNumber() + "行 : " + t.toString());
	}

	private static int getLineNumber() {
		return Thread.currentThread().getStackTrace()[5].getLineNumber();
	}
}
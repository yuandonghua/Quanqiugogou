package com.shopnum1.distributionportal.util;

import android.graphics.Bitmap;

interface DaoFactory {
	public void imgInsert(String table, String path, Bitmap img);
	public Bitmap imgQuery(String table, String path);
}

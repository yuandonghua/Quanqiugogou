package com.shopnum1.distributionportal.util;

import java.io.ByteArrayOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImplementDao implements DaoFactory {
	
	Context context;
	DbHelper helper;
	SQLiteDatabase db;
	final static byte[] lock = new byte[0];

	public ImplementDao(Context context){
		this.context = context;
		helper = new DbHelper(context);
	}

	@Override
	public void imgInsert(String table, String path, Bitmap img) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		img.compress(Bitmap.CompressFormat.PNG, 100, baos);
		ContentValues values = new ContentValues();
		values.put("imgpath", path);
		values.put("photo", baos.toByteArray());
		values.put("lastdate", System.currentTimeMillis());
		synchronized (lock) {
			db = helper.getWritableDatabase();
			db.insert(table, null, values);
			db.close();
		} 
	}

	@Override
	public Bitmap imgQuery(String table, String path) {
		String sqlQuery = "select * from " + table + " where imgpath='" + path + "'";
		Bitmap bitmap = null;
		synchronized (lock) {
			db = helper.getWritableDatabase();
			Cursor cursor = db.rawQuery(sqlQuery, null);
			if (cursor.moveToFirst()) {
				String sqlUp = "update " + table + " set lastdate=" + System.currentTimeMillis() + " where imgpath='" + path + "'";
				try {
					db.execSQL(sqlUp);
				} catch (Exception e) {
					e.printStackTrace();
				}
				byte[] buffer = cursor.getBlob(cursor.getColumnIndex("photo"));
				bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
			}
			cursor.close();
			db.close();
		}
		return bitmap;
	}
	
}
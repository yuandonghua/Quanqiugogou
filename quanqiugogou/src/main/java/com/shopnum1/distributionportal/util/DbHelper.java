package com.shopnum1.distributionportal.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "imgcache.db";
	
	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table comimage(_id integer primary key autoincrement, lastdate integer not null, imgpath text not null, photo blob)");
		db.execSQL("create table proimage(_id integer primary key autoincrement, lastdate integer not null, imgpath text not null, photo blob)");
		db.execSQL("create table picimage(_id integer primary key autoincrement, lastdate integer not null, imgpath text not null, photo blob)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table comimage if exists");
		db.execSQL("drop table proimage if exists");
		db.execSQL("drop table picimage if exists");
		onCreate(db);
	}
	
}
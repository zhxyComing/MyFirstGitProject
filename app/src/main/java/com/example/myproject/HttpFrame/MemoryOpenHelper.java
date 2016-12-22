/*******************************************************************************
 * Copyright (c) 2015 by ehoo Corporation all right reserved.
 * 2015-9-9 
 * 
 *******************************************************************************/
package com.example.myproject.HttpFrame;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MemoryOpenHelper extends SQLiteOpenHelper {


	public MemoryOpenHelper(Context context) {
		super(context, HttpFrameParams.getDatabaseName(), null, 1);
	}

	//目前只创建了存数据的   为了保证正常顺序，order采用逆填逆显示的方式 order默认值为10000，且该值得最小值可以获得
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS data (dataid integer primary key autoincrement, url varchar(400), path varchar(200) ,orders integer)");
		db.execSQL("CREATE TABLE IF NOT EXISTS img (dataid integer primary key autoincrement, url varchar(400), path varchar(200))");
//		db.execSQL("alter table data add test34 varchar(20)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}

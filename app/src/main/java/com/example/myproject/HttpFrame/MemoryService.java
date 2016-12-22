
package com.example.myproject.HttpFrame;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class MemoryService {
	private MemoryOpenHelper memoryOpenHelper;
    private volatile static MemoryService memoryService;

	private MemoryService(Context context) {
		memoryOpenHelper = new MemoryOpenHelper(context);
	}
	//由于是单例，所以要传Application
	public static MemoryService getInstance(Application context){
		if (memoryService==null){
			synchronized (MemoryService.class){
				if (memoryService==null){
					memoryService = new MemoryService(context);
				}
			}
		}
		return memoryService;
	}


	//sql多线程操作记得同步
	public synchronized void addData(Data data) {
		SQLiteDatabase db = memoryOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("url", data.getUrl());
		values.put("path", data.getPath());
		//System.currentTimeMillis()已经精确到毫秒级别 所以这里暂定一则新闻-1s  事实上一次更新最多不会超过20则，所以安全  1000==1s?
		values.put("orders", System.currentTimeMillis() - data.getOrder() * 1000);
		long insert = db.insert("data", null, values);
		if (insert > 0) {
			Log.e("testy", "Success");
		}else {
			Log.e("testy", "Fail");
		}
		db.close();
	}

	//sql多线程操作记得同步
	public synchronized void addImg(String url,String path) {
		SQLiteDatabase db = memoryOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("url", url);
		values.put("path", path);
		long insert = db.insert("img", null, values);
		if (insert > 0) {
			Log.e("testy", "Success");
		}else {
			Log.e("testy", "Fail");
		}
		db.close();
	}

	public void deleteData(String url) {
		SQLiteDatabase writableDatabase = memoryOpenHelper
				.getWritableDatabase();
		writableDatabase.execSQL("delete from data where url=?",
				new String[] { url });
		writableDatabase.close();
	}

	public void deleteImg(String url) {
		SQLiteDatabase writableDatabase = memoryOpenHelper
				.getWritableDatabase();
		writableDatabase.execSQL("delete from img where url=?",
				new String[] { url });
		writableDatabase.close();
	}

	public void deleteAll() {
		SQLiteDatabase writableDatabase = memoryOpenHelper
				.getWritableDatabase();
		try {
			writableDatabase.execSQL("delete from data where 1=1");
			writableDatabase.execSQL("delete from img where 1=1");
		}catch (Exception e){
			Log.e("test",e.toString());
		}
		writableDatabase.close();
	}

	public Data query(String url) {
		SQLiteDatabase writableDatabase = memoryOpenHelper
				.getWritableDatabase();
		Cursor cursor = null;
		try {
		     cursor = writableDatabase.rawQuery("select * from data where url=?", new  String[] {url});
			if(cursor.moveToFirst()){
				String path=cursor.getString(cursor.getColumnIndex("path"));
				long order=cursor.getLong(cursor.getColumnIndex("orders"));
				cursor.close();
				writableDatabase.close();
				//这个order是数据库的order，和传进来的order不一样，但是都能表示顺序！
				return new Data(url,path,order);
			}
		}catch (Exception e){
			Log.e("exception",e.toString());
		}
		if (cursor!=null){
			cursor.close();
		}
		writableDatabase.close();
		return null;
	}

	public String queryImg(String url) {
		SQLiteDatabase writableDatabase = memoryOpenHelper
				.getWritableDatabase();
		Cursor cursor = null;
		try {
			cursor = writableDatabase.rawQuery("select * from img where url=?", new  String[] {url});
			if(cursor.moveToFirst()){
				String path=cursor.getString(cursor.getColumnIndex("path"));
				cursor.close();
				writableDatabase.close();
				//这个order是数据库的order，和传进来的order不一样，但是都能表示顺序！
				return path;
			}
		}catch (Exception e){
			Log.e("exception",e.toString());
		}
		if (cursor!=null){
			cursor.close();
		}
		writableDatabase.close();
		return null;
	}

	public ArrayList<Data> queryAll() {
		SQLiteDatabase writableDatabase = memoryOpenHelper
				.getWritableDatabase();
		ArrayList<Data> list=new ArrayList<Data>();
		Cursor cursor = null;
		try {
			cursor = writableDatabase.rawQuery("select * from data order by orders desc", null);
			while(cursor.moveToNext()){
				String url=cursor.getString(cursor.getColumnIndex("url"));
		     	String path=cursor.getString(cursor.getColumnIndex("path"));
				long order =cursor.getLong(cursor.getColumnIndex("orders"));
				list.add(new Data(url,path,order));
			}
		}catch (Exception e){
            Log.e("exception",e.toString());
		}
		if (cursor!=null){
			cursor.close();
		}
		writableDatabase.close();
		return list;
	}

//  以后添加此功能
//	public void addProperty(){
//		添加新字段
//		try {
//			db.execSQL("alter table data add time integer");
//		}catch (Exception e){
//			Log.e("exception",e.toString());
//		}
//		values.put("time", data.getTime());
//	}
}

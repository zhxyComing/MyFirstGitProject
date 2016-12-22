package com.example.myproject.HttpFrame;

import android.os.Environment;

import java.io.File;
import java.util.Random;

/**
 * Created by 徐政 on 2016/11/26.
 */

public class HttpFrameParams {
    //default path
    public static String DataCachePath = getSD()== null ? null: getSD()+ "/myframe/";
    public static String ImgCachePath = getSD()== null ? null: getSD()+ "/myframe/";

    private static File getSD(){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return Environment.getExternalStorageDirectory();//获取跟目录
        }
        return null;
    }

    //path:"/xxx/xxx/"
    public static void setDataCachePath(String path){
        DataCachePath = getSD()==null ? null : getSD() + path;
    }

    public static void setImgCachePath(String path){
        ImgCachePath = getSD()==null ? null : getSD() + path;
    }

    public static final String GET = "GET";
    public static final String POST = "POST";

    private static String databaseName = "";

    public static void setDatabaseName(String databaseName) {
        HttpFrameParams.databaseName = databaseName;
    }

    public static String getDatabaseName() {
        return databaseName;
    }

    public static String getFileName(){
        return String.valueOf(System.currentTimeMillis())+new Random().nextInt(10000)+new Random().nextInt(1000);
    }

    public static MemoryService memoryService;
}

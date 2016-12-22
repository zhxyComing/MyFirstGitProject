package com.example.myproject.HttpFrame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by 徐政 on 2016/11/22.
 */

//文件操作类
public class FileUtils {

    //写Data  img待测
    public static String write(String json ,String fileName){
        FileOutputStream fout = null;//也可以传一个file作为参数
        try {
            if (HttpFrameParams.DataCachePath==null)return null;
            File file = new File(HttpFrameParams.DataCachePath);
            if (!file.exists()){
                file.mkdirs();
            }
            fout = new FileOutputStream(HttpFrameParams.DataCachePath + fileName);
            byte[] bytes = json.getBytes();
            fout.write(bytes);
            fout.close();
            return HttpFrameParams.DataCachePath + fileName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //读Data   img待测
    public static String read(String path){
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(path);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            String res = new String(buffer, "utf-8");
            fin.close();
            return res;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //delete
    public static void delete(File file){
        if (file.isFile()) {
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    //删除Data
    private static void deleteCacheData(){
        if (HttpFrameParams.DataCachePath!=null){
            delete(new File(HttpFrameParams.DataCachePath));
        }
    }

    //删除Img
    private static void deleteCacheImg(){
        if (HttpFrameParams.ImgCachePath!=null){
            delete(new File(HttpFrameParams.ImgCachePath));
        }
    }

    //删除data ，img ，数据库
    public static void deleteCache(DeleteCallBack callBack){
        deleteCacheData();
        deleteCacheImg();
        deleteCacheSQL();
        callBack.deleteComplete();
    }

    public interface DeleteCallBack{
        void deleteComplete();
    }

    //删除数据库
    private static void deleteCacheSQL() {
        HttpFrameParams.memoryService.deleteAll();
    }

    //获取的是保存时就压缩过的图片
    public static Bitmap getDiskBitmap(String path)
    {
        Bitmap bitmap = null;
        try
        {
            File file = new File(path);
            if(file.exists())
            {
                bitmap = BitmapFactory.decodeFile(path);
            }
        } catch (Exception e) {
        }
        return bitmap;
    }

    //? 方法压缩了吗?
    public static String saveBitmap(Bitmap bitmap) {
        if (HttpFrameParams.ImgCachePath==null)return null;
        File file = new File(HttpFrameParams.ImgCachePath);
        if (!file.exists()){
            file.mkdirs();
        }
        File f = new File(HttpFrameParams.ImgCachePath, HttpFrameParams.getFileName());
        if (f.exists()) {
            f.delete();
        }
        try {
            //借助fileoutputstream将bitmap写入到文件
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            return f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

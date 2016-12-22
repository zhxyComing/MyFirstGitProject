package com.example.myproject.HttpFrame;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 徐政 on 2016/11/23.
 */

/* 该网络框架实现了:*/
/* HttpFrame的使用入口，如果需要缓存，使用前务必init */
public class HttpManager {
    private static final int NO_KEY = -20161128;
    public static final int GET_DATA_SUCCESS = 0;
    public static final int GET_DATA_FAIL = 1;

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static ExecutorService imgService = Executors.newFixedThreadPool(3);

    public static String getTextAndCache(Data data, HttpCallBack callBack) throws NoInitException {
        return getTextAndCache(data.getOrder(),data.getUrl(),HttpFrameParams.POST,"utf-8",null,callBack,NO_KEY);
    }

    public static String getTextAndCache(Data data, int key ,HttpCallBack callBack) throws NoInitException {
        return getTextAndCache(data.getOrder(),data.getUrl(),HttpFrameParams.POST,"utf-8",null,callBack,key);
    }

    public static String getTextAndCache(long order , String url , HttpCallBack callBack) throws NoInitException {
        return getTextAndCache(order,url,HttpFrameParams.POST,"utf-8",null,callBack,NO_KEY);
    }

    public static String getTextAndCache(long order , int key ,String url , HttpCallBack callBack) throws NoInitException {
        return getTextAndCache(order,url,HttpFrameParams.POST,"utf-8",null,callBack,key);
    }

    public static String getTextAndCache(long order , String url , Map<String,String> params , HttpCallBack callBack) throws NoInitException {
        return getTextAndCache(order,url,HttpFrameParams.POST,"utf-8",params,callBack,NO_KEY);
    }

    public static String getTextAndCache(long order , int key ,String url , Map<String,String> params , HttpCallBack callBack) throws NoInitException {
        return getTextAndCache(order,url,HttpFrameParams.POST,"utf-8",params,callBack,key);
    }

    public static String getTextAndCache(long order , String url , String method ,Map<String,String> params , HttpCallBack callBack) throws NoInitException {
        return getTextAndCache(order,url,HttpFrameParams.POST,method ,params,callBack,NO_KEY);
    }

    public static String getTextAndCache(long order , int key ,String url , String method ,Map<String,String> params , HttpCallBack callBack) throws NoInitException {
        return getTextAndCache(order,url,HttpFrameParams.POST,method ,params,callBack,key);
    }

    public static String getTextAndCache(final long order, final String url, final String method, final String encoding, final Map<String,String> params, final HttpCallBack callBack , int key) throws NoInitException {
        //文字 不需要软缓存
        Data data;
        //数据库有记录，走path
        if (HttpFrameParams.memoryService==null){
            throw new NoInitException();
        }
        if ((data = HttpFrameParams.memoryService.query(url))!=null){
            String result = FileUtils.read(data.getPath());
            //数据库有记录，但是缓存文件删了:删除数据库记录,走网络程序
            if (result==null){
                HttpFrameParams.memoryService.deleteData(url);
                //走网络，异步才需要key!
                getDataByNet(order,url,method,encoding,params,callBack,key);
            }
            return result;
        //数据库无记录，直接走网络
        }else {
            getDataByNet(order,url,method,encoding,params,callBack,key);
        }
        return null;
    }

    private static void getDataByNet(final long order , final String url, final String method , final String encoding , final Map<String,String> params , final HttpCallBack callBack, final int key){
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final String json = NetUtils.getData(url,method,encoding,params);
                    //说明网络异常，则不往数据库添加！
                    if (json==null || json.equals("null")){
                        handlCallBack(callBack ,GET_DATA_FAIL ,"网络异常" ,url);
                        return;
                    }
                    if (HttpFrameParams.memoryService==null){
                        throw new NoInitException();
                    }
                    //sql不支持多线程，加同步锁也没用，一定要放到同一个线程去操作，这里回到主线程添加！
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            HttpFrameParams.memoryService.addData(new Data(url,FileUtils.write(json ,HttpFrameParams.getFileName()),order));
                        }
                    });
                    handlCallBack(callBack ,GET_DATA_SUCCESS ,json ,url);
                    handlCallBackWithKey(callBack ,GET_DATA_SUCCESS ,json ,url ,key);
                } catch (Exception e) {
                    handlCallBack(callBack ,GET_DATA_FAIL ,e.toString() ,url);
                    handlCallBackWithKey(callBack ,GET_DATA_FAIL ,e.toString() ,url ,key);
                } catch (NoInitException e) {
                    handlCallBack(callBack ,GET_DATA_FAIL ,e.toString() ,url);
                    handlCallBackWithKey(callBack ,GET_DATA_FAIL ,e.toString() ,url ,key);
                }
            }
        });
    }

    private static void handlCallBack(final HttpCallBack callBack , final int code , final String data , final String url){
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack!=null){
                    callBack.complete(code ,data ,url);
                }
            }
        });
    }

    private static void handlCallBackWithKey(final HttpCallBack callBack , final int code , final String data , final String url , final int key){
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack!=null){
                    callBack.completeWithKey(code ,data ,url ,key);
                }
            }
        });
    }

    private static Handler handler = new Handler();

    public interface HttpCallBack{
        void complete(int code, String json, String url);
        void completeWithKey(int code, String json, String url, int key);
    }

    //这个必须设置 context为application
    public static void init(Application application ,String DBName){
        HttpFrameParams.setDatabaseName(DBName);
        HttpFrameParams.memoryService = MemoryService.getInstance(application);
    }

    //path : "/xxx/xxx/"
    public static void init(Application application, String textPath , String imgPath ,String DBName){
        //必须先设置DBName
        HttpFrameParams.setDatabaseName(DBName);
        //设置数据库参数
        HttpFrameParams.memoryService = MemoryService.getInstance(application);
        //设置缓存参数
        HttpFrameParams.setDataCachePath(textPath);
        HttpFrameParams.setImgCachePath(imgPath);
    }

    public static void getText(final String url, final String method, final String encoding, final Map<String,String> params, final HttpCallBack callBack){
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final String data = NetUtils.getData(url, method, encoding, params);
                    if (data==null){
                        handlCallBack(callBack ,GET_DATA_FAIL ,"网络异常" ,url);
                    }else {
                        handlCallBack(callBack ,GET_DATA_SUCCESS ,data ,url);
                    }
                } catch (IOException e) {
                    handlCallBack(callBack ,GET_DATA_FAIL ,e.toString() ,url);
                }
            }
        });
    }

    public static void getText(String url,HttpCallBack callBack){
        getText(url ,HttpFrameParams.POST , "utf-8", null ,callBack);
    }

    public static void getText(String url, Map<String,String> map ,HttpCallBack callBack){
        getText(url ,HttpFrameParams.POST , "utf-8", map ,callBack);
    }

    public static void clearCache(FileUtils.DeleteCallBack callBack) throws NoInitException {
        if (HttpFrameParams.memoryService==null){
            throw new NoInitException();
        }
        FileUtils.deleteCache(callBack);
    }

    //query db all 缓存的新闻包括了内容和图片url
    public static ArrayList<Data> queryAll() throws NoInitException {
        if (HttpFrameParams.memoryService==null){
            throw new NoInitException();
        }
        return HttpFrameParams.memoryService.queryAll();
    }

    //path -> json
    public static String getFromPath(String path){
        return FileUtils.read(path);
    }

    private static Map<String,SoftReference<Bitmap>> map = new HashMap<>();

    public static Bitmap downloadBitmap(String url , OnDownloadListener listener){
        String path;
        if (map.containsKey(url)){
            Bitmap bitmap =  map.get(url).get();
            if (bitmap!=null){
                //如果缓存有，方法结束; 缓存没有，方法继续
                return bitmap;
            }
            //查询db img库是否有对应path
        }
        if ((path = HttpFrameParams.memoryService.queryImg(url))!=null){
            Bitmap bitmap = FileUtils.getDiskBitmap(path);
            //数据库有记录，但是缓存文件删了:删除数据库记录,走网络程序
            if (bitmap==null){
                HttpFrameParams.memoryService.deleteImg(url);
                getImgByNet(url,listener);
            }
            return bitmap;
        }else {
            getImgByNet(url,listener);
        }
        return null;
    }

    public interface OnDownloadListener{
        void complete(String url, Bitmap bitmap);
    }

    private static void getImgByNet(final String url, final OnDownloadListener listener){
        imgService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bitmap = BitmapFactory
                            .decodeStream(new URL(url).openStream());
                    //1软缓存
                    map.put(url, new SoftReference<Bitmap>(bitmap));
                    //2本地
                    final String path;
                    if ((path = FileUtils.saveBitmap(bitmap))!=null){
                        //不为空，说明图片保存成功，才能添加记录到数据库
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                HttpFrameParams.memoryService.addImg(url,path);
                            }
                        });
                    }
                    //3回调
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(listener!=null)
                                listener.complete(url , bitmap);
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //获取数据时 应当在listView外获取到对应新闻的类对象集合，adapter只涉及图片的操作
}

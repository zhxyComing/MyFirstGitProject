package com.example.myproject.HttpFrame;

/**
 * Created by 徐政 on 2016/11/26.
 */

//数据 例如一则新闻（包含新闻及图片）
public class Data {
    private String url;
    private String path;
    private long order;

    //参数order : 数据在数据列的位置
    /* 原先新闻2   db-> time-1    int-> 10
           新闻3        time-2          9
           新闻4        time-3          8
       后来新闻0        time-1          20
           新闻1        time-2          19
           新闻2         time-1         10
           新闻3        time-2          9
           新闻4        time-3          8
     */
    public Data(String url , String path , long order){
        this.url = url;
        this.path = path;
        this.order = order;
    }

    public String getUrl() {
        return url;
    }

    public long getOrder() {
        return order;
    }

    public String getPath() {
        return path;
    }
}

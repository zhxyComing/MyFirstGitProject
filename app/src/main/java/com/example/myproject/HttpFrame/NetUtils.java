package com.example.myproject.HttpFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Created by 徐政 on 2016/11/21.
 */

public class NetUtils {

    //url , POST or GET ，编码方式 ,POST参数
    public static String getData(String url, String method, String encoding, Map<String,String> params) throws IOException {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        conn.setRequestProperty("charset",encoding);

        //拼装参数上传
        StringBuilder builder;
        if (params!=null){
            builder = new StringBuilder();
            Set<Map.Entry<String, String>> entries = params.entrySet();
            for (Map.Entry<String,String> map : entries){
                builder.append(map.getKey()).append("=").append(map.getValue()).append("&");
            }
            builder.deleteCharAt(builder.length()-1);

            OutputStream ops = conn.getOutputStream();//向服务器写出！
            ops.write(builder.toString().getBytes());
        }
        if (conn.getResponseCode()==200){
            return readFromInputStream(conn.getInputStream(),encoding);//1.权限 2.return
        }
        return null;
    }

    private static String readFromInputStream(InputStream is,String encoding) throws IOException {
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int len = 0;

        while((len = is.read(bytes))!= -1){
            bais.write(bytes,0,len);
        }
        bais.flush();//3.flush
        bais.close();
        return new String(bais.toByteArray(),encoding);
    }

}

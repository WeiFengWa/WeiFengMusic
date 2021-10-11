package com.wei.music.utils;

import android.content.Context;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CacheInterceptor implements Interceptor {

    private Context cont;
    private int age;
    
    public CacheInterceptor(Context cont, int age) {
        this.cont = cont;
        this.age = age;
    } 

    @Override 
    public Response intercept(Chain chain) {
        Request request = chain.request();
        Response var10000 = null;
        Response response;
        try {
            if (ToolUtil.haveNetwork(cont)) {
                //如果有网，返回一个30内有效的响应，则30秒内同一请求会直接从缓存中读取
                response = chain.proceed(request);
                //构建maxAge = 30秒的CacheControl
                String cacheControl = new CacheControl.Builder()
                    .maxAge(age, TimeUnit.SECONDS)
                    .build()
                    .toString();
                var10000 = response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    //填入30秒的CacheControl
                    .header("Cache-Control", cacheControl)
                    .build();
            } else {
                //如果没网，用原来的请求重新构建一个强制从缓存中读取的请求
                request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
                var10000 = chain.proceed(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return var10000;
    }
}

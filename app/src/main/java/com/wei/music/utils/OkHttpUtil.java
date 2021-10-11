package com.wei.music.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import okhttp3.CacheControl;
import java.util.concurrent.TimeUnit;
import android.os.Environment;
import java.io.File;
import okhttp3.Cache;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import okhttp3.Interceptor;

public class OkHttpUtil {
    
    private static int cacheSize = 20 * 1024 * 1024;//10MB
    private static File cachePath;
    private static Cache cache;
    public static final int DAY = 60* 60 * 24;
    public static final int MINUTE = 60* 60;
    public static final int SECOND = 60;
     
    public OkHttpUtil() {   
    }

    private static class OkHttpUtilHolder {
        private final static OkHttpUtil OkHttpUtilINSTANCE = new OkHttpUtil();
    }
    
    public static OkHttpUtil getInstance() {
        return OkHttpUtilHolder.OkHttpUtilINSTANCE;
    }
    
    public static void getOkHttp(final Context context, String url, String cookie, Callback callback) {
        Request request = new Request.Builder()
            .url(url)
            .header("Cookie", cookie)
            .get().build();
        Call call = new OkHttpClient().newCall(request);
        call.enqueue(callback);
    }
    
    public static void get(final Context context, final String url, final String cookie, int age, Callback callback) {
        if(cachePath == null || cache == null) {
            cachePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            cache = new Cache(cachePath, cacheSize);
        }
        Request request = new Request.Builder()
            .url(url)
            .header("Cookie", cookie)
            .get().build();
        OkHttpClient client = new OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor((Interceptor)(new CacheInterceptor(context, age)))
            .addNetworkInterceptor((Interceptor)(new CacheInterceptor(context, age)))
            .build();      
        Call call = client.newCall(request);
        call.enqueue(callback);
    }
    
}  

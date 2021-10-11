package com.wei.music.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.graphics.ColorUtils;
import com.tencent.mmkv.MMKV;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.os.Looper;
import android.widget.Toast;

public class ToolUtil {
    
    private static MMKV mMmkv = null;
    private static CharSequence sysTimeStr = null;
     
    public ToolUtil() {
        if(mMmkv == null) {
            mMmkv = MMKV.defaultMMKV();
        }
    }
    
    private static class ToolUtilHolder {
        private final static ToolUtil ToolUtilINSTANCE = new ToolUtil();
    }
    
    public static ToolUtil getInstance() {
        return ToolUtilHolder.ToolUtilINSTANCE;
    }
    
    public static CharSequence getTime(String format, long time) {
        return sysTimeStr = DateFormat.format(format, time);
    }
    
    public static boolean write(String key, String value) {
        return mMmkv.encode(key, value);
    }
    
    public static boolean write(String key, int value) {
        return mMmkv.encode(key, value);
    }
    
    public static boolean write(String key, boolean value) {
        return mMmkv.encode(key, value);
    }
    
    public static boolean write(String key, long value) {
        return mMmkv.encode(key, value);
    }
    
    public static boolean write(String key, float value) {
        return mMmkv.encode(key, value);
    }
    
    public static String readString(String key) {
        return mMmkv.decodeString(key, "");
    }
    
    public static int readInt(String key) {
        return mMmkv.decodeInt(key, -1);
    }
    
    public static boolean readBool(String key) {
        return mMmkv.decodeBool(key, false);
    }
    
    public static long readLong(String key) {
        return mMmkv.decodeLong(key, -1);
    }
    
    public static float readFloat(String key) {
        return mMmkv.decodeFloat(key, 0.0f);
    }
     
    //判断是否有网络
    public static final boolean haveNetwork(Context cont) {
        ConnectivityManager manager = (ConnectivityManager) cont.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
    
    public static void setStatusBarColor(Activity activity, int status, boolean isDrak){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                  |(isDrak ? 
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 
                    View.SYSTEM_UI_FLAG_VISIBLE));
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(status);
        }
    }
    
    public static void setStatusBarColor(Activity activity, int status, int nav, boolean isDrak){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  
            activity.getWindow().clearFlags(
                                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS  
                              | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);  
            setBarText(activity, isDrak);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  
            activity.getWindow().setStatusBarColor(status);  
            activity.getWindow().setNavigationBarColor(nav);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                activity.getWindow().setNavigationBarContrastEnforced(false);
        }
    }
    
    public static void setStatusBarTextColor(Activity activity, int color) {   
        setBarText(activity, (ColorUtils.calculateLuminance(color) >= 0.5));
    }
    
    public static void setStatusBarTextColor(Activity activity, boolean isDrak) {   
        setBarText(activity, isDrak);
    }
    
    private static void setBarText(Activity activity, boolean isDrak) {
        activity.getWindow().getDecorView().setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION 
            | (isDrak ? 
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 
            View.SYSTEM_UI_FLAG_VISIBLE));
    }
    
    /** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  

    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
    
    
}

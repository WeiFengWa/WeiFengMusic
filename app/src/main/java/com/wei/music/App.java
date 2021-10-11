package com.wei.music;

import android.app.Application;
import android.content.Context;
import com.tencent.mmkv.MMKV;
import com.baidu.mobstat.StatService;

public class App extends Application {

    private static App sApp;
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        mContext = getApplicationContext();
        CrashHandler.init(this);
        MMKV.initialize(this);
        StatService.start(this);
    }

    public static App getApp() {
        return sApp;
    }

    public static Context getContext(){
        return mContext;
    }
}


package com.wei.music.activity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.wei.music.R;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import java.util.List;
import java.util.ArrayList;
import android.os.Build;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import com.tencent.mmkv.MMKV;
import android.content.ComponentName;
import android.support.v4.media.session.MediaSessionCompat;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import com.wei.music.utils.ToolUtil;
import android.content.pm.PackageManager;
import com.wei.music.service.MusicService;

public class StartActivity extends AppCompatActivity {

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    private ToolUtil mToolUtil;

    private final static int WHAT_DELAY = 1;
    private final static int DELAY_TIME = 30;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_DELAY:          
                    MMKV mkv = MMKV.defaultMMKV();      
                    if(!mkv.decodeBool("isFirstFun")) {
                        mkv.encode("isFirstFun", true);
                    } else {
                        if (mToolUtil.readBool("SongListId") && mToolUtil.readInt("MusicPosition") != -1) {
                            Bundle bundle = new Bundle();
                            bundle.putString("id", mToolUtil.readString("SongListId"));
                            bundle.putString("cookie", mToolUtil.readString("UserCookie"));
                            mMediaController.getTransportControls().sendCustomAction("Music", bundle);
                        }
                    }
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                    break;
            }      
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mToolUtil = ToolUtil.getInstance();
        mToolUtil.setStatusBarColor(this, Color.TRANSPARENT, Color.TRANSPARENT, true);
        initMediaBrowser();
    }

    private void initMediaBrowser() {
        mMediaBrowser = new MediaBrowserCompat(this,
                                               new ComponentName(this, MusicService.class),
                                               connectionCallback, null);
        mMediaBrowser.connect();
    }

    public void getPermission() {
        if (Build.VERSION.SDK_INT >= 23) {//android 6.0
            initPermission();
        } else {
            handler.sendEmptyMessageDelayed(WHAT_DELAY, DELAY_TIME);
        }
    }

    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
            try {
                mMediaController = new MediaControllerCompat(StartActivity.this, token);
            } catch (RemoteException e) {}
            mMediaController.registerCallback(mMediaCallback);
            getPermission();
        }
    };

    private MediaControllerCompat.Callback mMediaCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
        }
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
        }
        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }
    };


    String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION};
    List<String> mPermissionList = new ArrayList<>();
    private final int mRequestCode = 100;
    private void initPermission() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }
        if (mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }else{
            handler.sendEmptyMessageDelayed(WHAT_DELAY, DELAY_TIME);          
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            if (hasPermissionDismiss) {
                showPermissionDialog();
            }else{
                handler.sendEmptyMessageDelayed(WHAT_DELAY, DELAY_TIME);
            }
        }
    }

    public void showPermissionDialog() {
        startActivity(new Intent(this, PermissionActivity.class));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getPermission();
    }

}


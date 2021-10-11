package com.wei.music.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.wei.music.App;
import com.wei.music.R;
import com.wei.music.bean.UserMusicListBean;
import com.wei.music.utils.CloudMusicApi;
import com.wei.music.utils.OkHttpUtil;
import com.wei.music.utils.ToolUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import com.wei.music.bean.UserLikeListBean;
import androidx.media.MediaBrowserServiceCompat.Result;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.os.Looper;

public class MusicService extends MediaBrowserServiceCompat {
    
    private static final int NoticeId = 2969;
    private static final String NoticeName = "WeiFengMusic";
    
    private static final int SINGLE = 0;//单曲循环
    private static final int RANDOM = 1;//随机播放
    private static final int SEQUENCE = 2;//顺序播放 
    private Random mRandom = new Random();
    
    private int mPlayModel = SEQUENCE;
    private int mPosition = 0;//歌曲位置
    private int mDuration = 0;//歌曲长度
    private int mAudioSessionId = 0;//可视化ID
    private Thread mSeekBarThread;//进度条更新线程
    
    private OkHttpUtil mOkHttpUtil;
    private ToolUtil mToolUtil;
    
    private class SeekBarThread implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
                    mPlaybackState.setState(PlaybackStateCompat.STATE_PLAYING, mMediaPlayer.getCurrentPosition(), 1);
                    mMediaSession.setPlaybackState(mPlaybackState.build());
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        break;
                    }
                }
            }
        }
    }
    private WifiManager.WifiLock mWifiLock;
    private AudioManager mAudioManager;
    private AudioAttributes mAudioAttributes;
    private AudioFocusRequest mAudioFocusRequest;
    
    //媒体会话，受控端
    private static MediaSessionCompat mMediaSession;
    //播放器
    private MediaPlayer mMediaPlayer;
    //播放状态
    private PlaybackStateCompat.Builder mPlaybackState;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotification;
    private MediaMetadataCompat mMediaMetadata;
    
    private PendingIntent playAction;
    private PendingIntent prevAction;
    private PendingIntent nextAction;
    private MediaServlicReceiver mMediaServlicReceiver;
    
    private Gson mGson = new Gson();
    
    private List<MediaSessionCompat.QueueItem> mLastMusicList; 
    private List<MediaSessionCompat.QueueItem> mMusicList = new ArrayList<>();
    private List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
    private List<MediaBrowserCompat.MediaItem> mLikeList = new ArrayList<>();
    
    @Override
    public void onCreate() {
        super.onCreate();
        mToolUtil = ToolUtil.getInstance();
        mOkHttpUtil = OkHttpUtil.getInstance();
        initMediaPlayer();
        initMediaSeesion();      
    }
    
    private void initMediaSeesion() {
        mMediaSession = new MediaSessionCompat(this, NoticeName);     
        mPlaybackState = new PlaybackStateCompat.Builder()
            .setActions(
            PlaybackStateCompat.ACTION_PLAY |
            PlaybackStateCompat.ACTION_PAUSE |
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
            PlaybackStateCompat.ACTION_SEEK_TO
        );
        mMediaSession.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        mMediaSession.setMediaButtonReceiver(null);
        mMediaSession.setPlaybackState(mPlaybackState.build());
        mMediaSession.setCallback(mCallback);
        setSessionToken(mMediaSession.getSessionToken());     
        mMediaSession.setActive(true);
        
        Intent intentNext = new Intent("nextMusic");
        nextAction = PendingIntent.getBroadcast(getApplicationContext(), 3, intentNext, 0);
        
        Intent intentPlay = new Intent("playMusic");
        playAction = PendingIntent.getBroadcast(getApplicationContext(), 2, intentPlay, 0);
        
        Intent intentPrev = new Intent("prevMusic");
        prevAction = PendingIntent.getBroadcast(getApplicationContext(), 1, intentPrev, 0);
        
    }

    private void initMediaPlayer() {
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction("prevMusic");
        intentFilter.addAction("playMusic");
        intentFilter.addAction("nextMusic");
        mMediaServlicReceiver = new MediaServlicReceiver();
        registerReceiver(mMediaServlicReceiver, intentFilter);
        
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(App.getContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mWifiLock = ((WifiManager) getSystemService(App.getContext().WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "musicWifiLock");
        mWifiLock.acquire();
        mAudioManager = (AudioManager) getSystemService(App.getContext().AUDIO_SERVICE);
        mAudioSessionId = mMediaPlayer.getAudioSessionId();
        mToolUtil.write("AudioId", mAudioSessionId);
        mSeekBarThread = new Thread(new SeekBarThread());
        
        mPlayModel = mToolUtil.readInt("PlayModel");
    } 
    
    private boolean requestFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mAudioFocusRequest == null) {
                if (mAudioAttributes == null) {
                    mAudioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
                }
                mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mAudioAttributes)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(mOnAudioFocusChangeListener)
                    .build();
            }
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.requestAudioFocus(mAudioFocusRequest);
        } else {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }
    
    private void setMediaPlayer(final boolean isPlay) {
        try {
            if(mMediaPlayer == null) {
                initMediaPlayer();
            }
            if(mPosition >= mLastMusicList.size())
                mPosition = mLastMusicList.size() - 1;
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(CloudMusicApi.MUSIC_PLAY + mLastMusicList.get(mPosition).getDescription().getMediaId());
            mToolUtil.write("MusicPosition", mPosition);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mDuration = mMediaPlayer.getDuration();
                        if(isPlay) {
                            mMediaSession.getController().getTransportControls().play();
                        } else {
                            if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                                mMediaPlayer.seekTo(mToolUtil.readInt("MusicProgress"), mMediaPlayer.SEEK_CLOSEST);
                            else
                                mMediaPlayer.seekTo((int)mToolUtil.readInt("MusicProgress"));
                        }
                    }
                });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mMediaSession.getController().getTransportControls().skipToNext();
                        updataNotification();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private class MediaServlicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    mMediaSession.getController().getTransportControls().pause();
                    break;
                case "prevMusic":
                    mMediaSession.getController().getTransportControls().skipToPrevious();
                    break;
                case "playMusic":
                    mMediaSession.getController().getTransportControls().play();
                    break;
                case "nextMusic":
                    mMediaSession.getController().getTransportControls().skipToNext();
                    break;
            }  
        }
    }
    
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    mMediaSession.getController().getTransportControls().pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mMediaSession.getController().getTransportControls().pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mMediaSession.getController().getTransportControls().pause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    mMediaSession.getController().getTransportControls().play();
                    break;
            }
        }
    };

    private MediaSessionCompat.Callback mCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
            if (mMediaPlayer != null && mLastMusicList != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mPlaybackState.setState(PlaybackStateCompat.STATE_PAUSED, mMediaPlayer.getCurrentPosition(), 1);
                    mMediaSession.setPlaybackState(mPlaybackState.build());                  
                } else {
                    if (requestFocus()) {
                        mMediaPlayer.start();
                        if (!mSeekBarThread.isAlive()) {
                            mSeekBarThread.start();
                            mPlaybackState.setState(PlaybackStateCompat.STATE_PLAYING, mMediaPlayer.getCurrentPosition(), 1);
                            mMediaSession.setPlaybackState(mPlaybackState.build());
                        }
                    }
                }             
                updataNotification();
            } else {
                Toast.makeText(getApplication(), "没有数据", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            if (mMediaPlayer != null && mLastMusicList != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mPlaybackState.setState(PlaybackStateCompat.STATE_PAUSED, mMediaPlayer.getCurrentPosition(), 1);
                    mMediaSession.setPlaybackState(mPlaybackState.build());                  
                }
                updataNotification();
            }
        }
        
        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                mMediaPlayer.seekTo(pos, mMediaPlayer.SEEK_CLOSEST);
            else
                mMediaPlayer.seekTo((int)pos);
            mPlaybackState.setState(PlaybackStateCompat.STATE_PLAYING, pos, 1);
            mMediaSession.setPlaybackState(mPlaybackState.build());
        }
        
        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            if (mMediaPlayer != null && mLastMusicList.size() > 0) {
                if (mPlayModel == SEQUENCE) {
                    if (mPosition == mLastMusicList.size() - 1) {
                        mPosition = 0;
                    } else {
                        mPosition++;
                    }
                } else if(mPlayModel == RANDOM) {
                    mPosition = mRandom.nextInt(mLastMusicList.size());
                }
                mPlaybackState.setState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, 0, 1);
                mMediaSession.setPlaybackState(mPlaybackState.build());
                setMediaPlayer(true);
            }
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            if (mMediaPlayer != null && mLastMusicList.size() > 0) {
                if (mPlayModel == SEQUENCE) {
                    if (mPosition == 0) {
                        mPosition = mLastMusicList.size() - 1;
                    } else {
                        mPosition--;
                    }
                } else if(mPlayModel == RANDOM) {
                    mPosition = mRandom.nextInt(mLastMusicList.size());
                }
                mPlaybackState.setState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS, 0, 1);
                mMediaSession.setPlaybackState(mPlaybackState.build());
                setMediaPlayer(true);
            }
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
            mPosition = (int)id;
            upMusicList(true);
        }
        
        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
            switch(action) {
                case "LikeMusic":
                    unLikeMusic(extras.getString("id"), extras.getBoolean("is"));
                    break;
                case "Music":
                    mPosition = mToolUtil.readInt("MusicPosition");
                    setMusicList(extras.getString("id"), extras.getString("cookie"), true);
                    getLikeList();
                    break;
                case "MusicList":
                    setMusicList(extras.getString("id"), extras.getString("cookie"), false);
                    break;
                case "PlayModel":
                    mPlayModel = extras.getInt("model");
                    mToolUtil.write("PlayModel", mPlayModel); 
                    Bundle bundle = new Bundle();
                    bundle.putInt("model", mPlayModel);
                    mMediaSession.setExtras(bundle);
                    break;
            }
        }
    }; 
    
    private void upMusicList(boolean isPlay) {
        if (mLastMusicList != null) {
            mLastMusicList.clear();
            mLastMusicList = null;
            mLastMusicList = new ArrayList<>(mMusicList);
        } else {
            mLastMusicList = new ArrayList<>(mMusicList);
        }
        setMediaPlayer(isPlay);
    }
    
    private void unLikeMusic(String id, final boolean is) {
        mOkHttpUtil.get(App.getContext(), (is ? CloudMusicApi.MUSIC_LIKE : CloudMusicApi.MUSIC_UN_LIKE) + id, mToolUtil.readString("UserCookie"), mOkHttpUtil.SECOND / 60, new Callback() {
                @Override
                public void onFailure(Call p1, IOException p2) {
                }
                @Override
                public void onResponse(Call p1, Response response) throws IOException {
                    if(response.body().string().contains("200")) {
                        getLikeList();
                    }
                }
            });
    }
    
    private void getLikeList() {
        mOkHttpUtil.get(App.getContext(), CloudMusicApi.USER_LISK_LIST + mToolUtil.readString("UserId") + "&timestamp=" + System.currentTimeMillis(), mToolUtil.readString("UserCookie"), mOkHttpUtil.SECOND / 60, new Callback() {
                @Override
                public void onFailure(Call p1, IOException p2) {
                }
                @Override
                public void onResponse(Call p1, final Response response) throws IOException {
                    UserLikeListBean bean = new UserLikeListBean();
                    bean = mGson.fromJson(response.body().string(), UserLikeListBean.class);
                    if(mLikeList != null)
                        mLikeList.clear();
                    for(int i = 0; i < bean.ids.size(); i++) {
                        MediaMetadataCompat mMediaMetadata = new MediaMetadataCompat.Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, bean.ids.get(i))
                            .build();
                        mLikeList.add(new MediaBrowserCompat.MediaItem(mMediaMetadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
                    }
                }
            });
    }
    
    private void setMusicList(String id, String cookie, final boolean upMusicList) {
        mOkHttpUtil.get(App.getContext(), CloudMusicApi.SONG_LIST_DATA + id, cookie, mOkHttpUtil.DAY, new Callback() {
                @Override
                public void onFailure(Call p1, IOException p2) {
                }

                @Override
                public void onResponse(Call p1, Response response) throws IOException {
                    UserMusicListBean usermusicbean = new UserMusicListBean();
                    usermusicbean = mGson.fromJson(response.body().string(), UserMusicListBean.class);
                    mMusicList.clear();
                    for (int i = 0; i < usermusicbean.playlist.tracks.size(); i++) {                    
                        mMusicList.add(new MediaSessionCompat.QueueItem(new MediaMetadataCompat.Builder()
                                      .putString(MediaMetadataCompat.METADATA_KEY_TITLE, usermusicbean.playlist.tracks.get(i).name)//歌名
                                      .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, usermusicbean.playlist.tracks.get(i).ar.get(0).name)//歌手
                                      .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, usermusicbean.playlist.tracks.get(i).al.picUrl)//歌曲封面
                                      .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, usermusicbean.playlist.tracks.get(i).id)//歌曲id                                  
                                      .build().getDescription(), i));                                        
                    }
                    mMediaSession.setQueue(mMusicList);
                    if(upMusicList) {
                        upMusicList(false);
                    }
                }
            });
    }
    
    private void setMusicMetadata() {
        String name = mLastMusicList.get(mPosition).getDescription().getTitle().toString();
        String singer = mLastMusicList.get(mPosition).getDescription().getSubtitle().toString();
        String icon = mLastMusicList.get(mPosition).getDescription().getDescription().toString();
        String id = mLastMusicList.get(mPosition).getDescription().getMediaId();
        mToolUtil.write("MusicName", name);
        mToolUtil.write("MusicSinger", singer);
        mToolUtil.write("MusicIcon", icon);
        mToolUtil.write("MusicId", id);
        mToolUtil.write("MusicDuration", mDuration);
        mMediaMetadata = new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, name)//歌名
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, singer)//作者
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, icon)//歌曲封面
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)//歌曲id
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mDuration)//歌曲时长
            .build();
        mMediaSession.setMetadata(mMediaMetadata);
    }
    
    private void updataNotification(){
        setMusicMetadata();
        Glide.with(getApplicationContext()).load(mMediaMetadata.getString(MediaMetadata.METADATA_KEY_ALBUM)).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(NoticeName, NoticeName, NotificationManager.IMPORTANCE_LOW);
                        mNotificationManager.createNotificationChannel(notificationChannel);
                    }               
                    mNotification = MediaStyleHelper.from(App.getContext(), mMediaSession, NoticeName)
                        .setLargeIcon(resource)                   
                        .addAction(R.drawable.ic_previous, "prev", prevAction)
                        .addAction((mMediaPlayer.isPlaying() ? R.drawable.ic_play : R.drawable.ic_pause), "play", playAction)
                        .addAction(R.drawable.ic_next, "next", nextAction);                         
                    mNotificationManager.notify(NoticeId, mNotification.build());
                }
            });
    }
    
    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot(NoticeName, null);
    }
    
    @Override
    public void onLoadChildren(String parentId, MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.detach();
        if (mediaItems != null)
            mediaItems.clear();
        if (parentId.equals("MusicList") && mLastMusicList != null) {
            for (int i = 0; i < mLastMusicList.size(); i++) {
                MediaMetadataCompat mMediaMetadata = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mLastMusicList.get(i).getDescription().getTitle().toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mLastMusicList.get(i).getDescription().getSubtitle().toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mLastMusicList.get(i).getDescription().getDescription().toString())
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mLastMusicList.get(i).getDescription().getMediaId())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 100)
                    .build();
                mediaItems.add(new MediaBrowserCompat.MediaItem(mMediaMetadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
            }
            result.sendResult(mediaItems);
        } else if(parentId.equals("LikeList") && mLikeList != null) {
            result.sendResult(mLikeList);
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mSeekBarThread.interrupt();
            mToolUtil.write("MusicProgress", mMediaPlayer.getCurrentPosition());
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mWifiLock.release();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
            else
                mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
        stopForeground(true);
        if(mNotificationManager != null){
            mNotificationManager.cancel(NoticeId);
        }
        unregisterReceiver(mMediaServlicReceiver);
    }
    
}

package com.wei.music.helper;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import java.io.IOException;

public class MediaHelper {
    
    private static MediaHelper mInstance;
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private OnMediaHelperListener mOnMediaHelperListener;
    private int mResId = 5;
    
    
    public void setOnMediaHelperListener(OnMediaHelperListener mOnMediaHelperListener) {
        this.mOnMediaHelperListener = mOnMediaHelperListener;
    }
    
    public MediaHelper getInstance(Context context) {
        if(mInstance == null) {
            synchronized (MediaHelper.class) {
                if(mInstance == null) {
                    mInstance = new MediaHelper(context);
                }
            }
        }
        return mInstance;
    }
    
    public MediaHelper(Context context) {
        this.mContext = context;
        this.mMediaPlayer = new MediaPlayer();
    }
    
    public void setPath(String path) {
        if(mMediaPlayer.isPlaying()) {
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer.setDataSource(mContext, Uri.parse(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    if(mOnMediaHelperListener != null) {
                        mOnMediaHelperListener.onPrepared(mp);
                    }
                }
            });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(mMediaPlayer != null) {
                        mOnMediaHelperListener.onPauseState();
                    }
                }
            });
    }
    
    public void start() {
        if(!mMediaPlayer.isPlaying()) {
            return;
        }
        mMediaPlayer.start();
        if(mOnMediaHelperListener != null) {
            mOnMediaHelperListener.onPlayingState();
        }
    }
    
    public void pause(){
        if(!mMediaPlayer.isPlaying()){
            return;
        }
        mMediaPlayer.pause();
        if(mOnMediaHelperListener!=null){
            mOnMediaHelperListener.onPauseState();
        }
    }

    public boolean isPlaying(){
        if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
            return true;
        }
        return false;
    }

    public int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration(){
        return mMediaPlayer.getDuration();
    }

    public void seekTo(int progress){
        mMediaPlayer.seekTo(progress);
    }
    
    public interface OnMediaHelperListener {
        //音乐准备好之后调用
        void onPrepared(MediaPlayer mp);
        //音乐暂停状态
        void onPauseState();
        //音乐播放状态
        void onPlayingState();
    }
    
}

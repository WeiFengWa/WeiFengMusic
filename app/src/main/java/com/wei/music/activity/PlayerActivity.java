package com.wei.music.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateFormat;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.wei.music.R;
import com.wei.music.service.MusicService;
import com.wei.music.utils.CloudMusicApi;
import com.wei.music.utils.ColorUtil;
import com.wei.music.utils.GlideLoadUtils;
import com.wei.music.utils.ToolUtil;
import com.wei.music.view.FinishLayout;
import java.util.List;
import android.os.Build;
import androidx.viewpager2.widget.ViewPager2;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import com.wei.music.fragment.PlayerLrcFragment;
import com.wei.music.fragment.PlayerVisualizerFragment;
import com.wei.music.adapter.MainPagerAdapter;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.wei.music.view.MarqueeView;
import com.wei.music.utils.OkHttpUtil;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener, FinishLayout.OnFinishListener {

    private FrameLayout mLrcFrameLayout, mVisualizerFrameLayout;
    private ViewPager2 mViewPager2;
    private Fragment mLrcFragment, mVisualizerFragment;
    private List<Fragment> mPagerFragments = new ArrayList<>();
    private FinishLayout mFinishLayout;
    private GlideLoadUtils mGlideLoadUtils;
    private OkHttpUtil mOkHttpUtil;
    private ToolUtil mToolUtil;
    private MarqueeView mPlayerTitle;
    private TextView mPlayerSinger, mPlayerStartText, mPlayerEndText;
    private ImageView mPlayerLike, mPlayerComment, mPlayerEqualizer, mPlayerMore, mPlayerBack, mPlayerPrevious, mPlayerPlay, mPlayerNext, mPlayerList, mPlayerModel;
    private SeekBar mPlayerSeekBar;
    
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;  
    
    private String mMusicId = "";
    
    private boolean isVertical;
    private boolean isLike = false;
    
    private int mPlayModel;
    
    private OnLrcListener mLrcListener;
    private OnVisualizerListener mVisualizerListener;

    private FragmentManager mFragmentManager;

    private FragmentTransaction mFragmentTransaction;
    
    public interface OnLrcListener {
        void onUpLrc(String url);
        void onUpTime(long time);
        void onUpColor(int[] colors);
    }
    
    public interface OnVisualizerListener {
        void onUpImage(String url);
        void onUpColor(int color);
    }
    
    public void seekTo(long time) {
        mMediaController.getTransportControls().seekTo(time);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if(fragment == mLrcFragment)
            mLrcListener = (OnLrcListener) fragment;
        if(fragment == mVisualizerFragment)
            mVisualizerListener = (OnVisualizerListener) fragment;
        super.onAttachFragment(fragment);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mToolUtil = ToolUtil.getInstance();
        mToolUtil.setStatusBarColor(this, Color.TRANSPARENT, Color.TRANSPARENT, true);
        mGlideLoadUtils = GlideLoadUtils.getInstance();
        mOkHttpUtil = OkHttpUtil.getInstance();
        isVertical = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        initView();
        initMediaBrowser();
        initData();
    } 
    
    private void initMediaBrowser() {
        mMediaBrowser = new MediaBrowserCompat(this,
            new ComponentName(this, MusicService.class),
            connectionCallback, null);
        mMediaBrowser.connect();
    }

    private MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
            try {
                mMediaController = new MediaControllerCompat(PlayerActivity.this, token);
            } catch (RemoteException e) {}
            mMediaController.registerCallback(mMediaCallback);
        }
    };

    private MediaControllerCompat.Callback mMediaCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onExtrasChanged(Bundle extras) {
            super.onExtrasChanged(extras);
            upModelView(extras.getInt("model"));
        }
        
        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            MediaDescriptionCompat description = metadata.getDescription();
            if(!mMusicId.equals(description.getMediaId())) {
                initData();
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            mPlayerSeekBar.setProgress((int)state.getPosition());
            upMusicView(state);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }
    };

    private MediaBrowserCompat.SubscriptionCallback mCallback = new MediaBrowserCompat.SubscriptionCallback() {

        @Override
        public void onChildrenLoaded(String parentId, List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            if(parentId.equals("LikeList")) {
                for(int i = 0; i < children.size(); i++) {
                    if(children.get(i).getDescription().getMediaId().equals(mToolUtil.readString("MusicId"))) {
                        mPlayerLike.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_fill));
                        isLike = true;
                        return;
                    }
                }
            }
        }

        @Override
        public void onError(String parentId) {
            super.onError(parentId);
        }
    };

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.player_like:
                if(isLike) {
                    mPlayerLike.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_outline));
                    isLike = false;
                } else {
                    mPlayerLike.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_fill));
                    isLike = true;
                }
                Bundle likebundle = new Bundle();
                likebundle.putString("id", mToolUtil.readString("MusicId"));
                likebundle.putBoolean("is", isLike);
                mMediaController.getTransportControls().sendCustomAction("LikeMusic", likebundle);
                break;
            case R.id.player_equalizer:
                startActivity(new Intent(this, EqualizerActivity.class));
                break;       
            case R.id.player_previous:
                mMediaController.getTransportControls().skipToPrevious();
                break;
            case R.id.player_play:
                mMediaController.getTransportControls().play();
                break;
            case R.id.player_next:
                mMediaController.getTransportControls().skipToNext();
                break;
            case R.id.player_list:
                startActivity(new Intent(this, MusicListDialog.class));
                break;
            case R.id.player_model:
                if(mPlayModel++ >= 2) {
                    mPlayModel = 0;
                }
                Bundle bundle = new Bundle();
                bundle.putInt("model", mPlayModel);
                mMediaController.getTransportControls().sendCustomAction("PlayModel", bundle);
                break;
        }
    }
    
    private void upModelView(int model) {
        switch(model) {
            case 0:
                mPlayerModel.setImageDrawable(getResources().getDrawable(R.drawable.ic_single));
                break;
            case 1:
                mPlayerModel.setImageDrawable(getResources().getDrawable(R.drawable.ic_random));
                break;
            case 2:
                mPlayerModel.setImageDrawable(getResources().getDrawable(R.drawable.ic_sequence));
                break;
        }
    }
    
    private void upMusicView(PlaybackStateCompat state) {
        mPlayerPlay.setImageDrawable((state.getState() == PlaybackStateCompat.STATE_PLAYING) ? getResources().getDrawable(R.drawable.ic_play) : getResources().getDrawable(R.drawable.ic_pause));
        mPlayerStartText.setText(mToolUtil.getTime("mm:ss", state.getPosition()));
        if(mLrcListener != null) {
            mLrcListener.onUpTime(state.getPosition());
        }
    }
    
    private void initView() {
        mLrcFragment = new PlayerLrcFragment();
        mVisualizerFragment = new PlayerVisualizerFragment();
        if(isVertical) {
            mViewPager2 = (ViewPager2) findViewById(R.id.view_pager_player);
            mPagerFragments.add(mVisualizerFragment);
            mPagerFragments.add(mLrcFragment);
            mViewPager2.setAdapter(new MainPagerAdapter(this, mPagerFragments));
        } else {
            mLrcFrameLayout = (FrameLayout) findViewById(R.id.player_lrc_fragment);
            mVisualizerFrameLayout = (FrameLayout) findViewById(R.id.player_visualizer_fragment);
            mFragmentManager = getSupportFragmentManager();
            mFragmentTransaction = mFragmentManager.beginTransaction();
            mFragmentTransaction.add(R.id.player_lrc_fragment, mLrcFragment);
            mFragmentTransaction.add(R.id.player_visualizer_fragment, mVisualizerFragment);
            mFragmentTransaction.commit();
        }
        mFinishLayout = (FinishLayout) findViewById(R.id.slidingLayout);
        mFinishLayout.setOnFinishListener(this);
        mPlayerTitle = (MarqueeView) findViewById(R.id.player_title);
        mPlayerSinger = (TextView) findViewById(R.id.player_singer);
        mPlayerBack = (ImageView) findViewById(R.id.player_back);
        mPlayerSeekBar = (SeekBar) findViewById(R.id.player_seekbar);
        mPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekbar, int p2, boolean p3) {}
                @Override
                public void onStartTrackingTouch(SeekBar seekbar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekbar) {
                    seekTo(seekbar.getProgress());
                }
            });
        mPlayerStartText = (TextView) findViewById(R.id.player_starttext);
        mPlayerEndText = (TextView) findViewById(R.id.player_endtext);
        mPlayerLike = (ImageView) findViewById(R.id.player_like);
        mPlayerLike.setOnClickListener(this);
        mPlayerComment = (ImageView) findViewById(R.id.player_comment);
        mPlayerEqualizer = (ImageView) findViewById(R.id.player_equalizer);
        mPlayerEqualizer.setOnClickListener(this);
        mPlayerMore = (ImageView) findViewById(R.id.player_more);
        mPlayerPrevious = (ImageView) findViewById(R.id.player_previous);
        mPlayerPrevious.setOnClickListener(this);
        mPlayerPlay = (ImageView) findViewById(R.id.player_play);
        mPlayerPlay.setOnClickListener(this);
        mPlayerNext = (ImageView) findViewById(R.id.player_next);
        mPlayerNext.setOnClickListener(this);
        mPlayerList = (ImageView) findViewById(R.id.player_list);
        mPlayerList.setOnClickListener(this);
        mPlayerModel = (ImageView) findViewById(R.id.player_model);
        mPlayerModel.setOnClickListener(this);
        mPlayModel = mToolUtil.readInt("PlayModel");
    }
    
    private void initData() {
        upModelView(mPlayModel);
        if(mToolUtil.readBool("MusicId")) {
            mPlayerLike.setImageDrawable(getResources().getDrawable(R.drawable.ic_heart_outline));
            mMediaBrowser.unsubscribe("LikeList");
            mMediaBrowser.subscribe("LikeList", mCallback);
            mMusicId = mToolUtil.readString("MusicId");
            long duration = mToolUtil.readLong("MusicDuration");
            mPlayerSeekBar.setMax((int)duration);
            CharSequence sysTimeStr = DateFormat.format("mm:ss", duration);
            mPlayerEndText.setText(sysTimeStr);
            mPlayerTitle.setText(mToolUtil.readString("MusicName"));
            mPlayerSinger.setText(mToolUtil.readString("MusicSinger"));
            mGlideLoadUtils.getBitmap(this, mToolUtil.readString("MusicIcon"), 300, new SimpleTarget<Bitmap>() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        initDataView(bitmap);           
                    }
                });
            if(mVisualizerListener != null)
                mVisualizerListener.onUpImage(mToolUtil.readString("MusicIcon"));
            if(mLrcListener != null)
                mLrcListener.onUpLrc(CloudMusicApi.MUSIC_LRC + mToolUtil.readString("MusicId"));
        }
    }
    
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void initDataView(Bitmap bitmap) {
        int[] colors = ColorUtil.getColor(bitmap);
        mPlayerBack.setImageBitmap(bitmap);
        mToolUtil.setStatusBarTextColor(PlayerActivity.this, colors[0]);
        mPlayerTitle.setTextColor(colors[1]);
        mPlayerSinger.setTextColor(colors[1]);
        mPlayerStartText.setTextColor(colors[1]);
        mPlayerEndText.setTextColor(colors[1]);
        mPlayerLike.setColorFilter(colors[1]);
        mPlayerComment.setColorFilter(colors[1]);
        mPlayerEqualizer.setColorFilter(colors[1]);
        mPlayerMore.setColorFilter(colors[1]);
        mPlayerPrevious.setColorFilter(colors[1]);
        mPlayerPlay.setColorFilter(colors[1]);
        mPlayerNext.setColorFilter(colors[1]);
        mPlayerList.setColorFilter(colors[1]);
        mPlayerModel.setColorFilter(colors[1]);
        if(mLrcListener != null)
            mLrcListener.onUpColor(colors);
        if(mVisualizerListener != null)
            mVisualizerListener.onUpColor(colors[1]);
        LayerDrawable layerDrawable = (LayerDrawable) mPlayerSeekBar.getProgressDrawable();
        Drawable drawable = layerDrawable.getDrawable(2);
        drawable.setColorFilter(colors[1], PorterDuff.Mode.SRC);                       
        StateListDrawable statelist = (StateListDrawable) mPlayerSeekBar.getThumb();
        statelist.getStateDrawable(0).setColorFilter(colors[1], PorterDuff.Mode.SRC);
        statelist.getStateDrawable(1).setColorFilter(colors[1], PorterDuff.Mode.SRC);
        statelist.getStateDrawable(2).setColorFilter(colors[1], PorterDuff.Mode.SRC);                      
        mPlayerSeekBar.setThumb(statelist);
        mPlayerSeekBar.invalidate();        
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        
    }
    
    @Override
    public void onFinish() {  
        finish();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("android:support:fragments", null);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
        if(mMediaController != null) {
            mMediaController.unregisterCallback(mMediaCallback);
            mMediaController = null;        
        }
        if(mMediaBrowser.isConnected()) {
            mMediaBrowser.disconnect();
        }
    }
}

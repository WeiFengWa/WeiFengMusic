package com.wei.music.activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.wei.music.R;
import com.wei.music.adapter.MusicListAdapter;
import com.wei.music.utils.GlideLoadUtils;
import com.wei.music.utils.ToolUtil;
import com.wei.music.utils.OkHttpUtil;
import com.wei.music.view.MarqueeView;
import com.wei.music.service.MusicService;
import com.wei.music.utils.ColorUtil;
import com.wei.music.utils.CloudMusicApi;
import com.wei.music.bean.UserMusicListBean;
import com.wei.music.utils.AppBarStateChangeListener;


public class MusicListActivity extends AppCompatActivity implements View.OnClickListener, MusicListAdapter.OnItemClick {

    private LinearLayout mPlayBarRoot;
    private FrameLayout mPlayBarView;
    private ImageView mMusicListBackground, mMusicListIcon, mPlayBarIcon, mPlayBarPause, mPlayBarList;
    private RecyclerView mRecyclerView;
    private MusicListAdapter mMusicListAdpater;
    private Gson mGson = new Gson();
    private MarqueeView mTitleView;
    private TextView mMusicListName, mMusicListMsg, mPlayBarTitle;
    private AppBarLayout mAppBarLayout;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    
    private ToolUtil mToolUtil;
    private GlideLoadUtils mGlideLoadUtils;
    private OkHttpUtil mOkHttpUtil;
    
    private Bitmap mBackBitmap = null;
    private int[] colors = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musiclist);
        mToolUtil = ToolUtil.getInstance();
        mToolUtil.setStatusBarColor(this, Color.TRANSPARENT, getResources().getColor(R.color.colorPrimary), true);
        mGlideLoadUtils = GlideLoadUtils.getInstance();
        mOkHttpUtil = OkHttpUtil.getInstance();
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
                mMediaController = new MediaControllerCompat(MusicListActivity.this, token);
            } catch (RemoteException e) {}
            mMediaController.registerCallback(mMediaCallback);
            initData(mToolUtil.readString("SongListId"), mToolUtil.readString("UserCookie"));
        }
    };

    private MediaControllerCompat.Callback mMediaCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
        }
        
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            initData();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            mPlayBarPause.setImageDrawable((state.getState() == PlaybackStateCompat.STATE_PLAYING) ? getResources().getDrawable(R.drawable.ic_play) : getResources().getDrawable(R.drawable.ic_pause));
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
            mMusicListAdpater = new MusicListAdapter(MusicListActivity.this, queue);
            mRecyclerView.setAdapter(mMusicListAdpater); 
            mMusicListAdpater.OnClickListener(MusicListActivity.this);
        }
    };
    
    public void getBackBitmap() {
        if(mBackBitmap == null) {
            mGlideLoadUtils.getBitmap(this, mToolUtil.readString("SongListIcon"), 300, new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        mBackBitmap = bitmap;
                        colors = ColorUtil.getColor(mBackBitmap);
                        mMusicListBackground.setImageBitmap(mBackBitmap);
                        mMusicListName.setTextColor(colors[1]);
                        mMusicListMsg.setTextColor(colors[1]);
                        mToolUtil.setStatusBarTextColor(MusicListActivity.this, colors[0]);
                    }
                });
                return;
        }
        mToolUtil.setStatusBarTextColor(this, colors[0]);
    }
    
    private void initData() {
        if (mToolUtil.readBool("MusicId")) {
            mGlideLoadUtils.setCircle(this, mToolUtil.readString("MusicIcon"), mPlayBarIcon);
            mPlayBarTitle.setText(mToolUtil.readString("MusicName") + "-" + mToolUtil.readString("MusicSinger"));          
            Glide.with(getApplicationContext()).load(mToolUtil.readString("MusicIcon")).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        int[] colors = ColorUtil.getColor(resource);
                        GradientDrawable mGroupDrawable= (GradientDrawable) mPlayBarRoot.getBackground();
                        mGroupDrawable.setColor(colors[1]);
                        mPlayBarTitle.setTextColor(colors[0]);
                        mPlayBarPause.setColorFilter(colors[0]);
                        mPlayBarList.setColorFilter(colors[0]);
                    }
                });
        }
    }     
    
    public void initData(final String id, final String cookie) { 
        mMusicListName.setText(mToolUtil.readString("SongListName"));
        mGlideLoadUtils.setRound(MusicListActivity.this, mToolUtil.readString("SongListIcon"), 8,  mMusicListIcon);       
        getBackBitmap();
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("cookie", cookie);
        mMediaController.getTransportControls().sendCustomAction("MusicList", bundle); 
        mOkHttpUtil.get(this, CloudMusicApi.SONG_LIST_DATA + id, cookie, mOkHttpUtil.DAY, new Callback() {

                @Override
                public void onFailure(Call p1, IOException p2) {
                }

                @Override
                public void onResponse(Call p1, Response response) throws IOException {
                    UserMusicListBean usermusicbean = new UserMusicListBean();
                    usermusicbean = mGson.fromJson(response.body().string(), UserMusicListBean.class);
                    runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //mMusicListMsg.setText(usermusicbean.playlist.description);    
                            }
                        });
                }
            });
    }

    public void initView() {
        mTitleView = (MarqueeView) findViewById(R.id.toolbar_title);
        mTitleView.setText("歌单");
        mAppBarLayout = (AppBarLayout) findViewById(R.id.musiclist_appbar);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                    switch(state) {
                        case EXPANDED:
                            mTitleView.setText("");
                            getBackBitmap();
                            break;
                        case COLLAPSED:
                            mTitleView.setText(mMusicListName.getText());
                            ToolUtil.setStatusBarTextColor(MusicListActivity.this, getResources().getColor(R.color.colorPrimary));
                            break;
                        case INTERMEDIATE:
                            break;
                    }
                }
            });
        mMusicListName = (TextView) findViewById(R.id.musiclist_name);
        mMusicListMsg = (TextView) findViewById(R.id.musiclist_msg);
        mMusicListBackground = (ImageView) findViewById(R.id.musiclist_back);
        mMusicListIcon = (ImageView) findViewById(R.id.musiclist_icon);
        mRecyclerView = (RecyclerView) findViewById(R.id.music_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mPlayBarIcon = (ImageView) findViewById(R.id.playbar_icon);    
        mPlayBarList = (ImageView) findViewById(R.id.playbar_list);
        mPlayBarList.setOnClickListener(this);
        mPlayBarPause = (ImageView) findViewById(R.id.playbar_pause);
        mPlayBarTitle = (MarqueeView) findViewById(R.id.playbar_title);
        mPlayBarPause.setOnClickListener(this);
        mPlayBarRoot = (LinearLayout) findViewById(R.id.playbar_root);
        mPlayBarView = (FrameLayout) findViewById(R.id.playbar_view);
        mPlayBarView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.playbar_pause:
                mMediaController.getTransportControls().play();
                break;
            case R.id.playbar_view:
                startActivity(new Intent(this, PlayerActivity.class));
                break;
            case R.id.playbar_list:
                startActivity(new Intent(this, MusicListDialog.class));
                break;
        }
    }
    
    @Override
    public void OnClick(MediaSessionCompat.QueueItem data, int position) {
        mMediaController.getTransportControls().skipToQueueItem(position);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMediaBrowser != null) {
            mMediaBrowser.disconnect();
            Glide.get(this).clearMemory();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}


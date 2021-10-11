package com.wei.music.activity;
import android.content.ComponentName;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.wei.music.R;
import com.wei.music.adapter.MusicListDialogAdapter;
import com.wei.music.service.MusicService;
import com.wei.music.utils.ToolUtil;
import java.util.List;


public class MusicListDialog extends AppCompatActivity implements MusicListDialogAdapter.OnItemClick {

    private RecyclerView mRecyclerView;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;  


    private BottomSheetBehavior mBottomSheet;
    private LinearLayout mSheetLinear;

    private ToolUtil mToolUtil;
    private boolean isMove = false;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_activity_musiclist);
        mToolUtil = ToolUtil.getInstance();
        mToolUtil.setStatusBarColor(this, Color.TRANSPARENT, getResources().getColor(R.color.colorPrimary), true);
        initMediaBrowser();
        initView();
    }


    public void initView() {
        mSheetLinear = findViewById(R.id.bottom_sheet);
        mBottomSheet = new BottomSheetBehavior().from(mSheetLinear);
        mBottomSheet.setBottomSheetCallback(mBottomSheetCallback);
        mRecyclerView = (RecyclerView) findViewById(R.id.dialog_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);  
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (mShouldScroll) {
                        mShouldScroll = false;
                        smoothMoveToPosition(mToPosition);
                    }
                }
            });
    }
    
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if(newState == BottomSheetBehavior.STATE_HIDDEN) {
                finish();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            //这里是拖拽中的回调，根据slideOffset可以做一些动画
        }
    };

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
                mMediaController = new MediaControllerCompat(MusicListDialog.this, token);
            } catch (RemoteException e) {}
            mMediaController.registerCallback(mMediaCallback);
            mMediaBrowser.unsubscribe("MusicList");
            mMediaBrowser.subscribe("MusicList", mCallback);
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

    private MediaBrowserCompat.SubscriptionCallback mCallback = new MediaBrowserCompat.SubscriptionCallback() {

        private MusicListDialogAdapter mMusicListAdpater;

        @Override
        public void onChildrenLoaded(String parentId, List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);
            if(children.size() == 0)
                return;
            mMusicListAdpater = new MusicListDialogAdapter(MusicListDialog.this, children);
            mRecyclerView.setAdapter(mMusicListAdpater); 
            mMusicListAdpater.OnClickListener(MusicListDialog.this);
            smoothMoveToPosition(mToolUtil.readInt("MusicPosition"));
        }

        @Override
        public void onError(String parentId) {
            super.onError(parentId);
        }
    };

    @Override
    public void OnClick(MediaBrowserCompat.MediaItem data, int position) {
        mMediaController.getTransportControls().skipToQueueItem(position);
    }
    
    //目标项是否在最后一个可见项之后
    private boolean mShouldScroll;
    //记录目标项位置
    private int mToPosition;
    /**
     * 滑动到指定位置
     */
    private void smoothMoveToPosition(final int position) {
        // 第一个可见位置
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // 最后一个可见位置
        int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
        if (position < firstItem) {
            // 第一种可能:跳转位置在第一个可见位置之前
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // 第二种可能:跳转位置在第一个可见位置之后
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                mRecyclerView.smoothScrollBy(0, top);
            }
        } else {
            // 第三种可能:跳转位置在最后可见项之后
            mRecyclerView.smoothScrollToPosition(position);
            mToPosition = position;
            mShouldScroll = true;
        }
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


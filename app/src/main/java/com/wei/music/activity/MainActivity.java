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
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.wei.music.R;
import com.wei.music.fragment.AboutFragment;
import com.wei.music.fragment.HomeFragment;
import com.wei.music.fragment.MoreFragment;
import com.wei.music.service.MusicService;
import com.wei.music.utils.ColorUtil;
import com.wei.music.utils.GlideLoadUtils;
import com.wei.music.utils.ToolUtil;
import com.wei.music.view.MarqueeView;
import java.util.List;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import com.wei.music.adapter.MainPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.tabs.TabLayout.Tab;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener, DrawerLayout.DrawerListener, NavigationView.OnNavigationItemSelectedListener {

    private ViewPager2 mViewPager2;
    private List<String> mTitles = new ArrayList<>();
    private List<Fragment> mPagerFragments = new ArrayList<>();
    private FrameLayout mPlayBarView; 
    private NavigationView mLeftNav;
    private DrawerLayout mDrawMain;
    private CardView mContCard;
    private ImageView mTitleBut;
    private MarqueeView mTitleView;
    private ImageView mPlayBarIcon, mPlayBarPause, mPlayBarList;
    private MarqueeView mPlayBarTitle;
    private GlideLoadUtils mGlideLoadUtils;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private LinearLayout mPlayBarRoot;
    private ToolUtil mToolUtil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolUtil = ToolUtil.getInstance();
        mToolUtil.setStatusBarColor(this, Color.TRANSPARENT, getResources().getColor(R.color.colorPrimary), true);
        mGlideLoadUtils = GlideLoadUtils.getInstance();
        initMediaBrowser();
        initView();
        InitData();
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
                mMediaController = new MediaControllerCompat(MainActivity.this, token);
            } catch (RemoteException e) {}
            mMediaController.registerCallback(mMediaCallback);
            mMediaBrowser.unsubscribe("cs");
            mMediaBrowser.subscribe("cs", mCallback);
        }
    };

    private MediaControllerCompat.Callback mMediaCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            InitData();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            mPlayBarPause.setImageDrawable((state.getState() == PlaybackStateCompat.STATE_PLAYING) ? getResources().getDrawable(R.drawable.ic_play) : getResources().getDrawable(R.drawable.ic_pause));
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
        }

        @Override
        public void onError(String parentId) {
            super.onError(parentId);
        }
    };

    private void initView() {
        mTitles.add("Home");
        mTitles.add("More");
        mTitles.add("About");
        mViewPager2= (ViewPager2) findViewById(R.id.view_pager_main);
        mPagerFragments.add(new HomeFragment());
        mPagerFragments.add(new MoreFragment());
        mPagerFragments.add(new AboutFragment());
        MainPagerAdapter mPagerAdapter = new MainPagerAdapter(this, mPagerFragments);
        mViewPager2.setAdapter(mPagerAdapter);
        mViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if(positionOffset <= 0.5) {
                    mTitleView.setText(mTitles.get(position));
                    mTitleView.setAlpha(1 - positionOffset * 2);
                } else {
                    if(position < 2) {
                        mTitleView.setText(mTitles.get(position + 1));
                    } else {
                        mTitleView.setText(mTitles.get(position));
                    }
                    mTitleView.setAlpha(positionOffset * 2 - 1);
                }
            }
        });
        mTitleView = (MarqueeView) findViewById(R.id.toolbar_title);
        mLeftNav = (NavigationView) findViewById(R.id.left_nav);
        //left_nav.setItemIconTintList(null);
        mLeftNav.setNavigationItemSelectedListener(this);
        mLeftNav.getMenu().getItem(0).setChecked(true);
        mLeftNav.setBackgroundColor(Color.TRANSPARENT);
        mDrawMain = (DrawerLayout) findViewById(R.id.drawer_main);
        mDrawMain.setDrawerListener(this);
        mDrawMain.setScrimColor(Color.TRANSPARENT);
        mContCard = (CardView) findViewById(R.id.cont_card);
        mTitleBut = (ImageView) findViewById(R.id.toolbar_but);
        mTitleBut.setOnClickListener(this);   
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

    private void InitData() {
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

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.playbar_list:
                startActivity(new Intent(this, MusicListDialog.class));
                break;
            case R.id.playbar_pause:
                mMediaController.getTransportControls().play();
                break;
            case R.id.playbar_view:
                startActivity(new Intent(MainActivity.this, PlayerActivity.class));
                break;
            case R.id.toolbar_but:
                startActivity(new Intent(this, SearchActivity.class));
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()) {
                //left
            case R.id.left_nav_home:
                Toast.makeText(this, "hello word", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    @Override
    public void onDrawerSlide(View view, float slideOffset) {
        float scale = 1-slideOffset;//1~0
        float leftScale = (float) (1-0.3*scale);
        float rightScale = (float) (0.7f+0.3*scale);//0.7~1
        view.setScaleX(leftScale);//1~0.7
        view.setScaleY(leftScale);//1~0.7
        mContCard.setScaleX(rightScale);
        mContCard.setScaleY(rightScale);
        mContCard.setTranslationX(view.getMeasuredWidth()*slideOffset);//0~width
        mContCard.setRadius(ToolUtil.dip2px(this, 18*slideOffset));
        mContCard.setElevation(ToolUtil.dip2px(this, 4*slideOffset));
    }
    @Override
    public void onDrawerOpened(View p1) {}
    @Override
    public void onDrawerClosed(View p1) {}
    @Override
    public void onDrawerStateChanged(int p1) {}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mDrawMain.isDrawerOpen(GravityCompat.START))  {
                mDrawMain.closeDrawers();            
            } else {
                finish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    //在活动销毁时对服务进行解绑和停止线程
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
    }

}


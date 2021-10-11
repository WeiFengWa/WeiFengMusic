package com.wei.music.fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.wei.music.R;
import androidx.fragment.app.Fragment;
import android.content.Context;
import com.wei.music.activity.PlayerActivity;
import com.wei.music.view.LrcView;
import android.widget.Toast;
import com.wei.music.utils.ColorUtil;
import android.app.Activity;
import com.wei.music.utils.CloudMusicApi;
import com.wei.music.utils.ToolUtil;
import com.wei.music.utils.GlideLoadUtils;
import com.bumptech.glide.request.target.SimpleTarget;
import android.graphics.Bitmap;
import com.bumptech.glide.request.animation.GlideAnimation;

public class PlayerLrcFragment extends Fragment implements PlayerActivity.OnLrcListener {
    
    private View mRootView;
    private LrcView mLrcView;
    private PlayerActivity mActivity;
    private ToolUtil mToolUtil;
    private GlideLoadUtils mGlideLoadUtils;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.player_lrc_fragment, null);
        mToolUtil = ToolUtil.getInstance();
        mGlideLoadUtils = GlideLoadUtils.getInstance();
        mActivity = (PlayerActivity)getActivity();
        mLrcView = mRootView.findViewById(R.id.player_lrcview);
        mLrcView.setDraggable(true, new LrcView.OnPlayClickListener() {
                @Override
                public boolean onPlayClick(long time) {
                    mActivity.seekTo(time);
                    return true;
                }
            });
        initData();
        return mRootView;
    }

    private void initData() {
        onUpLrc(CloudMusicApi.MUSIC_LRC + mToolUtil.readString("MusicId"));
        mGlideLoadUtils.getBitmap(getActivity(), mToolUtil.readString("MusicIcon"), 300, new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                    onUpColor(ColorUtil.getColor(bitmap));      
                }
            });
    }
    
    @Override
    public void onUpLrc(String url) {
        mLrcView.loadLrcByUrl(url);
    }

    @Override
    public void onUpTime(long time) {
        mLrcView.updateTime(time);
    }

    @Override
    public void onUpColor(int[] colors) {
        mLrcView.setNormalColor(ColorUtil.getMixedColor(colors[0], colors[1]));
        mLrcView.setCurrentColor(colors[1]);
        mLrcView.setTimelineColor(colors[1]);
        mLrcView.setTimelineTextColor(colors[1]);
        mLrcView.setTimeTextColor(colors[1]);
        mLrcView.setPlayDrawableColor(colors[1]);
    }
    
}

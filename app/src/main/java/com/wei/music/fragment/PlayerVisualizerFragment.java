package com.wei.music.fragment;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.wei.music.R;
import com.wei.music.activity.PlayerActivity;
import com.wei.music.utils.ColorUtil;
import com.wei.music.utils.GlideLoadUtils;
import com.wei.music.utils.ToolUtil;
import com.wei.music.view.VisualizerView;

public class PlayerVisualizerFragment extends Fragment implements PlayerActivity.OnVisualizerListener {
    
    private View mRootView;
    private VisualizerView mVisualizer;
    private ImageView mPlayerImage;
    private GlideLoadUtils mGlideLoadUtils;
    private ToolUtil mToolUtil;
    private boolean isVertical;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.player_visualizer_fragment, null);
        mToolUtil = ToolUtil.getInstance();
        mGlideLoadUtils = GlideLoadUtils.getInstance();
        isVertical = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        initView();
        initData();
        return mRootView;
    }

    private void initView() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int widthPixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        mVisualizer = mRootView.findViewById(R.id.player_visualizer);
        if(mToolUtil.readInt("AudioId") != -1) {
            mVisualizer.setAudioSessionId(mToolUtil.readInt("AudioId"));
        }
        mPlayerImage = mRootView.findViewById(R.id.player_image);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mPlayerImage.getLayoutParams();
        params.width = widthPixels / 2 - ToolUtil.dip2px(getActivity(), 34);
        params.height = widthPixels / 2 - ToolUtil.dip2px(getActivity(), 34);
        if(!isVertical) {
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            mVisualizer.setShowStyle(VisualizerView.ShowStyle.STYLE_LINE_BAR_AND_WAVE);
        }
        mPlayerImage.setLayoutParams(params);
    }
    
    private void initData() {
        onUpImage(mToolUtil.readString("MusicIcon"));
        mGlideLoadUtils.getBitmap(getActivity(), mToolUtil.readString("MusicIcon"), 300, new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                    onUpColor(ColorUtil.getColor(bitmap)[1]);      
                }
            });
    }

    @Override
    public void onUpImage(String url) {
        if(isVertical) {
            mGlideLoadUtils.setCircle(getActivity(), url, mPlayerImage);
        } else {
            mGlideLoadUtils.setRound(getActivity(), url, 8, mPlayerImage);
        }
    }

    @Override
    public void onUpColor(int color) {
        mVisualizer.setLineBarColor(color);
    }

    @Override
    public void onResume() {
        super.onResume();
        mVisualizer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mVisualizer.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVisualizer.release();
    }
    
}

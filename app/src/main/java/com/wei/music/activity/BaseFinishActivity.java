package com.wei.music.activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.wei.music.R;
import android.graphics.Color;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.wei.music.view.FinishLayout;
import com.wei.music.utils.ToolUtil;

public abstract class BaseFinishActivity extends AppCompatActivity implements FinishLayout.OnFinishListener {

    private FinishLayout mFinishLayout;
    private ToolUtil mToolUtil;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_finish_activity);
        mToolUtil = ToolUtil.getInstance();
        mToolUtil.setStatusBarColor(this, Color.TRANSPARENT, getResources().getColor(R.color.colorPrimary), true);
        mFinishLayout = (FinishLayout) findViewById(R.id.base_finish_activity_layout);
        mFinishLayout.setOnFinishListener(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);    
        params.gravity = Gravity.BOTTOM;
        mFinishLayout.addView(View.inflate(this, initLayout(), null), params);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.bottom_in_anim);
        mFinishLayout.getChildAt(0).startAnimation(anim);
        initView();
        initData();
    }

    public abstract int initLayout();

    public abstract void initView();
    
    public abstract void initData();

    @Override
    public void onFinish() {
        finish();
    }
    
}

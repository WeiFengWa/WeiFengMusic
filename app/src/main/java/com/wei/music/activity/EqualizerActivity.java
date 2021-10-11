package com.wei.music.activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.wei.music.R;
import com.wei.music.utils.ToolUtil;
import com.wei.music.view.EqualizerView;
import androidx.viewpager2.widget.ViewPager2;
import java.util.List;
import androidx.fragment.app.Fragment;
import com.wei.music.fragment.MoreFragment;
import com.wei.music.fragment.AboutFragment;
import java.util.ArrayList;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.tabs.TabLayout.Tab;

public class EqualizerActivity extends AppCompatActivity {

    private EqualizerView mEqualizerView;

    private ToolUtil mToolUtil;
    
    private Button but;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);
        mToolUtil = ToolUtil.getInstance();
        //initView();
        but = findViewById(R.id.activityequalizerButton1);
        but.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View p1) {
                    initView();
                }
            });
    }

    private void initView() {
        mEqualizerView = findViewById(R.id.equalizerview);
        mEqualizerView.setAudioSessionId(mToolUtil.readInt("AudioId"));
    }
    
    
    
    
}

package com.wei.music.activity;
import android.os.Bundle;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.wei.music.R;
import com.wei.music.view.FlowLayout;
import com.wei.music.utils.ToolUtil;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.view.View;

public class SearchActivity extends AppCompatActivity {

    private FlowLayout mFlowLayout;
    private ToolUtil mToolUtil;
    private EditText mSearchEdit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mToolUtil = ToolUtil.getInstance();
        initView();
    }

    private void initView() {
        mSearchEdit = (EditText) findViewById(R.id.search_edittext);
        String[] s = new String[]{"盗将行", "赤伶", "吹灭小山河", "等什么君", "安和桥", "所恋皆星河", "辞九门回忆", "杨花落尽子规啼"};
        mFlowLayout = (FlowLayout) findViewById(R.id.search_flowLayout);
        mFlowLayout.setHorizontalSpacing(getResources().getDimension(R.dimen.view_margin_4dp));
        mFlowLayout.setVerticalSpacing(getResources().getDimension(R.dimen.view_margin_4dp));
        for(int i = 0; i < s.length; i ++){  
            final TextView view = new TextView(this);  
            view.setText(s[i]);
            view.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_padding_bg));
            view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View p1) {
                        mSearchEdit.setText(view.getText());
                    }
                });
            mFlowLayout.addView(view);  
        }  
    }
    
    
    
}

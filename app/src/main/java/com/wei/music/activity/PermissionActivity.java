package com.wei.music.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.wei.music.R;


public class PermissionActivity extends BaseFinishActivity implements View.OnClickListener {

    private TextView permissionone, permissiontwo;
    private Button settingbut;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public int initLayout() {
        return R.layout.dialog_activity_permission;
    }

    @Override
    public void initView() {
        permissionone = (TextView) findViewById(R.id.permission_one);
        permissionone.setOnClickListener(this);
        permissiontwo = (TextView) findViewById(R.id.permission_two);
        permissiontwo.setOnClickListener(this);
        settingbut = (Button) findViewById(R.id.permission_but);
        settingbut.setOnClickListener(this);
    }

    @Override
    public void initData() {
        
    }
    
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.permission_but:
                Uri packageURI = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                startActivity(intent);
                finish();
                break;
        }
    }

}

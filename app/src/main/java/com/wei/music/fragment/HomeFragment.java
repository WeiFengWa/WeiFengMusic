package com.wei.music.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.wei.music.R;
import com.wei.music.activity.MusicListActivity;
import com.wei.music.adapter.MyRecycleAdapter;
import com.wei.music.bean.SongListBean;
import com.wei.music.bean.UserLoginBean;
import com.wei.music.bean.UserSongListBean;
import com.wei.music.utils.CloudMusicApi;
import com.wei.music.utils.GlideLoadUtils;
import com.wei.music.utils.OkHttpUtil;
import com.wei.music.utils.ToolUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import android.widget.Toast;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private View mRootview, mDialogView;
    private BottomSheetDialog mLoginDialog;
    private RecyclerView mRecyclerview;
    private ImageView mUserImag, mUserBackImg;
    private TextView mUserName, mUserSignature;
    private MyRecycleAdapter mSonglistadapter;
    private List<SongListBean> mSonglist = new ArrayList<>();
    private Gson mGson = new Gson();   
    private String mUserId;
    private String mUserCookie; 
    private ToolUtil mToolUtil;
    private GlideLoadUtils mGlideLoadUtils;
    private OkHttpUtil mOkHttpUtil;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootview = inflater.inflate(R.layout.home_fragment, null);
        mToolUtil = ToolUtil.getInstance();
        mGlideLoadUtils = GlideLoadUtils.getInstance();
        mOkHttpUtil = OkHttpUtil.getInstance();
        initView(); 
        InitData();
        return mRootview;
    }

    private void initView() {
        mUserImag = (ImageView) mRootview.findViewById(R.id.user_icon);
        mUserImag.setOnClickListener(this);      
        mUserBackImg = (ImageView) mRootview.findViewById(R.id.user_background);
        mUserName = (TextView) mRootview.findViewById(R.id.user_name);
        mUserSignature = (TextView) mRootview.findViewById(R.id.user_signature);
        mRecyclerview = (RecyclerView) mRootview.findViewById(R.id.recycleview_home);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setSmoothScrollbarEnabled(true);
        manager.setAutoMeasureEnabled(true);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerview.setLayoutManager(manager);  
        mRecyclerview.setHasFixedSize(true);
        mRecyclerview.setNestedScrollingEnabled(false);
        mSonglistadapter = new MyRecycleAdapter(getActivity(), mSonglist);
        mRecyclerview.setAdapter(mSonglistadapter); 
        mSonglistadapter.OnClickListener(new MyRecycleAdapter.OnItemClick() {
                @Override
                public void OnClick(SongListBean data, View image, View title, View msg) {
                    mToolUtil.write("SongListId", data.getId());
                    mToolUtil.write("SongListName", data.getTitle());
                    mToolUtil.write("SongListIcon", data.getImage());
                    View playbar = getActivity().findViewById(R.id.playbar_view);
                    Intent intent = new Intent(getActivity(), MusicListActivity.class);
                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                                                                       Pair.create(image, "song_image"),
                                                                                       Pair.create(title, "song_title"),
                                                                                       Pair.create(msg, "song_msg"),
                                                                                       Pair.create(playbar, "song_playbar")).toBundle();
                    startActivity(intent, bundle);
                }
            });
    }

    private void InitData() {
        mUserId = mToolUtil.readString("UserId");
        mUserCookie = mToolUtil.readString("UserCookie");
        if (mToolUtil.readBool("UserId")) {
            getUserData(mUserId, mUserCookie); 
        } else {
            login();
        }
    }     

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_icon:
                login();
                break;
        }
    }

    public void login() {
        mDialogView = getLayoutInflater().inflate(R.layout.layout_login, null);
        mLoginDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogStyle);
        mLoginDialog.setContentView(mDialogView);
        mLoginDialog.getDelegate().findViewById(R.id.design_bottom_sheet)
            .setBackgroundColor(Color.TRANSPARENT);
        mLoginDialog.show();
        final EditText user = (EditText) mDialogView.findViewById(R.id.login_user);
        final EditText password = (EditText) mDialogView.findViewById(R.id.login_password);
        final Button login = (Button) mDialogView.findViewById(R.id.login_go);
        login.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserLogin(user.getText().toString(), password.getText().toString());
                    login.setVisibility(View.INVISIBLE);
                }
            });
    }

    public void UserLogin(String user, String password) {
        mOkHttpUtil.getOkHttp(getActivity(), String.format(CloudMusicApi.LOGIN_PHONE, user, password), "", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String body = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(body.contains("密码错误") || body.contains("{\"code\":400}")) {
                            Toast.makeText(getActivity(), "账号或密码错误", Toast.LENGTH_SHORT).show();
                            mDialogView.findViewById(R.id.login_go).setVisibility(View.VISIBLE);
                            return;
                        }
                        Toast.makeText(getActivity(), "登录成功", Toast.LENGTH_SHORT).show();
                        mLoginDialog.dismiss();
                        UserLoginBean userbean = mGson.fromJson(body, UserLoginBean.class);
                        mUserId = userbean.account.id;
                        mUserCookie = userbean.cookie;
                        mToolUtil.write("UserIcon", userbean.profile.avatarUrl);
                        mToolUtil.write("UserBg", userbean.profile.backgroundUrl);
                        mToolUtil.write("UserName", userbean.profile.nickname);
                        mToolUtil.write("UserSignature", userbean.profile.signature);
                        mToolUtil.write("UserId", mUserId);
                        mToolUtil.write("UserCookie", mUserCookie);
                        getUserData(mUserId, mUserCookie);
                    }
                });
            }
        });
    }

    public void getUserData(String id, String cookie) {
        mGlideLoadUtils.setCircle(getActivity(), mToolUtil.readString("UserIcon"), mUserImag);
        mGlideLoadUtils.setRound(getActivity(), mToolUtil.readString("UserBg"), 8, mUserBackImg);
        mUserName.setText(mToolUtil.readString("UserName"));
        mUserSignature.setText(mToolUtil.readString("UserSignature"));
        mOkHttpUtil.get(getActivity(), CloudMusicApi.USER_SONG_LIST + id, cookie, mOkHttpUtil.DAY, new Callback() {
                @Override
                public void onFailure(Call p1, IOException p2) {
                }
                @Override
                public void onResponse(Call p1, final Response response) throws IOException {
                    UserSongListBean songlistbean = new UserSongListBean();
                    songlistbean = mGson.fromJson(response.body().string(), UserSongListBean.class);
                    mSonglist.clear();
                    for (int i = 0; i < songlistbean.playlist.size(); i++) {
                        mSonglist.add(new SongListBean(
                                          songlistbean.playlist.get(i).name, 
                                          songlistbean.playlist.get(i).trackCount, 
                                          songlistbean.playlist.get(i).coverImgUrl, 
                                          songlistbean.playlist.get(i).id));
                    }
                    getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {                              
                                mSonglistadapter.notifyDataSetChanged();
                            }
                        });
                }
            });
    }



}

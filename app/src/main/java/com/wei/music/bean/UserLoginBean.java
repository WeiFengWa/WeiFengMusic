package com.wei.music.bean;

public class UserLoginBean {
    
    public String token;
    public String cookie;
    
    public String level;
    public String listenSongs;
    
    public account account;
    public profile profile;
    
    public class account {
        public String id;//用户ID
    }
    public class profile {
        public String nickname;//用户名字
        public String avatarUrl;//用户头像
        public String backgroundUrl;//背景墙
        public String signature;//签名
        public String follows;//关注
        public String followeds;//粉丝
    }
    
}

package com.wei.music.bean;
import java.util.ArrayList;


//用户的歌单
public class UserSongListBean {
    
    
    
    public ArrayList<playlist> playlist = new ArrayList();
    
    public class playlist {
        public String coverImgUrl;//歌单封面
        public String name;//歌单名字
        public String id;//歌单ID
        public String trackCount;//歌曲数量
    }
    
    
}

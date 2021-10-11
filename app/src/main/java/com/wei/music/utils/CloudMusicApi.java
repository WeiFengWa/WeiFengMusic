package com.wei.music.utils;

public class CloudMusicApi {
    public static final String CLOUD_MUSIC_API ="https://netease-cloud-music-api-wei.vercel.app/";// "https://autumnfish.cn/";
    public static final String LOGIN_PHONE = CLOUD_MUSIC_API + "login/cellphone?phone=%s&password=%s";
    public static final String USER_DATA = CLOUD_MUSIC_API +  "user/detail?uid=";
    public static final String USER_SONG_LIST = CLOUD_MUSIC_API + "user/playlist?uid=";
    public static final String SONG_LIST_DATA = CLOUD_MUSIC_API + "playlist/detail?id="; 
    public static final String MUSIC_PLAY ="http://music.163.com/song/media/outer/url?id=";
    public static final String MUSIC_LRC = "http://music.163.com/api/song/media?id=";//CLOUD_MUSIC_API + "lyric?id=";
    public static final String MUSIC_LIKE = CLOUD_MUSIC_API + "like?id=";
    public static final String MUSIC_UN_LIKE = CLOUD_MUSIC_API + "like?like=false&id=";
    public static final String USER_LISK_LIST = CLOUD_MUSIC_API + "likelist?uid=";
}

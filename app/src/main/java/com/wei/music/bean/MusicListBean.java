package com.wei.music.bean;

public class MusicListBean {

    public String musicname;//歌曲名
    public String musicid;//歌曲ID
    public String picUrl;//封面
    public String singername;//歌手名
    public String singerid;//歌手ID

    public MusicListBean(String musicname, String musicid, String picUrl, String singername, String singerid) {
        this.musicname = musicname;
        this.musicid = musicid;
        this.picUrl = picUrl;
        this.singername = singername;
        this.singerid = singerid;
    }

    public void setMusicname(String musicname) {
        this.musicname = musicname;
    }

    public String getMusicname() {
        return musicname;
    }

    public void setMusicid(String musicid) {
        this.musicid = musicid;
    }

    public String getMusicid() {
        return musicid;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setSingername(String singername) {
        this.singername = singername;
    }

    public String getSingername() {
        return singername;
    }

    public void setSingerid(String singerid) {
        this.singerid = singerid;
    }

    public String getSingerid() {
        return singerid;
    }

   
}

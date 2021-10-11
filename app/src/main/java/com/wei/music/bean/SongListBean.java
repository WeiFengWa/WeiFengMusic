package com.wei.music.bean;

public class SongListBean {
    private String title, number;
    private String image;
    private String id;

    public SongListBean(String title, String number, String image, String id) {
        this.title = title;
        this.number = number;
        this.image = image;
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}

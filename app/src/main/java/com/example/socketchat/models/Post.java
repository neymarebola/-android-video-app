package com.example.socketchat.models;

import android.net.Uri;
import android.os.Parcelable;

import java.io.Serializable;

public class Post implements Serializable {
    private String pid; // id cua tung bai viet
    private String uid; // xac dinh danh tinh nguoi dang
    private String content; // noi dung bai dang
    private String urlImg; // hinh anh bai dang
    private long postedTime; // thoi gian dang bai
    private Like like;
    private CommentWrapper comment;

    public String getUid() {
        return uid;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Post() {
    }

    public Post(String pid, String uid, String content, String urlImg, long postedTime, Like like, CommentWrapper comment) {
        this.pid = pid;
        this.uid = uid;
        this.content = content;
        this.urlImg = urlImg;
        this.postedTime = postedTime;
        this.like = like;
        this.comment = comment;
    }

    public Like getLike() {
        return like;
    }

    public void setLike(Like like) {
        this.like = like;
    }

    public Post(String uid, String content, String urlImg, long postedTime, Like like, CommentWrapper comment) {
        this.uid = uid;
        this.content = content;
        this.urlImg = urlImg;
        this.postedTime = postedTime;
        this.like = like;
        this.comment = comment;
    }

    public CommentWrapper getComment() {
        return comment;
    }

    public void setComment(CommentWrapper comment) {
        this.comment = comment;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrlImg() {
        return urlImg;
    }

    public void setUrlImg(String urlImg) {
        this.urlImg = urlImg;
    }

    public long getPostedTime() {
        return postedTime;
    }

    public void setPostedTime(long postedTime) {
        this.postedTime = postedTime;
    }

    public long getDate() {
        return postedTime;
    }

    public void setDate(long date) {
        this.postedTime = date;
    }

    public Post(String uid, String content, String urlImg, long date) {
        this.uid = uid;
        this.content = content;
        this.urlImg = urlImg;
        this.postedTime = date;
    }
}

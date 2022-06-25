package com.example.socketchat.models;

public class Comment {
    private String uid;
    private String comment;
    private String pId;
    private long commentedTime;

    public Comment(String uid, String comment, String pId, long commentedTime) {
        this.uid = uid;
        this.comment = comment;
        this.pId = pId;
        this.commentedTime = commentedTime;
    }

    public Comment(String uid, String comment, long commentedTime) {
        this.uid = uid;
        this.comment = comment;
        this.commentedTime = commentedTime;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCommentedTime() {
        return commentedTime;
    }

    public void setCommentedTime(long commentedTime) {
        this.commentedTime = commentedTime;
    }

    public Comment() {
    }
}

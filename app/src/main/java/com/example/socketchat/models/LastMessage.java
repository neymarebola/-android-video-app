package com.example.socketchat.models;

import java.io.Serializable;

public class LastMessage implements Serializable {
    private String senderId;
    private String receiverId;
    private String text;
    private long time;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public LastMessage(String senderId, String receiverId, String text, long time) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.time = time;
    }

    public LastMessage() {
    }
}

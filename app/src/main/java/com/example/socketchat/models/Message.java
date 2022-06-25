package com.example.socketchat.models;

import java.io.Serializable;

public class Message implements Serializable {
    private int mId;
    private String my_id; // id nguoi gui tin nhan
    private String contact_id; // id nguoi nhan tin nhan
    private String msg_text; // noi dung tin nhan
    private int type;
    private int status;
    private long date;


    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Message(String my_id, String contact_id, String msg_text, int type, int status, long date) {
        this.my_id = my_id;
        this.contact_id = contact_id;
        this.msg_text = msg_text;
        this.type = type;
        this.status = status;
        this.date = date;
    }

    public Message() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Message(String my_id, String contact_id, String msg_text, int type) {
        this.my_id = my_id;
        this.contact_id = contact_id;
        this.msg_text = msg_text;
        this.type = type;
    }

    public Message(String my_id, String contact_id, String msg_text) {
        this.my_id = my_id;
        this.contact_id = contact_id;
        this.msg_text = msg_text;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getMy_id() {
        return my_id;
    }

    public void setMy_id(String my_id) {
        this.my_id = my_id;
    }

    public String getContact_id() {
        return contact_id;
    }

    public void setContact_id(String contact_id) {
        this.contact_id = contact_id;
    }

    public String getMsg_text() {
        return msg_text;
    }

    public void setMsg_text(String msg_text) {
        this.msg_text = msg_text;
    }

    public Message(int mId, String my_id, String contact_id, String msg_text) {
        this.mId = mId;
        this.my_id = my_id;
        this.contact_id = contact_id;
        this.msg_text = msg_text;
    }
}

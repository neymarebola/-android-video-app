package com.example.socketchat.models;

import java.io.Serializable;

public class Notification implements Serializable {
    private int type;
    private String sId;
    private String rId;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getsId() {
        return sId;
    }

    public void setsId(String sId) {
        this.sId = sId;
    }

    public String getrId() {
        return rId;
    }

    public Notification() {
    }

    public void setrId(String rId) {
        this.rId = rId;
    }

    public Notification(int type, String sId, String rId) {
        this.type = type;
        this.sId = sId;
        this.rId = rId;
    }
}

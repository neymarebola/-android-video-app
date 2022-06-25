package com.example.socketchat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Like implements Serializable {
    private int count;
    private ArrayList<String> listUid = new ArrayList<>();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<String> getListUid() {
        return listUid;
    }

    public void setListUid(ArrayList<String> listUid) {
        this.listUid = listUid;
    }

    public Like() {
    }

    public Like(int count, ArrayList<String> listUid) {
        this.count = count;
        this.listUid = listUid;
    }
}

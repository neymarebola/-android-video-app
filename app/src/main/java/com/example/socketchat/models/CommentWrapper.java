package com.example.socketchat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommentWrapper implements Serializable {
    private int count;
    private ArrayList<Comment> list = new ArrayList<>();

    public CommentWrapper() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<Comment> getList() {
        return list;
    }

    public void setList(ArrayList<Comment> list) {
        this.list = list;
    }

    public CommentWrapper(int count, ArrayList<Comment> list) {
        this.count = count;
        this.list = list;
    }
}

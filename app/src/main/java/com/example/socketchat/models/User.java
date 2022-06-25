package com.example.socketchat.models;

import java.io.Serializable;

public class User implements Serializable {
    String id;
    String email, username, password, status;
    String profileImg;
    int follow, follower;

    public User(String id, String email, String username, String password, String status, String profileImg, int follow, int follower) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = status;
        this.profileImg = profileImg;
        this.follow = follow;
        this.follower = follower;
    }

    public User(String id, String email, String username, String password, String status, String profileImg) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = status;
        this.profileImg = profileImg;
    }

    public int getFollow() {
        return follow;
    }

    public void setFollow(int follow) {
        this.follow = follow;
    }

    public int getFollower() {
        return follower;
    }

    public void setFollower(int follower) {
        this.follower = follower;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public User(String id, String email, String username, String password, String status) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User(String id, String username, String password, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
    }

    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public User() {
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

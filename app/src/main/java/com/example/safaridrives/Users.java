package com.example.safaridrives;

public class Users {
    private String username;
    private String email;
    private String uid;

    public Users() {

    }

    public Users(String username, String email, String uid) {
        this.username = username;
        this.email = email;
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }
}


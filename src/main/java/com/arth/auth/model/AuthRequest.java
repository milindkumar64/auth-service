package com.arth.auth.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class AuthRequest {
    private String username;
    private String password;
    // getters & setters


    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
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


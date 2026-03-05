package com.arth.auth.dto;

import java.util.List;

public class UserDetail {

    private String username;
    private String email;
    private List<String> roles;

    public UserDetail() {
    }

    public UserDetail(String username, String email, List<String> roles) {
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}

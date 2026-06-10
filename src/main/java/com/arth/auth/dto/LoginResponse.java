package com.arth.auth.dto;

public class LoginResponse {
    String jwt;
    Long userId;

    public LoginResponse() {
    }

    public LoginResponse(String jwt, Long userId) {
        this.jwt = jwt;
        this.userId = userId;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(String username) {
        this.userId = userId;
    }
}
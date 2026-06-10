package com.arth.auth.dto;

public class SignUpRequest {
    Long Id;
    String username;

    public SignUpRequest(Long id, String username) {
        Id = id;
        this.username = username;
    }

    public SignUpRequest() {
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

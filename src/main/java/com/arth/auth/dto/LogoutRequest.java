package com.arth.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {

    @NotBlank
    private String accessToken;

    @NotBlank
    private String refreshToken;

    public LogoutRequest() {
    }

    public LogoutRequest(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

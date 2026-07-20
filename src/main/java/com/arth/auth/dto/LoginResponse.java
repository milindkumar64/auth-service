package com.arth.auth.dto;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private long expiresIn;
    private long refreshExpiresIn;
    private String tokenType;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, Long userId,
                         long expiresIn, long refreshExpiresIn, String tokenType) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        this.tokenType = tokenType;
    }

    public static LoginResponse of(String accessToken, String refreshToken, Long userId,
                                   long accessExpirationMs, long refreshExpirationMs) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                userId,
                accessExpirationMs / 1000,
                refreshExpirationMs / 1000,
                "Bearer");
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(long refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}

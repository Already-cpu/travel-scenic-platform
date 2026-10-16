package com.example.travel.dto;

public class TokenResponse<T> {
    private String token;
    private T user;

    public TokenResponse() {
    }

    public TokenResponse(String token, T user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public T getUser() { return user; }
    public void setUser(T user) { this.user = user; }
}

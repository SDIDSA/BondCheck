package com.sdidsa.bondcheck.models.requests;

public class TokenRequest {
    private String token;

    public TokenRequest(String token) {
        this.token = token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

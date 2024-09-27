package com.sdidsa.bondcheck.models.responses;

/** @noinspection unused*/
public class CheckBondResponse {
    private String user_id;
    private String state;

    public CheckBondResponse(String user_id, String state) {
        this.user_id = user_id;
        this.state = state;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
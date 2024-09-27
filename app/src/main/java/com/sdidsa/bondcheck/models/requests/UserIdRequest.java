package com.sdidsa.bondcheck.models.requests;

import com.sdidsa.bondcheck.abs.utils.Store;

public class UserIdRequest {
    private String user_id;

    public UserIdRequest(String user_id) {
        this.user_id = user_id;
    }

    public UserIdRequest() {
        this(Store.getUserId());
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }
}

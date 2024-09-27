package com.sdidsa.bondcheck.models.requests;

import com.google.gson.annotations.SerializedName;
import com.sdidsa.bondcheck.abs.utils.Store;

/** @noinspection unused, unused , unused , unused , unused */
public class BondObject {

    @SerializedName("user_id_1")
    private String userId1;

    @SerializedName("user_id_2")
    private String userId2;

    public BondObject(String userId2) {
        this.userId1 = Store.getUserId();
        this.userId2 = userId2;
    }

    public BondObject(String userId1, String userId2) {
        this.userId1 = userId1;
        this.userId2 = userId2;
    }

    public String getUserId1() {
        return userId1;
    }

    public void setUserId1(String userId1) {
        this.userId1 = userId1;
    }

    public String getUserId2() {
        return userId2;
    }

    public void setUserId2(String userId2) {
        this.userId2 = userId2;
    }
}
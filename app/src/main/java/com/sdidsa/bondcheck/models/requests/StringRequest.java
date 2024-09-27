package com.sdidsa.bondcheck.models.requests;

public class StringRequest {
    private String value;

    public StringRequest(String value) {
        this.value = value;
    }

    public StringRequest(Object otherUser) {
        this(String.valueOf(otherUser));
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

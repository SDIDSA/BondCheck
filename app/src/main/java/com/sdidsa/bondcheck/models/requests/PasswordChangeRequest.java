package com.sdidsa.bondcheck.models.requests;

/** @noinspection unused*/
public class PasswordChangeRequest {
    private String current_pass, new_pass;

    public PasswordChangeRequest(String current_pass, String new_pass) {
        this.current_pass = current_pass;
        this.new_pass = new_pass;
    }

    public String getCurrent_pass() {
        return current_pass;
    }

    public void setCurrent_pass(String current_pass) {
        this.current_pass = current_pass;
    }

    public String getNew_pass() {
        return new_pass;
    }

    public void setNew_pass(String new_pass) {
        this.new_pass = new_pass;
    }
}

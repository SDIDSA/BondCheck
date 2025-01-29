package com.sdidsa.bondcheck.models.requests;

import androidx.annotation.NonNull;

public record SavePostRequest(String to, String content, String location, String detail,
                              String media) {

    @NonNull
    @Override
    public String toString() {
        return "SavePostRequest{" +
                "\n\tto='" + to + '\'' +
                "\n\tcontent='" + content + '\'' +
                "\n\tlocation='" + location + '\'' +
                "\n\tdetail='" + detail + '\'' +
                "\n\tmedia='" + media + '\'' +
                "\n}";
    }
}

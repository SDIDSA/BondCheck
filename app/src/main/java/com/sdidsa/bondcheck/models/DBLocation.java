package com.sdidsa.bondcheck.models;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;

public record DBLocation(Double latitude, Double longitude) {
    static final DecimalFormat df = new DecimalFormat("#.####");

    public String round(String lang) {
        return (df.format(latitude) + "_" + df.format(longitude)).replace(".", "_")
                +"_" + lang;
    }

    @NonNull
    @Override
    public String toString() {
        return "DBLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}

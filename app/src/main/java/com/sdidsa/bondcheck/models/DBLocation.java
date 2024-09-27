package com.sdidsa.bondcheck.models;

import androidx.annotation.NonNull;

public record DBLocation(Double latitude, Double longitude) {
    @NonNull
    @Override
    public String toString() {
        return "DBLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}

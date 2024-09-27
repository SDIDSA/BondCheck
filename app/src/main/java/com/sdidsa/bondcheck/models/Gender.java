package com.sdidsa.bondcheck.models;

public enum Gender {
    Male("gender_male"),
    Female("gender_female"),
    Unknown("gender_unknown");

    private final String display;
    Gender(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}

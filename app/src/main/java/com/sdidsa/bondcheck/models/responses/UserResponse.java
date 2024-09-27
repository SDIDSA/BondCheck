package com.sdidsa.bondcheck.models.responses;

import androidx.annotation.NonNull;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.models.Gender;

public class UserResponse {
    private String id;
    private final Property<String> username;
    private final Property<String> email;
    private final Property<String> avatar;
    private final Property<String> bio;
    private final Property<String> gender;

    public UserResponse() {
        this.username = new Property<>();
        this.email = new Property<>();
        this.avatar = new Property<>();
        this.bio = new Property<>();
        this.gender = new Property<>();
    }

    public Property<String> avatar() {
        return avatar;
    }

    public Property<String> username() {
        return username;
    }

    public Property<String> email() {
        return email;
    }

    public Property<String> bio() {
        return bio;
    }

    public Property<String> gender() {
        return gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getAvatar() {
        return avatar.get();
    }

    public void setAvatar(String avatar) {
        this.avatar.set(avatar);
    }

    public String getBio() {
        return bio.get();
    }

    public void setBio(String bio) {
        this.bio.set(bio);
    }

    public String getGender() {
        return gender.get();
    }

    public void setGender(String gender) {
        this.gender.set(gender);
    }

    public Gender genderValue() {
        return Gender.valueOf(gender.get());
    }

    @NonNull
    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", username=" + username +
                ", email=" + email +
                ", avatar=" + avatar +
                ", bio=" + bio +
                ", gender=" + gender +
                '}';
    }

    public void copyFrom(UserResponse other) {
        this.id = other.id;
        this.username.set(other.username.get());
        this.email.set(other.email.get());
        this.avatar.set(other.avatar.get());
        this.bio.set(other.bio.get());
        this.gender.set(other.gender.get());
    }

}

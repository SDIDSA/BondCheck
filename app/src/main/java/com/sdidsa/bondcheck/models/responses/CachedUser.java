package com.sdidsa.bondcheck.models.responses;

public class CachedUser {
    private final UserResponse user;
    private long lastUpdate;

    public CachedUser(UserResponse user) {
        this.user = user;
        this.lastUpdate = -1;
    }

    public UserResponse getUser() {
        return user;
    }

    public synchronized void setUser(UserResponse user) {
        this.user.copyFrom(user);
        this.lastUpdate = System.currentTimeMillis();
    }

    public boolean isExpired(long maxAge) {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastUpdate > maxAge * 1000;
    }
}

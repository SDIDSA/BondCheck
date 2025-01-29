package com.sdidsa.bondcheck.models;

import android.content.Context;

import java.util.List;

public class Activity extends PostDetail {
    private static List<PostDetail> cache;

    public static List<PostDetail> listItems(Context context) {
        if (cache == null) {
            cache = listItems(context, Activity.class, "activities");
        }
        return cache;
    }

    public Activity(String description, String emoji) {
        super(description, emoji);
    }
}

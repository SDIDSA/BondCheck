package com.sdidsa.bondcheck.models;

import android.content.Context;

import java.util.List;

public class Feeling extends PostDetail {
    private static List<PostDetail> cache;

    public static List<PostDetail> listItems(Context context) {
        if (cache == null) {
            cache = listItems(context, Feeling.class, "feelings");
        }
        return cache;
    }

    public Feeling(String description, String emoji) {
        super(description, emoji);
    }
}
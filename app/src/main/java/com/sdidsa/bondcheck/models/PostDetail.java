package com.sdidsa.bondcheck.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sdidsa.bondcheck.abs.utils.Assets;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.models.responses.PostResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class PostDetail {

    private final String description;
    private final String emoji;

    public PostDetail(String description, String emoji) {
        this.description = description;
        this.emoji = emoji;
    }

    protected static <T extends PostDetail> List<PostDetail> listItems(Context context, Class<T> type, String fileName) {
        ArrayList<PostDetail> cache = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(
                    Assets.readAsset(context, "post/" + fileName + ".json"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                String description = type.getSimpleName().toLowerCase() + "." +
                        json.getString(type.getSimpleName().toLowerCase());
                String emoji = json.getString("emoji");
                PostDetail detail = type.getConstructor(String.class, String.class)
                        .newInstance(description, emoji);
                cache.add(detail);
            }

        } catch (JSONException | NoSuchMethodException | IllegalAccessException |
                 InstantiationException | InvocationTargetException e) {
            ErrorHandler.handle(e, "read " + fileName + " list from assets");
        }
        cache.sort(Comparator.comparing(PostDetail::description));
        return cache;
    }

    public String description() {
        return description;
    }

    public String emoji() {
        return emoji;
    }

    public boolean match(String toMatch) {
        return description.toLowerCase().contains(toMatch.toLowerCase());
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("type", getClass().getSimpleName().toLowerCase());
            obj.put("description", description);
            obj.put("emoji", emoji);
        } catch (JSONException e) {
            ErrorHandler.handle(e, "serialize location detail");
        }

        return obj.toString();
    }

    private static final HashMap<PostResponse, PostDetail> cache = new HashMap<>();

    public static PostDetail deserialize(PostResponse post) {
        if (cache.containsKey(post)) return cache.get(post);
        String detail = post.detail();
        if (detail == null || detail.equals("null")) {
            cache.put(post, null);
            return null;
        }

        try {
            JSONObject obj = new JSONObject(detail);
            String type = obj.getString("type");
            String description = obj.getString("description");
            String emoji = obj.getString("emoji");

            PostDetail res;
            if (type.equalsIgnoreCase("feeling")) {
                res = new Feeling(description, emoji);
            } else {
                res = new Activity(description, emoji);
            }
            cache.put(post, res);
            return res;
        } catch (JSONException e) {
            ErrorHandler.handle(e, "deserialize post detail");
            return null;
        }
    }
}

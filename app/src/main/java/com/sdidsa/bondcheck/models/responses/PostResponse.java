package com.sdidsa.bondcheck.models.responses;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Item;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.main.create.location.LocationDetail;
import com.sdidsa.bondcheck.models.DBLocation;
import com.sdidsa.bondcheck.models.PostDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public record PostResponse(String id, String by, String to,
                           String content, String location, String detail,
                           String media, Date created_at) implements Item {

    public LocationDetail getLocationDetail() {
        return LocationDetail.deserialize(this);
    }

    public PostDetail getPostDetail() {
        return PostDetail.deserialize(this);
    }

    public String[] getMedia() {
        if(media == null || media.equalsIgnoreCase("null")) return null;
        try {
            JSONArray arr = new JSONArray(media);
            String[] res = new String[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                res[i] = arr.getString(i);
            }
            return res;
        } catch (JSONException e) {
            ErrorHandler.handle(e, "parse post media");
        }
        return null;
    }

    public int indexOf(String url) {
        String[] media = getMedia();
        if(media == null) return -1;
        for(int i = 0; i < media.length; i++) {
            if(media[i].equalsIgnoreCase(url)) return i;
        }
        return -1;
    }

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

    @Override
    public int getTypeIcon() {
        return R.drawable.create;
    }

    @Override
    public String getType() {
        return "post";
    }

    @Override
    public String provider() {
        return by;
    }

    @Override
    public String requester() {
        return to;
    }

    @Override
    public DBLocation getLocation() {
        return getLocationDetail().getLocation();
    }

    @Override
    public boolean hasLocation() {
        return getLocationDetail() != null;
    }

    @Override
    public ItemOverlay getOverlay(Context owner) {
        return null;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("requester", to);
            obj.put("provider", by);
            obj.put("latitude", hasLocation() ? getLocation().latitude() : null);
            obj.put("longitude", hasLocation() ? getLocation().longitude() : null);
            obj.put("created_at", created_at);
        } catch (JSONException e) {
            ErrorHandler.handle(e, "serialize post as item");
        }
        return obj;
    }
}

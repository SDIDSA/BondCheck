package com.sdidsa.bondcheck.app.app_content.session.content.item_display;

import android.content.Context;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.models.DBLocation;
import com.sdidsa.bondcheck.models.responses.PostResponse;

import org.json.JSONObject;

import java.util.Date;

public interface Item {
    @DrawableRes int getTypeIcon();
    String getType();
    String provider();
    String requester();
    DBLocation getLocation();
    boolean hasLocation();
    Date created_at();
    ItemOverlay getOverlay(Context owner);
    JSONObject toJSON();

    default boolean isEditable(String uid) {
        return (this instanceof PostResponse && provider().equals(uid));
    }

    default boolean isDeletable(String uid) {
        return (this instanceof PostResponse && provider().equals(uid)) ||
                (!(this instanceof PostResponse) && requester().equals(uid));
    }

}

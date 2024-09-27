package com.sdidsa.bondcheck.app.app_content.session.content.item_display;

import android.content.Context;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.models.DBLocation;

import org.json.JSONObject;

import java.util.Date;

public interface Item {
    @DrawableRes int getTypeIcon();
    String getType();
    String provider();
    DBLocation getLocation();
    boolean hasLocation();
    Date created_at();
    ItemOverlay getOverlay(Context owner);
    JSONObject toJSON();
}

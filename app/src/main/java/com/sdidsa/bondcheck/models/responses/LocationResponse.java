package com.sdidsa.bondcheck.models.responses;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.text.DateFormat;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.app.app_content.session.content.locations.LocationOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Item;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlay;
import com.sdidsa.bondcheck.models.DBLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public record LocationResponse(String id, String requester, String provider,
                               Double latitude,
                               Double longitude,
                               Date created_at)
        implements Item {
    public LocationResponse(JSONObject obj) throws JSONException {
        this(obj.getString("id"),
                obj.getString("requester"),
                obj.getString("provider"),
                obj.getDouble("latitude"),
                obj.getDouble("longitude"),
                DateFormat.parseDbString(obj.getString("created_at")));
    }

    @Override
    public Double latitude() {
        return latitude == null ? Double.NaN : latitude;
    }

    @Override
    public Double longitude() {
        return longitude == null ? Double.NaN : longitude;
    }

    public String getType() {
        return "location";
    }

    @Override
    public int getTypeIcon() {
        return R.drawable.location_fill;
    }

    @Override
    public DBLocation getLocation() {
        return new DBLocation(latitude(), longitude());
    }

    @Override
    public boolean hasLocation() {
        return !Double.isNaN(latitude());
    }

    @Override
    public ItemOverlay getOverlay(Context owner) {
        return LocationOverlay.getInstance(owner);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("requester", requester);
            obj.put("provider", provider);
            obj.put("latitude", latitude);
            obj.put("longitude", longitude);
            obj.put("created_at", created_at);
        } catch (JSONException e) {
            ErrorHandler.handle(e, "serializing item");
        }
        return obj;
    }
}

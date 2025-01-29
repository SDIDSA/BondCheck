package com.sdidsa.bondcheck.app.app_content.session.content.main.create.location;

import androidx.annotation.NonNull;

import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.models.DBLocation;
import com.sdidsa.bondcheck.models.responses.PostResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public record LocationDetail(LocationType type, String label,
                             String region, String country,
                             double lat, double lon) {
    public LocationDetail(String type, String label,
                          String region, String country,
                          double lat, double lon) {
        this(LocationType.fromString(type), label, region, country, lat, lon);
    }
    public LocationDetail(LocationType type, String label,
                          DBLocation loc) {
        this(type, label, "", "", loc.latitude(), loc.longitude());
    }

    public DBLocation getLocation() {
        return new DBLocation(lat, lon);
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("type", type().name());
            obj.put("label", label);
            obj.put("region", region);
            obj.put("country", country);
            obj.put("lat", lat);
            obj.put("lon", lon);
        } catch (JSONException e) {
            ErrorHandler.handle(e, "serialize location detail");
        }

        return obj.toString();
    }

    private static final HashMap<String, LocationDetail> cache = new HashMap<>();
    public static LocationDetail deserialize(PostResponse post) {
        if(cache.containsKey(post.id())) return cache.get(post.id());
        String location = post.location();
        if(location == null || location.equals("null")) {
            cache.put(post.id(), null);
            return null;
        }

        try {
            JSONObject obj = new JSONObject(location);
            String type = obj.getString("type");
            String label = obj.getString("label");
            String region = obj.getString("region");
            String country = obj.getString("country");
            double lat = obj.getDouble("lat");
            double lon = obj.getDouble("lon");
            LocationDetail detail = new LocationDetail(type, label, region, country, lat, lon);
            cache.put(post.id(), detail);
            return detail;
        } catch (JSONException e) {
            ErrorHandler.handle(e, "deserialize location detail");
            return null;
        }
    }
}

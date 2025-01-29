package com.sdidsa.bondcheck.abs.components.controls.location;

import com.sdidsa.bondcheck.abs.utils.Platform;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GeoCoder {
    private static final String BASE_URL = "https://nominatim.openstreetmap.org/reverse";
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    public static CompletableFuture<String> getAddress(double latitude, double longitude, String lang) {
        return CompletableFuture.supplyAsync(() -> {
            String apiUrl = buildUrl(latitude, longitude, lang);
            try {
                JSONObject address = fetchAddress(apiUrl);
                return formatAddress(address);
            } catch (Exception e) {
                throw new GeocodingException("Failed to fetch address", e);
            }
        }, Platform::runBack);
    }

    private static String buildUrl(double latitude, double longitude, String lang) {
        return String.format(Locale.ROOT,
                "%s?format=json&lat=%f&lon=%f&zoom=18&addressdetails=1&accept-language=%s",
                BASE_URL, latitude, longitude, lang);
    }

    private static JSONObject fetchAddress(String apiUrl) throws IOException, JSONException {
        Request request = new Request.Builder()
                .url(apiUrl)
                .header("User-Agent", "BondCheck/1.0")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new GeocodingException("Failed to fetch address: " + response.code());
            }

            String responseBody = response.body() != null ? response.body().string() : null;
            if (responseBody == null) {
                throw new GeocodingException("Empty response from server");
            }

            JSONObject root = new JSONObject(responseBody);
            return root.getJSONObject("address");
        }
    }

    private static String formatAddress(JSONObject address) {
        String town = address.optString("town", address.optString("city", ""));
        String state = address.optString("state", address.optString("country", ""));

        if (town.isEmpty() || state.isEmpty()) {
            throw new GeocodingException("Invalid address data received");
        }

        return town + ", " + state;
    }

    public static class GeocodingException extends RuntimeException {
        public GeocodingException(String message) {
            super(message);
        }

        public GeocodingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
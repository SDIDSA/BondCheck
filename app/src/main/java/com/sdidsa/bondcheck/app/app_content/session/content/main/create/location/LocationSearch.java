package com.sdidsa.bondcheck.app.app_content.session.content.main.create.location;

import android.os.Build;

import androidx.annotation.NonNull;

import com.sdidsa.bondcheck.BuildConfig;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LocationSearch {
    private static final String BASE_URL = "https://api.stadiamaps.com/geocoding/v1/autocomplete";

    public static void searchLocation(String searchText,
                                      String lang,
                                      Consumer<List<LocationDetail>> onSuccess,
                                      Consumer<String> onError) {
        Platform.runBack(() -> {
            try {
                String encodedText = Build.VERSION.SDK_INT >= 33 ?
                        URLEncoder.encode(searchText, StandardCharsets.UTF_8) :
                        URLEncoder.encode(searchText, StandardCharsets.UTF_8.toString());
                String urlString = String.format("%s?text=%s&layers=venue,locality&lang=%s&api_key=%s",
                        BASE_URL, encodedText, lang, BuildConfig.STADIA_API_KEY);

                JSONObject jsonResponse = fetch(urlString);

                JSONArray arr = jsonResponse.getJSONArray("features");
                ArrayList<LocationDetail> locations = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    JSONObject properties = obj.getJSONObject("properties");
                    String type = properties.getString("layer");
                    String name = properties.getString("name");
                    String region = properties.getString("region");
                    String country = properties.getString("country");
                    JSONObject geometry = obj.getJSONObject("geometry");
                    JSONArray coords = geometry.getJSONArray("coordinates");
                    double lat = coords.getDouble(1);
                    double lon = coords.getDouble(0);
                    locations.add(new LocationDetail(type, name, region, country, lat, lon));
                }

                Platform.runLater(() -> onSuccess.accept(locations));
            } catch (IOException | JSONException e) {
                ErrorHandler.handle(e, "search for location " + searchText);
                onError.accept(e.toString());
            }
        });
    }

    @NonNull
    private static JSONObject fetch(String urlString) throws IOException, JSONException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
        );
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return new JSONObject(response.toString());
    }
}
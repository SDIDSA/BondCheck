package com.sdidsa.bondcheck.abs.components.controls.location;

import androidx.annotation.NonNull;

import com.sdidsa.bondcheck.abs.utils.Platform;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class GeoCoder {

    public static void getAddress(double latitude,
                                             double longitude,
                                             OnGeocodeResultListener listener,
                                             String lang) {
        new ReverseGeocodeTask(latitude, longitude, listener, lang).execute();
    }

    public interface OnGeocodeResultListener {
        void onGeocodeResult(String address);
        void onError(Exception error);
    }

    private record ReverseGeocodeTask(double latitude, double longitude,
                                      OnGeocodeResultListener listener,
                                      String lang) {

        private void execute() {
            Platform.runBack(() -> {
                String apiUrl = String.format(Locale.getDefault(),
                        "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&" +
                                "zoom=18&addressdetails=1&accept-language=%s",
                        latitude, longitude, lang
                );
                try {
                    JSONObject address = getJsonObject(apiUrl);
                    String town = address.isNull("town") ? address.getString("city") : address.getString("town");
                    String state = address.getString("state");
                    String addressString = town + ", " + state;
                    listener.onGeocodeResult(addressString);
                } catch (Exception e) {
                    listener.onError(e);
                }
            });
        }

        private static @NonNull JSONObject getJsonObject(String apiUrl) throws IOException, JSONException {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject root = new JSONObject(response.toString());
            return root.getJSONObject("address");
        }
    }
}
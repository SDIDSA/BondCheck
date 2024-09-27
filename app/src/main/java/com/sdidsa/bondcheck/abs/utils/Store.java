package com.sdidsa.bondcheck.abs.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.display.UiScale;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.privacy.PrivacyGroup;

import org.json.JSONArray;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class Store {
    private static SharedPreferences settingsD;

    //DISPLAY SETTINGS
    private static final String THEME = "theme";
    private static final String ANIMATIONS = "animations";
    private static final String LANGUAGE = "language";
    private static final String SCALE = "scale";

    //PRIVACY SETTINGS
    private static final String SCREEN_CONSENT = "screen_consent";
    private static final String CENSOR_APPS = "censor_apps";

    //NOTIFICATION SETTINGS
    private static final String NOTIFY_ON = "notify_on_";
    private static final String NOTIFY_ON_SCREEN = NOTIFY_ON + "screen";
    private static final String NOTIFY_ON_MIC = NOTIFY_ON + "mic";
    private static final String NOTIFY_ON_LOCATION = NOTIFY_ON + "location";

    //PERMISSION SKIPS
    private static final String SKIP_MICROPHONE_PERMISSION = "skip_microphone_permission";
    private static final String SKIP_LOCATION_PERMISSION = "skip_location_permission";
    private static final String SKIP_USAGE_STATS_PERMISSION = "skip_usage_stats_permission";

    //ENABLE_DISABLE
    private static final String ENABLE_SCREEN = "enable_screen";
    private static final String ENABLE_MIC = "enable_mic";
    private static final String ENABLE_LOCATION = "enable_location";

    //USER DATA
    private static final String JWT_TOKEN = "jwt_token";
    private static final String USER_ID = "user_id";
    private static final String REMEMBER_USERNAME = "rememberUsername";

    public static void init(Context owner) {
        if(settingsD == null)
            settingsD = owner.getSharedPreferences("global" ,Context.MODE_PRIVATE);
    }

    private static final Semaphore mutex = new Semaphore(1);
    private static String getSetting(String key, String def) {
        mutex.acquireUninterruptibly();
        try {
            String val = settingsD.getString(key, def);
            mutex.release();
            return val;
        }catch(Exception x) {
            mutex.release();
            return def;
        }
    }

    private static void setSetting(String key, String value, Consumer<String> onSuccess) {
        Platform.runBack(() -> {
            mutex.acquireUninterruptibly();
            SharedPreferences.Editor editor = settingsD.edit();
            editor.putString(key, value);
            boolean success = editor.commit();
            mutex.release();
            if(success && onSuccess != null)
                Platform.runLater(() -> {
                    try {
                        onSuccess.accept(value);
                    } catch (Exception e) {
                        ErrorHandler.handle(e, "storing data at " + key);
                    }
                });
        });
    }

    private static void setBoolean(String key, boolean value, Consumer<Boolean> onSuccess) {
        setSetting(key, Boolean.toString(value), s -> {
            if(onSuccess != null)
                onSuccess.accept(Boolean.parseBoolean(s));
        });
    }

    public static String getTheme() {
        return getSetting(THEME, Style.THEME_SYSTEM);
    }

    public static void setTheme(String theme, Consumer<String> onSuccess) {
        setSetting(THEME, theme, onSuccess);
    }

    public static String getJwtToken() {
        return getSetting(JWT_TOKEN, "");
    }

    public static void setJwtToken(String token, Consumer<String> onSuccess) {
        setSetting(JWT_TOKEN, token, onSuccess);
    }

    public static String getRememberUsername() {
        return getSetting(REMEMBER_USERNAME, "");
    }

    public static void setRememberUsername(String username, Consumer<String> onSuccess) {
        setSetting(REMEMBER_USERNAME, username, onSuccess);
    }

    public static String getAnimations() {
        return getSetting(ANIMATIONS, Animation.DEFAULT);
    }

    public static void setAnimations(String animations, Consumer<String> onSuccess) {
        setSetting(ANIMATIONS, animations, onSuccess);
    }

    public static String getLanguage() {
        return getSetting(LANGUAGE, "en_US");
    }

    public static void setLanguage(String value, Consumer<String> onSuccess) {
        setSetting(LANGUAGE, value, onSuccess);
    }

    public static UiScale getScale() {
        return UiScale.forText(getSetting(SCALE, UiScale.AUTO.getText()));
    }

    public static void setScale(String value, Consumer<String> onSuccess) {
        setSetting(SCALE, value, onSuccess);
    }

    public static String getCensorApps() {
        return getSetting(CENSOR_APPS, "[]");
    }

    public static void setCensorApps(String value, Consumer<String> onSuccess) {
        setSetting(CENSOR_APPS, value, onSuccess);
    }

    public static HashSet<String> getCensoredApps() {
        HashSet<String> res = new HashSet<>();

        try {
            JSONArray arr = new JSONArray(getCensorApps());
            for(int i = 0; i < arr.length(); i++) {
                res.add(arr.getString(i));
            }
        }catch(Exception e) {
            ErrorHandler.handle(e, "getting censored apps");
        }

        return res;
    }

    public static void setCensoredApps(Set<String> apps, Consumer<String> onSuccess) {
        JSONArray arr = new JSONArray();
        for(String app : apps) {
            arr.put(app);
        }
        setCensorApps(arr.toString(), onSuccess);
    }

    public static String getUserId() {
        return getSetting(USER_ID, "-1");
    }

    public static void setUserId(String value, Consumer<String> onSuccess) {
        setSetting(USER_ID, value, onSuccess);
    }

    public static String getScreenConsent() {
        return getSetting(SCREEN_CONSENT,
                PrivacyGroup.ASK_EVERY_TIME);
    }

    public static void setScreenConsent(String value, Consumer<String> onSuccess) {
        setSetting(SCREEN_CONSENT, value, onSuccess);
    }

    private static boolean isNotifyOn(String key) {
        return Boolean.parseBoolean(getSetting(key, "true"));
    }

    public static boolean isNotifyOnScreen() {
        return isNotifyOn(NOTIFY_ON_SCREEN);
    }

    public static boolean isNotifyOnMic() {
        return isNotifyOn(NOTIFY_ON_MIC);
    }

    public static boolean isNotifyOnLocation() {
        return isNotifyOn(NOTIFY_ON_LOCATION);
    }

    public static void setNotifyOnScreen(boolean value, Consumer<Boolean> onSuccess) {
        setBoolean(NOTIFY_ON_SCREEN, value, onSuccess);
    }

    public static void setNotifyOnMic(boolean value, Consumer<Boolean> onSuccess) {
        setBoolean(NOTIFY_ON_MIC, value, onSuccess);
    }

    public static void setNotifyOnLocation(boolean value, Consumer<Boolean> onSuccess) {
        setBoolean(NOTIFY_ON_LOCATION, value, onSuccess);
    }

    public static void setSkipMicrophonePermission(boolean value, Consumer<Boolean> onSuccess) {
        setBoolean(SKIP_MICROPHONE_PERMISSION, value, onSuccess);
    }

    public static void setSkipLocationPermission(boolean value, Consumer<Boolean> onSuccess) {
        setBoolean(SKIP_LOCATION_PERMISSION, value, onSuccess);
    }

    public static void setSkipUsageStatsPermission(boolean value, Consumer<Boolean> onSuccess) {
        setBoolean(SKIP_USAGE_STATS_PERMISSION, value, onSuccess);
    }

    public static boolean isSkipMicrophonePermission() {
        return Boolean.parseBoolean(getSetting(SKIP_MICROPHONE_PERMISSION, "false"));
    }

    public static boolean isSkipLocationPermission() {
        return Boolean.parseBoolean(getSetting(SKIP_LOCATION_PERMISSION, "false"));
    }

    public static boolean isSkipUsageStatsPermission() {
        return Boolean.parseBoolean(getSetting(SKIP_USAGE_STATS_PERMISSION, "false"));
    }

    public static void setEnableScreen(boolean value, Consumer<Boolean> onSuccess) {
        setBoolean(ENABLE_SCREEN, value, onSuccess);
    }

    public static boolean isEnableScreen() {
        return Boolean.parseBoolean(getSetting(ENABLE_SCREEN, "true"));
    }

    public static void enableMic(boolean value, Consumer<Boolean> onSuccess) {
        setBoolean(ENABLE_MIC, value, onSuccess);
    }

    public static boolean isEnableMic() {
        return Boolean.parseBoolean(getSetting(ENABLE_MIC, "true"));
    }

    public static void enableLocation(boolean value, Consumer<Boolean> onSuccess) {
        setBoolean(ENABLE_LOCATION, value, onSuccess);
    }

    public static boolean isEnableLocation() {
        return Boolean.parseBoolean(getSetting(ENABLE_LOCATION, "true"));
    }
}

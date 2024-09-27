package com.sdidsa.bondcheck.abs.components.controls.location;
import android.util.LruCache;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.models.DBLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class AddressProxy {
    private static final LruCache<String, String> memoryCache =
            new LruCache<>(1000);
    private static DiskAddressCache diskCache;

    public static void init(App owner) {
        diskCache = new DiskAddressCache(owner, "addresses");
    }

    private static String makeKey(DBLocation location, String lang) {
        return (location.latitude() + "_" +
                location.longitude() + "_" +
                lang).replace(".", "_");
    }



    static final HashMap<String, List<Consumer<String>>> waiters = new HashMap<>();
    public synchronized static void getAddress(DBLocation location, String lang, Consumer<String> onResult) {
        String key = makeKey(location, lang);

        if(waiters.containsKey(key)) {
            Objects.requireNonNull(waiters.get(key)).add(onResult);
            return;
        }

        List<Consumer<String>> forThis = new ArrayList<>();
        forThis.add(onResult);
        waiters.put(key, forThis);

        Platform.runBack(() -> {
            String cachedAddress = get(key);
            if (cachedAddress != null) {
                waiters.remove(key);
                Platform.runLater(() -> forThis.forEach(w -> w.accept(cachedAddress)));
                return;
            }

            Platform.runBack(() -> {
                String downloadedAddress = null;
                try {
                    downloadedAddress = fetch(location, lang);
                    if (downloadedAddress != null) {
                        put(key, downloadedAddress);
                    }
                } catch (Exception x) {
                    ErrorHandler.handle(x, "reverse geocoding " + location);
                }

                String finalAddress = downloadedAddress;
                Platform.runLater(() -> {
                    waiters.remove(key);
                    forThis.forEach(w -> w.accept(finalAddress));
                });
            });
        });
    }

    private static String get(String key) {
        String found = memoryCache.get(key);
        if (found == null) {
            found = diskCache.getAddress(key);
            if(found != null) {
                memoryCache.put(key, found);
            }
        }
        return found;
    }

    private static void put(String key, String address) {
        if (memoryCache.get(key) == null) {
            memoryCache.put(key, address);
        }

        if (!diskCache.exists(key)) {
            diskCache.put(key, address);
        }
    }

    private static String fetch(DBLocation location, String lang) {
        return ContextUtils.getDisplayableAddress(location, lang);
    }
}

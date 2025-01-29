package com.sdidsa.bondcheck.abs.utils.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.sdidsa.bondcheck.abs.components.controls.location.GeoCoder;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.models.DBLocation;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class LocationUtils {


    public static String getDisplayableAddress(DBLocation location, String lang) {
        AtomicReference<String> res = new AtomicReference<>();
        Semaphore waiter = new Semaphore(0);
        GeoCoder.getAddress(location.latitude(), location.longitude(), lang).thenAccept(address -> {
            res.set(address);
            waiter.release();
        }).exceptionally(error -> {
            ErrorHandler.handle(error, "reverse geocoding location");
            waiter.release();
            return null;
        });
        waiter.acquireUninterruptibly();
        return res.get();
    }

    @SuppressLint("MissingPermission")
    public static void getLocationAsync(Context owner, Consumer<Location> onLocation) {
        FusedLocationProviderClient client = LocationServices
                .getFusedLocationProviderClient(owner);

        if (!PermissionUtils.isGranted(owner, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            onLocation.accept(null);
            return;
        }
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,
                        null)
                .addOnFailureListener(failure ->
                        ErrorHandler.handle(failure, "requesting location"))
                .addOnSuccessListener(location -> {
                    if(location != null) {
                        onLocation.accept(location);
                    }else {
                        getApproxLocation(owner, onLocation);
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private static void getApproxLocation(Context owner, Consumer<Location> onLocation) {
        FusedLocationProviderClient client = LocationServices
                .getFusedLocationProviderClient(owner);
        client.getCurrentLocation(Priority.PRIORITY_LOW_POWER,
                        null)
                .addOnFailureListener(failure ->
                        ErrorHandler.handle(failure, "requesting location"))
                .addOnSuccessListener(location -> {
                    if(location != null) {
                        onLocation.accept(location);
                    }else {
                        getApproxLocation(owner, onLocation);
                    }
                });
    }

    public static DBLocation getLocation(Context owner) {
        Log.i("context utils", "requesting location...");
        AtomicReference<Location> res = new AtomicReference<>();
        Semaphore waiter = new Semaphore(0);
        Platform.runAfter(waiter::release, 15000);
        getLocationAsync(owner, location -> {
            res.set(location);
            waiter.release();
        });
        waiter.acquireUninterruptibly();

        if(res.get() == null) {
            return null;
        }else {
            return new DBLocation(res.get().getLatitude(), res.get().getLongitude());
        }
    }
}

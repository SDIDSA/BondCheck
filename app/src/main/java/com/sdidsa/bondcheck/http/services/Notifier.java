package com.sdidsa.bondcheck.http.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

public class Notifier {
    private static final String REQUEST_CHANNEL_ID = "request_channel";

    public static void createNotificationChannel(Context context) {
        CharSequence name = "Request Channel";
        String description = "Channel for request notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(REQUEST_CHANNEL_ID,
                name, importance);
        channel.setDescription(description);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.createNotificationChannel(channel);
    }

    @SuppressLint("MissingPermission")
    public static void showNotification(Context context, String requestType, @DrawableRes int icon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, REQUEST_CHANNEL_ID)
                .setSmallIcon(icon)
                .setContentTitle("BondCheck request")
                .setContentText("You just received a " + requestType + " request")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(new Random().nextInt(10000), builder.build());
    }
}

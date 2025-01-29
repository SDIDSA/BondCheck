package com.sdidsa.bondcheck.app.services;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.app.BondCheck;
import com.sdidsa.bondcheck.http.Socket;
import com.sdidsa.bondcheck.http.services.Notifier;

import org.json.JSONException;

public class SocketService extends Service {
    private static final int ID = View.generateViewId();

    private static final @DrawableRes int PAUSED_ICON = R.drawable.heart_broken;
    private static final @DrawableRes int RESUMED_ICON = R.drawable.heart_pulse;

    private static final String RESUME_HEAD = "notif_resume_head";
    private static final String PAUSE_HEAD = "notif_pause_head";
    private static final String RESUME_BODY = "notif_resume_body";
    private static final String PAUSE_BODY = "notif_pause_body";
    private static final String RESUME_SERVICE = "notif_resume_service";
    private static final String PAUSE_SERVICE = "notif_pause_service";

    private static final String ACTION_PAUSE_SERVICE = "PAUSE_SERVICE";
    private static final String ACTION_RESUME_SERVICE = "RESUME_SERVICE";

    private boolean paused = false;

    private Socket socket;
    private static final String CHANNEL_ID = "SocketServiceChannel";

    private NotificationManager manager;

    private BroadcastListener listener;

    @Override
    public void onCreate() {
        super.onCreate();

        manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        Store.init(this);

        paused = Store.isPauseSharing();

        socket = new Socket();
        socket.connect();

        socket.on("screen_request", args -> {
            Log.i("socket", "screen request received");
            String requester;
            try {
                requester = args.getString("requester");
            } catch (JSONException e) {
                ErrorHandler.handle(e, "parsing socket event data");
                return;
            }
            if(!isInUse()) {
                socket.emit("screen_off",
                        "requester", requester);
                return;
            }
            if(paused) {
                socket.emit("service_paused",
                        "state", "paused",
                        "service", "screen",
                        "requester", requester);
            }else {
                if(Store.isNotifyOnScreen()) {
                    Notifier.showNotification(this, "screen",
                            R.drawable.mobile_fill);
                }
                App.requestCapture(this, requester);
            }
        });

        socket.on("mic_request", args -> {
            String requester;
            int duration;
            try {
                requester = args.getString("requester");
                duration = args.getInt("duration");
            } catch (JSONException e) {
                ErrorHandler.handle(e, "parsing socket event data");
                return;
            }
            if(paused) {
                socket.emit("service_paused",
                        "state", "paused",
                        "service", "mic",
                        "requester", requester);
            } else {
                if(App.requestMic(this, requester, duration)) {
                    if(Store.isNotifyOnMic()) {
                        Notifier.showNotification(this, "microphone",
                                R.drawable.mic_fill);
                    }
                } else {
                    socket.emit("service_disabled",
                            "service", "mic",
                            "requester", requester);
                }
            }
        });

        socket.on("location_request", args -> {
            String requester;
            try {
                requester = args.getString("requester");
            } catch (JSONException e) {
                ErrorHandler.handle(e, "parsing socket event data");
                return;
            }
            if(paused) {
                socket.emit("service_paused",
                        "state", "paused",
                        "service", "location",
                        "requester", requester);
            } else {
                if(Store.isNotifyOnLocation()) {
                    Notifier.showNotification(this, "location",
                            R.drawable.location_fill);
                }
                App.requestLocation(this, socket, requester);
            }
        });

        listener = new BroadcastListener(this);

        listener.on(Action.LOCALE_CHANGED, () -> {
            LocaleUtils.localeChanged(this);
            manager.notify(ID, createNotification());
        });

        Notifier.createNotificationChannel(this);

        socket.onAny((event, data) ->
                sendBroadcast(App.broadcastSocketEvent(event, data)));
    }



    public boolean isInUse() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        boolean isScreenOn = powerManager.isInteractive();
        boolean isUnlocked = !keyguardManager.isKeyguardLocked();
        return isScreenOn && isUnlocked;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, startId);
        Platform.runBack(() -> {
            if(intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_PAUSE_SERVICE -> {
                        if(paused) return;
                        paused = true;

                        sendBroadcast(App.broadcast(Action.STOP_SCREEN_SHARING));
                        sendBroadcast(App.broadcast(Action.PAUSE_SHARING));
                        Store.setPauseSharing(true, null);

                        manager.notify(ID, createNotification());
                    }
                    case ACTION_RESUME_SERVICE -> {
                        if(!paused) return;
                        paused = false;

                        sendBroadcast(App.broadcast(Action.RESUME_SHARING));
                        Store.setPauseSharing(false, null);

                        manager.notify(ID, createNotification());
                    }
                    default ->
                            ErrorHandler.handle(new IllegalArgumentException("Unknown action: " +
                                            intent.getAction()),
                                    "handling intent");
                }

                return;
            }

            createNotificationChannel();

            startForeground(ID, createNotification());
        });

        return START_STICKY;
    }

    private @DrawableRes int getIcon() {
        return paused ? PAUSED_ICON : RESUMED_ICON;
    }

    private String getHead() {
        return paused ? RESUME_HEAD : PAUSE_HEAD;
    }

    private String getBody() {
        return paused ? RESUME_BODY : PAUSE_BODY;
    }

    private String getActionText() {
        return paused ? RESUME_SERVICE : PAUSE_SERVICE;
    }

    private String getAction() {
        return paused ? ACTION_RESUME_SERVICE : ACTION_PAUSE_SERVICE;
    }

    public static void setPaused(Context context, boolean paused) {
        String action = paused ? ACTION_RESUME_SERVICE : ACTION_PAUSE_SERVICE;
        try {
            actionIntent(context, action).send();
        } catch (PendingIntent.CanceledException e) {
            ErrorHandler.handle(e, "set pause state to " + paused);
        }
    }

    private Notification createNotification() {
        Locale locale = LocaleUtils.getLocale(this).get();
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                IconCompat.createWithResource(this, R.drawable.empty),
                locale.get(getActionText()),
                actionIntent(getAction()))
                .build();

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(locale.get(getHead()))
                .setContentText(locale.get(getBody()))
                .setSmallIcon(getIcon())
                .setOngoing(true)
                .setContentIntent(activityIntent())
                .addAction(action)
                .build();
    }

    private PendingIntent actionIntent(String action) {
        return actionIntent(this, action);
    }

    private static PendingIntent actionIntent(Context context, String action) {
        Intent intent = new Intent(context, SocketService.class);
        intent.setAction(action);

        return PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private PendingIntent activityIntent() {
        Intent i = new Intent(this, BondCheck.class);
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        return PendingIntent.getActivity(this,
                0, i, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        unregisterReceiver(listener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Service State",
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }
}

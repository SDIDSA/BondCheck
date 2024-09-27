package com.sdidsa.bondcheck.app.services;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;

public enum Action {
    REQUEST_SCREEN(),
    STOP_SCREEN_SHARING(),

    KILL_ACTIVITY(),
    STOP_SERVICE(),
    STYLE_CHANGED(),
    LOCALE_CHANGED(),

    SOCKET_EVENT();

    public static final String INTENT_FILTER = "com.sdidsa.bondcheck.ACTION_REQUEST";

    public static SocketEventListener socketEventReceiver(Context context) {
        SocketEventListener res = new SocketEventListener();
        registerListener(context, res);
        return res;
    }

    public static BroadcastListener broadcastListener(Context context) {
        BroadcastListener res = new BroadcastListener();
        registerListener(context, res);
        return res;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private static void registerListener(Context context, BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter(INTENT_FILTER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        }else {
            context.registerReceiver(receiver, filter);
        }
    }
}

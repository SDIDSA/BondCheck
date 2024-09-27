package com.sdidsa.bondcheck.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sdidsa.bondcheck.abs.utils.ErrorHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SocketEventListener extends BroadcastReceiver {
    private final HashMap<String, ArrayList<Consumer<JSONObject>>> listeners;

    public SocketEventListener() {
        listeners = new HashMap<>();
    }

    public void on(String event, Consumer<JSONObject> listener) {
        ArrayList<Consumer<JSONObject>> handlers =
                listeners.computeIfAbsent(event, k -> new ArrayList<>());
        handlers.add(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("broadcast_action")) {
            Action action = Action.valueOf(
                    Objects.requireNonNull(intent.getExtras())
                            .getString("broadcast_action"));

            if(action == Action.SOCKET_EVENT) {
                String event = intent.getExtras().getString("event");
                List<Consumer<JSONObject>> handlers = listeners.get(event);
                if(handlers != null) {
                    try {
                        JSONObject data = new JSONObject(intent.getExtras()
                                .getString("data", "{}"));
                        handlers.forEach(handler -> handler.accept(data));
                    } catch (JSONException e) {
                        ErrorHandler.handle(e, "parsing socket event data");
                    }
                }
            }
        }
    }

    public void unregister(Context owner) {
        owner.unregisterReceiver(this);
        Log.i("socket", "unregistering");
    }
}

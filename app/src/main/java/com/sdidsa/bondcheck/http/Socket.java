package com.sdidsa.bondcheck.http;

import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.Store;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.socket.client.IO;

public class Socket {
    public static final String REGISTER_SOCKET = "register_socket";

    private io.socket.client.Socket mSocket;
    {
        try {
            mSocket = IO.socket(ApiService.BASE_URL);
        } catch (URISyntaxException e) {
            ErrorHandler.handle(e, "connecting socket");
        }
    }

    public void connect() {
        mSocket.connect();
        mSocket.on(io.socket.client.Socket.EVENT_CONNECT, args ->
                mSocket.emit(REGISTER_SOCKET, Store.getUserId()));
    }

    public void on(String event, Consumer<JSONObject> listener) {
        mSocket.on(event, args -> Platform.runLater(() -> {
            JSONObject obj = new JSONObject();
            if(args.length > 0) {
                if(args[0] instanceof String str) {
                    try {
                        obj = new JSONObject(str);
                    } catch (JSONException e) {
                        ErrorHandler.handle(e, "parsing socket event data");
                    }
                }else if(args[0] instanceof JSONObject object) {
                    obj = object;
                }
            }
            listener.accept(obj);
        }));
    }

    public void onAny(BiConsumer<String, JSONObject> handler) {
        mSocket.onAnyIncoming(args -> {
            String event = args[0].toString();
            JSONObject data = (JSONObject) args[1];
            handler.accept(event, data);
        });
    }

    public void emit(String event, SocketData...data) {
        JSONObject obj = new JSONObject();
        try {
            for(SocketData d : data) {
                obj.put(d.key(), d.value());
            }
        } catch (JSONException e) {
            ErrorHandler.handle(e, "sending socket event");
        }
        mSocket.emit(event, obj);
    }

    public void emit(String event, Object...data) {
        if(data.length % 2 != 0) {
            ErrorHandler.handle(
                    new IllegalArgumentException("Number of arguments can't be odd"),
                    "sending socket event");
        }
        ArrayList<SocketData> socketData = new ArrayList<>();
        for(int i = 0; i < data.length; i+=2) {
            Object key = data[i];
            Object value = data[i + 1];

            if(key instanceof String strKey) {
                socketData.add(new SocketData(strKey, value));
            }else {
                ErrorHandler.handle(
                        new IllegalArgumentException("incorrect argument type"),
                        "sending socket event"
                );
            }
        }
        emit(event, socketData.toArray(new SocketData[0]));
    }

    public void disconnect() {
        mSocket.disconnect();
        mSocket.close();
    }
}

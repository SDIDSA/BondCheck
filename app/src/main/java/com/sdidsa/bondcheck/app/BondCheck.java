package com.sdidsa.bondcheck.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.Loader;
import com.sdidsa.bondcheck.app.app_content.NetErr;
import com.sdidsa.bondcheck.app.app_content.auth.Welcome;
import com.sdidsa.bondcheck.app.app_content.auth.login.Login;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.permission.PermissionCheck;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.display.UiScale;
import com.sdidsa.bondcheck.app.services.Action;
import com.sdidsa.bondcheck.app.services.BroadcastListener;
import com.sdidsa.bondcheck.app.services.FloatingService;
import com.sdidsa.bondcheck.app.services.SocketService;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.requests.TokenRequest;

import retrofit2.Call;

public class BondCheck extends App {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    BroadcastListener receiver;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void postCreate() {
        super.postCreate();

        receiver = new BroadcastListener(this);
        receiver.on(Action.KILL_ACTIVITY, this::finish);

        Animation.applySpeed(Store.getAnimations());

        UiScale uiScale = Store.getScale();
        if(uiScale == UiScale.AUTO)  {
            int sh = getResources().getDisplayMetrics().heightPixels;

            float dp = SizeUtils.pxToDipNoScale(sh, this);
            float scale = 0.0008864f * dp + 0.2304f;
            scale = ((int) (scale * 100)) / 100f;
            SizeUtils.scale = Math.max(
                    Math.min(scale, UiScale.BIGGEST.getScale()),
                    UiScale.SMALLEST.getScale());
        }else {
            SizeUtils.scale = Store.getScale().getScale();
        }

        Platform.runAfter(() -> loadPage(Loader.class), 200);

        Platform.runAfter(() -> {
            String token = Store.getJwtToken();
            if (token != null && !token.isBlank()) {
                tokenExists(token);
            } else {
                loadPage(Welcome.class);
            }
        }, 1000);
    }

    public static void loadSession(Context owner) {
        if (PermissionCheck.shouldShow(owner)) {
            ContextUtils.loadPage(owner, PermissionCheck.class);
        } else {
            Platform.runBack(() -> {
                Intent serviceIntent = new Intent(owner, SocketService.class);
                ContextCompat.startForegroundService(owner, serviceIntent);
            });

            ContextUtils.loadPage(owner, Home.class);
        }
    }

    private void tokenExists(String token) {
        Call<GenericResponse> call = api(this)
                .validateToken(new TokenRequest(token));

        Service.enqueue(call, gr -> {
            if (gr.code() == 200) {
                assert gr.body() != null;
                String newToken = gr.body().getMessage();
                Store.setJwtToken(newToken, s -> {
                    ContextUtils.setToken(this, s);
                    Platform.runLater(() -> loadSession(this));
                });
            } else if (gr.code() == 404) {
                toast("Invalid Token");
                Store.setJwtToken("", null);
                Platform.runLater(() -> loadPage(Login.class));
            } else {
                Platform.runLater(() -> loadPage(NetErr.class));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!Store.getJwtToken().isBlank() && PermissionCheck.hasOverlayPermission(this)) {
            Log.d("FloatingService", "onPause: " + Store.getJwtToken());
            startService(new Intent(this, FloatingService.class));
        } else {
            stopService(new Intent(this, FloatingService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(this, FloatingService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
package com.sdidsa.bondcheck.app.app_content.session.content.history.screen;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.content.history.HistorySection;
import com.sdidsa.bondcheck.app.app_content.session.content.screenshots.Screenshots;
import com.sdidsa.bondcheck.app.services.Action;
import com.sdidsa.bondcheck.app.services.SocketEventListener;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.responses.ScreenshotResponse;

import org.json.JSONException;

import java.util.List;

import retrofit2.Call;

public class ScreenHistory extends HistorySection {
    public ScreenHistory(Context owner) {
        super(owner, "screen_history", R.drawable.mobile_fill);

        setOnMore(() ->
                Platform.runBack(() -> {
                    Home home = Page.getInstance(owner, Home.class);
                    home.nextInto(Screenshots.class, null);
        }));

        SocketEventListener socket = Action.socketEventReceiver(owner);
        socket.on("screen_response", data -> {
            try {
                ScreenshotResponse sr = new ScreenshotResponse(data);

                if(root.getChildCount() >= 3) {
                    root.removeViewAt(1);
                }

                addScreen(sr, 0);
            } catch (JSONException e) {
                ErrorHandler.handle(e, "getting url from screenshot response event");
            }
        });
        socket.on("service_paused_screen", args -> {
            if(isAttachedToWindow()) {
                ContextUtils.toast(owner, "service paused");
            }
        });

        socket.on("screen_off", args ->{
            if(isAttachedToWindow()) {
                ContextUtils.toast(owner, "screen off");
            }
        });
    }

    private long last = -1;
    public void fetch() {
        ready = false;
        final long command = System.currentTimeMillis();
        last = command;
        startLoading();
        Call<List<ScreenshotResponse>> call = App.api(owner).
                getScreenshots();

        Service.enqueue(call, resp -> {
            if (command != last) return;
            ready = true;
            clearItems();
            stopLoading();

            if(resp.isSuccessful()) {
                List<ScreenshotResponse> list = resp.body();
                assert list != null;
                for (int i = 0; i < list.size() && i < 2; i++) {
                    addScreen(list.get(i), i);
                }
            }else {
                ContextUtils.toast(owner, "problem_string");
            }
        });
    }

    private void addScreen(ScreenshotResponse url, int index) {
        addItem(new ScreenshotThumbnail(owner,
                url), index);
    }
}

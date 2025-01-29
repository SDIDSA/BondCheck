package com.sdidsa.bondcheck.app.app_content.session.content.screenshots;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.content.main.Main;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemView;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Items;
import com.sdidsa.bondcheck.app.services.Action;
import com.sdidsa.bondcheck.app.services.SocketEventListener;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.requests.StringRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.responses.ScreenshotResponse;

import org.json.JSONException;

import java.util.List;

import retrofit2.Call;

public class Screenshots extends Items {

    public Screenshots(Context owner) {
        super(owner, "screenshots", "request_screen", R.drawable.mobile_outline);

        SocketEventListener socket = Action.socketEventReceiver(owner);
        socket.on("screen_response", data -> {
            try {
                ScreenshotResponse screen = new ScreenshotResponse(data);
                ImageProxy.getImage(owner, screen.asset_id(), ignore -> {
                    ScreenshotView item = ScreenshotView.make(owner, screen);
                    if(isAttachedToWindow()) {
                        item.setAlpha(0);

                        dragDown().setOnFinished(() -> {
                            items.addView(item, 0);
                            reset();
                            item.showAlone()
                                    .setOnFinished(this::reset).start();
                        }).start();
                    } else {
                        if(items.getChildCount() >= 3) {
                            ItemView last = (ItemView) items.getChildAt(items.getChildCount() - 1);
                            items.removeView(last);
                        }
                        items.addView(item, 0);
                    }
                });
            } catch (JSONException e) {
                ErrorHandler.handle(e, "parsing socket received json screenshot");
            }
        });
        socket.on("service_paused_screen", args -> {
            request.stopLoading();
            if(isAttachedToWindow()) {
                ContextUtils.toast(owner, "service paused");
            }
        });

        socket.on("screen_off", args ->{
            request.stopLoading();
            if(isAttachedToWindow()) {
                ContextUtils.toast(owner, "screen off");
            }
        });
    }

    @Override
    protected void requestItem() {
        Main main = Fragment.getInstance(owner, Main.class);
        assert main != null;
        String otherUser = main.getBondStatus().getOther_user();

        request.startLoading();

        Call<GenericResponse> call = App.api(owner).requestScreenshot(
                new StringRequest(otherUser)
        );

        Service.enqueue(call, resp -> {
            request.stopLoading();
            if (resp.code() == 503) {
                ContextUtils.toast(owner, "user offline");
            } else if(!resp.isSuccessful()){
                ContextUtils.toast(owner, "problem_string");
            }
        });
    }

    @Override
    protected void fetch() {
        fetching = true;
        refresh.hide().start();
        showLoader().start();
        hideAll(() -> {
            items.removeAllViews();
            Call<List<ScreenshotResponse>> call = App.api(owner).getScreenshots();

            Service.enqueue(call, resp -> {
                refresh.show().start();
                hideLoader().start();
                if (resp.isSuccessful()) {
                    List<ScreenshotResponse> list = resp.body();
                    assert list != null;
                    for (ScreenshotResponse screen : list) {
                        ScreenshotView view = ScreenshotView.make(owner, screen);
                        view.setAlpha(0);
                        items.addView(view);
                    }
                    Platform.runAfterScaled(() -> showAll(() -> fetching = false), 50);
                } else {
                    ContextUtils.toast(owner, "problem_string");
                }
            });
        });
    }
}

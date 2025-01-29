package com.sdidsa.bondcheck.app.app_content.session.content.locations;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
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
import com.sdidsa.bondcheck.models.responses.LocationResponse;

import org.json.JSONException;

import java.util.List;

import retrofit2.Call;

public class Locations extends Items {

    public Locations(Context owner) {
        super(owner, "locations", "request_location", R.drawable.location_outline);

        SocketEventListener socket = Action.socketEventReceiver(owner);
        socket.on("location_response", data -> {
            try {
                LocationResponse location = new LocationResponse(data);
                LocationView item = LocationView.make(owner, location);
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
            } catch (JSONException e) {
                ErrorHandler.handle(e, "parsing socket received json location");
            }
        });
        socket.on("service_paused_location", args -> {
            if(isAttachedToWindow()) {
                ContextUtils.toast(owner, "service paused");
            }
        });

        socket.on("location_off", args ->{
            if(isAttachedToWindow()) {
                ContextUtils.toast(owner, "location off");
            }
        });
    }

    @Override
    protected void requestItem() {
        Main main = Fragment.getInstance(owner, Main.class);
        assert main != null;
        String otherUser = main.getBondStatus().getOther_user();

        request.startLoading();

        Call<GenericResponse> call = App.api(owner).requestLocation(
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
            Call<List<LocationResponse>> call = App.api(owner).getLocations();

            Service.enqueue(call, resp -> {
                refresh.show().start();
                hideLoader().start();
                if (resp.isSuccessful()) {
                    List<LocationResponse> list = resp.body();
                    assert list != null;
                    for (LocationResponse location : list) {
                        LocationView view = LocationView.make(owner, location);
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

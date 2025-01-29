package com.sdidsa.bondcheck.app.app_content.session.content.history.location;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.content.history.HistorySection;
import com.sdidsa.bondcheck.app.app_content.session.content.locations.Locations;
import com.sdidsa.bondcheck.app.services.Action;
import com.sdidsa.bondcheck.app.services.SocketEventListener;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.responses.LocationResponse;

import org.json.JSONException;

import java.util.List;

import retrofit2.Call;

public class LocationHistory extends HistorySection {
    public LocationHistory(Context owner) {
        super(owner, "location_history", R.drawable.location_fill);

        setOnMore(() ->
                Platform.runBack(() -> {
                    Home home = Page.getInstance(owner, Home.class);
                    home.nextInto(Locations.class, null);
        }));

        SocketEventListener socket = Action.socketEventReceiver(owner);
        socket.on("location_response", args -> {
            try {
                LocationResponse rr = new LocationResponse(args);

                if(root.getChildCount() >= 3) {
                    root.removeViewAt(1);
                }

                addLocation(rr, 0);

            } catch (JSONException e) {
                ErrorHandler.handle(e, "getting url from record response event");
            }
        });

        socket.on("service_paused_location", args -> {
            if(isAttachedToWindow()) {
                ContextUtils.toast(owner, "service paused");
            }
        });
    }

    private long last = -1;
    public void fetch() {
        ready = false;
        final long command = System.currentTimeMillis();
        last = command;
        startLoading();
        Call<List<LocationResponse>> call = App.api(owner).
                getLocations();

        Service.enqueue(call, resp -> {
            if (command != last) return;
            ready = true;
            clearItems();
            stopLoading();

            if(resp.isSuccessful()) {
                List<LocationResponse> list = resp.body();
                assert list != null;
                for (int i = 0; i < list.size() && i < 2; i++) {
                    addLocation(list.get(i), i);
                }
            }
        });
    }

    private void addLocation(LocationResponse data, int index) {
        addItem(new LocationThumbnail(owner,
                data), index);
    }
}

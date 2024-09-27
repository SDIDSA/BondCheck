package com.sdidsa.bondcheck.app.app_content.session.content.history.microphone;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.Page;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.Home;
import com.sdidsa.bondcheck.app.app_content.session.content.history.HistorySection;
import com.sdidsa.bondcheck.app.app_content.session.content.records.Records;
import com.sdidsa.bondcheck.app.services.Action;
import com.sdidsa.bondcheck.app.services.SocketEventListener;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.responses.RecordResponse;

import org.json.JSONException;

import java.util.List;

import retrofit2.Call;

public class MicrophoneHistory extends HistorySection {
    public MicrophoneHistory(Context owner) {
        super(owner, "mic_history", R.drawable.mic_fill);

        setOnMore(() ->
                Platform.runBack(() -> {
                    Home home = Page.getInstance(owner, Home.class);
                    home.nextInto(Records.class, null);
        }));

        SocketEventListener socket = Action.socketEventReceiver(owner);
        socket.on("mic_response", args -> {
            try {
                RecordResponse rr = new RecordResponse(args);

                if(root.getChildCount() >= 3) {
                    root.removeViewAt(1);
                }

                addRecord(rr, 0);

            } catch (JSONException e) {
                ErrorHandler.handle(e, "getting url from record response event");
            }
        });

        socket.on("service_paused_mic", args -> {
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
        Call<List<RecordResponse>> call = App.api(owner).
                getRecords();

        Service.enqueue(call, resp -> {
            if (command != last) return;
            ready = true;
            clearItems();
            stopLoading();

            if(resp.isSuccessful()) {
                List<RecordResponse> list = resp.body();
                assert list != null;
                for (int i = 0; i < list.size() && i < 2; i++) {
                    addRecord(list.get(i), i);
                }
            }
        });
    }

    private void addRecord(RecordResponse data, int index) {
        addItem(new MicrophoneThumbnail(owner,
                data), index);
    }
}

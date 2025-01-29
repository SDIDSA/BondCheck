package com.sdidsa.bondcheck.app.app_content.session.content.records;

import android.content.Context;
import android.view.ViewGroup;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.content.main.Main;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemView;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.Items;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.requests.StringRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.responses.RecordResponse;

import org.json.JSONException;

import java.util.List;

import retrofit2.Call;

public class Records extends Items {
    private final DurationPicker dur;
    public Records(Context owner) {
        super(owner, "records", "request_microphone", R.drawable.mic_outline);

        dur = new DurationPicker(owner);
        dur.setOnPick(this::privateRequest);

        ViewGroup par = ((ViewGroup)request.getParent());

        par.removeView(request);
        par.addView(dur);
        par.addView(request);

        socket.on("mic_response", data -> {
            try {
                RecordResponse record = new RecordResponse(data);
                RecordView item = RecordView.make(owner, record);
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
                ErrorHandler.handle(e, "parsing socket received json record");
            }
        });
        socket.on("service_paused_mic", args -> {
            if(isAttachedToWindow()) {
                ContextUtils.toast(owner, "service paused");
            }
        });
        socket.on("service_disabled_mic", args -> {
            if(isAttachedToWindow()) {
                ContextUtils.toast(owner, "service disabled");
            }
        });
    }

    @Override
    protected void requestItem() {
        if(dur.isShown()) dur.hide();
        else dur.show();

    }

    private void privateRequest(int duration) {
        request.startLoading();
        Main main = Fragment.getInstance(owner, Main.class);
        assert main != null;
        String otherUser = main.getBondStatus().getOther_user();

        Call<GenericResponse> call = App.api(owner).requestMic(
                new StringRequest(otherUser + "_" + duration)
        );

        Service.enqueue(call, resp -> {
            request.stopLoading();
            if(resp.code() == 503) {
                ContextUtils.toast(owner, "user offline");
            } else if(!resp.isSuccessful()){
                ContextUtils.toast(owner, "problem_string");
            }
        });
    }

    protected void fetch() {
        fetching = true;
        refresh.hide().start();
        showLoader().start();
        hideAll(() -> {
            items.removeAllViews();
            Call<List<RecordResponse>> call = App.api(owner).getRecords();

            Service.enqueue(call, resp -> {
                refresh.show().start();
                hideLoader().start();
                if (resp.isSuccessful()) {
                    List<RecordResponse> list = resp.body();
                    assert list != null;
                    for (RecordResponse record : list) {
                        RecordView view = RecordView.make(owner, record);
                        view.setAlpha(0);
                        items.addView(view);
                    }
                    Platform.runBack(() -> showAll(() -> fetching = false));
                } else {
                    ContextUtils.toast(owner, "problem_string");
                }
            });
        });
    }
}

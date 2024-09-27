package com.sdidsa.bondcheck.app.app_content.session.content.history.microphone;

import android.content.Context;

import androidx.annotation.Nullable;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.audio.AudioProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredSpinLoading;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.content.history.HistoryThumbnail;
import com.sdidsa.bondcheck.app.app_content.session.content.records.RecordOverlay;
import com.sdidsa.bondcheck.models.responses.RecordResponse;

public class MicrophoneThumbnail extends HistoryThumbnail {
    private final RecordResponse data;

    private final ColoredSpinLoading loader;
    private final ColoredIcon play;

    public MicrophoneThumbnail(Context owner) {
        this(owner, null);
    }

    public MicrophoneThumbnail(Context owner, @Nullable RecordResponse data) {
        super(owner);
        this.data = data;
        setPadding(5);

        play = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.play);
        play.setSize(48);
        play.setPadding(8);
        play.setAutoMirror(true);

        loader = new ColoredSpinLoading(owner, Style.TEXT_SEC, 38);

        startLoading();

        if(data == null) return;

        AudioProxy.getAudio(owner, data.asset_id(), f -> stopLoading());
        setOnClickListener((e) -> RecordOverlay.getInstance(owner).show(data));
    }

    private void startLoading() {
        removeAllViews();
        addCentered(loader);
        loader.startLoading();
    }

    private void stopLoading() {
        Platform.runLater(() -> {
            removeAllViews();
            loader.stopLoading();
            addCentered(play);
        });
    }

    public RecordResponse getData() {
        return data;
    }
}

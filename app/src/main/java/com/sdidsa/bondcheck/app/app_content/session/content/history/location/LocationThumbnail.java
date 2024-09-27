package com.sdidsa.bondcheck.app.app_content.session.content.history.location;

import android.content.Context;

import androidx.annotation.Nullable;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.app.app_content.session.content.locations.LocationOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.history.HistoryThumbnail;
import com.sdidsa.bondcheck.models.responses.LocationResponse;

public class LocationThumbnail extends HistoryThumbnail {
    private final LocationResponse data;

    public LocationThumbnail(Context owner) {
        this(owner, null);
    }

    public LocationThumbnail(Context owner, @Nullable LocationResponse data) {
        super(owner);
        this.data = data;
        setPadding(5);

        ColoredIcon play = new ColoredIcon(owner, Style.TEXT_SEC, R.drawable.map_marker);
        play.setSize(48);
        play.setPadding(8);
        play.setAutoMirror(true);

        addCentered(play);

        if(data == null) return;

        setOnClickListener((e) -> LocationOverlay.getInstance(owner).show(data));
    }

    public LocationResponse getData() {
        return data;
    }
}

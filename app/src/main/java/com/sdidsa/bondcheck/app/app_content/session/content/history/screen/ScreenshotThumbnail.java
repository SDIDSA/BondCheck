package com.sdidsa.bondcheck.app.app_content.session.content.history.screen;

import android.content.Context;

import androidx.annotation.Nullable;

import com.sdidsa.bondcheck.abs.components.controls.image.ImageProxy;
import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.history.HistoryThumbnail;
import com.sdidsa.bondcheck.app.app_content.session.content.shared.HomeSection;
import com.sdidsa.bondcheck.app.app_content.session.content.screenshots.ScreenshotOverlay;
import com.sdidsa.bondcheck.models.responses.ScreenshotResponse;

public class ScreenshotThumbnail extends HistoryThumbnail {
    private final ScreenshotResponse data;

    public ScreenshotThumbnail(Context owner) {
        this(owner, null);
    }
    public ScreenshotThumbnail(Context owner, @Nullable ScreenshotResponse data) {
        super(owner);
        this.data = data;
        int size = SizeUtils.dipToPx(HomeSection.ITEM_SIZE, owner);

        NetImage image = new NetImage(owner);
        image.setSize(HomeSection.ITEM_SIZE);
        image.startLoading();
        image.setCornerRadius(10);

        addCentered(image);

        if(data == null) return;

        setOnClickListener(e -> ScreenshotOverlay.getInstance(owner).show(data));

        ImageProxy.getImageThumb(owner, data.asset_id(), size, image::setImageBitmap);
    }

    public ScreenshotResponse getData() {
        return data;
    }
}

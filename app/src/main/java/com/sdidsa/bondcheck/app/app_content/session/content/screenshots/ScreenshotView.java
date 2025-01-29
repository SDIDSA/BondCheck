package com.sdidsa.bondcheck.app.app_content.session.content.screenshots;

import android.content.Context;

import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.app.app_content.session.content.shared.HomeSection;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemView;
import com.sdidsa.bondcheck.models.responses.ScreenshotResponse;

public class ScreenshotView extends ItemView {

    public synchronized static ScreenshotView make(Context owner, ScreenshotResponse screen) {
        ScreenshotView view = instance(owner, ScreenshotView.class);
        view.loadScreen(screen);
        return view;
    }

    private final NetImage img;

    public ScreenshotView(Context owner) {
        super(owner);

        setPadding(15);
        setCornerRadius(15);

        img = new NetImage(owner, Style.BACK_SEC);
        img.setCornerRadius(10);
        img.setSize(HomeSection.ITEM_SIZE);
        addViews(img);
    }

    private void loadScreen(ScreenshotResponse screen) {
        loadItem(screen);

        img.setImageThumbUrl(screen.asset_id(), HomeSection.ITEM_SIZE);
        second.setText(screen.app());

        setOnClickListener((e) -> ScreenshotOverlay.getInstance(owner).show(screen));
    }
}

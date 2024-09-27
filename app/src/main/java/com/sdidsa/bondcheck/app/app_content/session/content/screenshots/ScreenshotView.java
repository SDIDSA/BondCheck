package com.sdidsa.bondcheck.app.app_content.session.content.screenshots;

import android.content.Context;

import com.sdidsa.bondcheck.abs.components.controls.image.NetImage;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.shared.HomeSection;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemView;
import com.sdidsa.bondcheck.models.responses.ScreenshotResponse;

import java.util.ArrayList;

public class ScreenshotView extends ItemView {
    private static final ArrayList<ScreenshotView> cache = new ArrayList<>();

    public synchronized static ScreenshotView make(Context owner, ScreenshotResponse screen) {
        cache.removeIf(item -> item.getOwner() != owner);

        ScreenshotView view = null;
        for(ScreenshotView c : cache) {
            if(c.getParent() == null) {
                view = c;
                break;
            }
        }

        if(view == null) {
            view = new ScreenshotView(owner);
            cache.add(view);
        }

        view.loadScreen(screen);

        return view;
    }

    private final NetImage img;

    private ScreenshotView(Context owner) {
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

        img.setImageThumbUrl(screen.asset_id(), ContextUtils.dipToPx(84, owner));
        second.setText(screen.app());

        setOnClickListener((e) -> ScreenshotOverlay.getInstance(owner).show(screen));
    }
}

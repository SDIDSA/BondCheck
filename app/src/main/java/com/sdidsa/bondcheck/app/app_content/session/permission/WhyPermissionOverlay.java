package com.sdidsa.bondcheck.app.app_content.session.permission;

import android.content.Context;
import android.view.View;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.components.controls.button.Button;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.item_display.ItemOverlayHeader;

public class WhyPermissionOverlay extends PartialSlideOverlay {
    private final StackPane illustration;
    protected final Button grant;
    protected final Button skip;

    public WhyPermissionOverlay(Context owner) {
        this(owner, "", "", null);
    }

    public WhyPermissionOverlay(Context owner, String titleString, String headerString,
                                Runnable request) {
        super(owner, .7);

        list.setPadding(20);
        list.setSpacing(15);

        ItemOverlayHeader top = new ItemOverlayHeader(owner);
        top.hideSave();
        top.hideInfo();
        top.setOnClose(this::hide);

        top.setTitle(titleString);

        ColoredLabel header = new ColoredLabel(owner, Style.TEXT_SEC, headerString)
                .setFont(new Font(18));
        header.setLineSpacing(4);

        illustration = new StackPane(owner);
        ContextUtils.spacer(illustration, Orientation.VERTICAL);

        skip = new ColoredButton(owner, Style.BACK_SEC, Style.TEXT_NORM, "I don't need this")
                .setFont(new Font(18, FontWeight.MEDIUM));
        skip.setVisibility(GONE);

        grant = new ColoredButton(owner, Style.ACCENT, Style.WHITE, "Grant Now")
                .setFont(new Font(18, FontWeight.MEDIUM));
        grant.setOnClick(() -> {
            if(request != null) {
                request.run();
            }
            hide();
        });

        list.addView(top);
        list.addView(header);

        list.addView(illustration);
        list.addView(skip);
        list.addView(grant);

        addOnShowing(() -> {
            View[] views = ContextUtils.getViewChildren(illustration);

            Animation.sequenceFadeInUp(owner, views)
                    .start();
        });
    }

    protected void addLayer(@DrawableRes int res, StyleToColor fill) {
        addLayer(res, fill, 1);
    }

    protected void addLayer(@DrawableRes int res, StyleToColor fill, float alpha) {
        ColoredIcon layer = new ColoredIcon(owner, fill, res);
        layer.setImageAlpha(alpha);
        layer.setLayoutParams(new LayoutParams(-1, -1));
        illustration.addCentered(layer);
    }
}

package com.sdidsa.bondcheck.app.app_content.session.content.main;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateYAnimation;
import com.sdidsa.bondcheck.abs.animation.view.scale.ScaleXYAnimation;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;

public class StickyCreate extends ColoredStackPane {
    private final Animation showCreate;
    private final Animation hideCreate;
    private boolean shown = false;

    public StickyCreate(Context owner) {
        super(owner, Style.BACK_TER);
        setCornerRadius(56);

        ColoredIcon icon = new ColoredIcon(owner,
                Style.TEXT_SEC, R.drawable.create, 56);
        addView(icon);
        icon.setPadding(14);

        int byPx = SizeUtils.by(owner);
        setAlpha(0f);

        showCreate = new ParallelAnimation(300)
                .addAnimation(new TranslateYAnimation(this, byPx, 0))
                .addAnimation(new AlphaAnimation(this, 0, 1))
                .addAnimation(new ScaleXYAnimation(this, .7f, 1))
                .setInterpolator(Interpolator.EASE_OUT);
        MarginUtils.setMarginRight(this, owner, 10);

        hideCreate = showCreate.reverse();
    }

    private long lastChange = -1;
    public void show() {
        if(System.currentTimeMillis() - lastChange < 500) return;
        if (shown || showCreate == null) return;
        shown = true;
        hideCreate.stop();
        showCreate.start();
        lastChange = System.currentTimeMillis();
    }

    public void hide() {
        if(System.currentTimeMillis() - lastChange < 500) return;
        if (!shown || hideCreate == null) return;
        shown = false;
        showCreate.stop();
        hideCreate.start();
        lastChange = System.currentTimeMillis();
    }
}

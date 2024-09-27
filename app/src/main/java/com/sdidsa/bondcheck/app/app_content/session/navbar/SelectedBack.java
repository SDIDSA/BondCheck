package com.sdidsa.bondcheck.app.app_content.session.navbar;

import android.content.Context;
import android.graphics.Rect;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.layout.ColoredStackPane;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.locale.Localized;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class SelectedBack extends StackPane implements Localized {
    private final ColoredStackPane back;
    private final ColoredStackPane border;
    public SelectedBack(Context owner) {
        super(owner);
        setTranslationY(-ContextUtils.dipToPx(28, owner));

        back = new ColoredStackPane(owner, Style.BACK_PRI);
        border = new ColoredStackPane(owner, Style.BACK_PRI);

        back.setLayoutParams(new LayoutParams(-1, -1));
        border.setLayoutParams(new LayoutParams(-1, -1));
        setLayoutParams(new LayoutParams(-1, ContextUtils.dipToPx(100, owner)));
        ContextUtils.setMarginUnified(back, owner, 10);

        addView(border);
        addView(back);

        applyLocale(ContextUtils.getLocale(owner));
    }

    public Animation applyClip(boolean oc, boolean nc, int ow, int nw) {
        int ot = oc ? 0 : ContextUtils.dipToPx(28, owner);
        int nt = nc ? 0 : ContextUtils.dipToPx(28, owner);

        Rect r = new Rect(0, 0, getWidth(), ContextUtils.dipToPx(100, owner));
        return new Animation(300) {
            @Override
            public void update(float v) {
                float ct = ot + (nt - ot) * v;
                float cw = ow + (nw - ow) * v;
                r.set(0, (int) ct, (int) cw, r.bottom);
                border.setClipBounds(r);
            }
        }.setInterpolator(Interpolator.EASE_OUT);
    }

    public void applyClip(boolean nc, int nw) {
        Rect r = new Rect(0, nc ? 0 : ContextUtils.dipToPx(28, owner), nw, ContextUtils.dipToPx(100, owner));
        border.setClipBounds(r);
    }

    @Override
    public void setCornerRadius(float[] radius) {
        back.setCornerRadius(radius);
        border.setCornerRadius(radius);
    }

    public void setFill(StyleToColor fill) {
        back.setFill(fill);
    }

    public void setFill(int color, StyleToColor fill) {
        back.setFill(color, fill);
    }

    @Override
    public void applyLocale(Locale locale) {
        setScaleX(locale.isRtl() ? -1 : 1);
        if((locale.isRtl() && getTranslationX() > 0) ||
                (!locale.isRtl() && getTranslationX() < 0))
            setTranslationX(-getTranslationX());
    }

    @Override
    public void applyLocale(Property<Locale> locale) {
        Localized.bindLocale(this, locale);
    }
}

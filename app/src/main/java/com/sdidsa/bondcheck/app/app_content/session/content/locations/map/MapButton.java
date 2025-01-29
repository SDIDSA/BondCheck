package com.sdidsa.bondcheck.app.app_content.session.content.locations.map;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.abs.utils.view.MarginUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;

public class MapButton extends ColoredIcon {
    private int horizontal = 0, vertical = 0;

    public MapButton(Context owner) {
        this(owner, R.drawable.empty);
    }

    public MapButton(Context owner, int id) {
        super(owner, Style.TEXT_SEC,
                Style.BACK_PRI, id, 42);
        setPadding(10);
        setCornerRadius(10);
        setElevation(SizeUtils.dipToPx(10, owner));
        setLayoutParams(new LayoutParams(-2, -2));
        setSize(42);

        applyLocale(LocaleUtils.getLocale(owner));
    }

    public void setOffset(int vertical, int horizontal) {
        this.horizontal = horizontal;
        this.vertical = vertical;

        applyLocale(LocaleUtils.getLocale(owner).get());
    }

    @Override
    public void applyLocale(Locale locale) {
        super.applyLocale(locale);
        MarginUtils.setMarginTopRight(this, owner,
                (52 * vertical) + 10,
                (52 * horizontal) + 10);
    }
}

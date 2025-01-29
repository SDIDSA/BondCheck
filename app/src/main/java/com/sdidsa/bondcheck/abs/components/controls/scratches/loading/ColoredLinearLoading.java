package com.sdidsa.bondcheck.abs.components.controls.scratches.loading;

import android.content.Context;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;

public class ColoredLinearLoading extends LinearLoading implements Styleable {
    private final StyleToColor fill;

    public ColoredLinearLoading(Context owner) {
        this(owner, Style.TEXT_SEC, 20);
    }

    public ColoredLinearLoading(Context owner, StyleToColor fill, float size) {
        super(owner, size);
        this.fill = fill;

        applyStyle(StyleUtils.getStyle(owner));
    }

    @Override
    public void applyStyle(Style style) {
        setFill(fill.get(style));
    }

}

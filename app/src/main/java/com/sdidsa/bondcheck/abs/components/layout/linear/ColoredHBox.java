package com.sdidsa.bondcheck.abs.components.layout.linear;

import android.content.Context;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;

public class ColoredHBox extends HBox implements Styleable {
    private StyleToColor fill;
    public ColoredHBox(Context owner) {
        this(owner, Style.EMPTY);
    }

    public ColoredHBox(Context owner, StyleToColor fill) {
        super(owner);
        this.fill = fill;

        applyStyle(StyleUtils.getStyle(owner));
    }

    public void setFill(StyleToColor fill) {
        this.fill = fill;
        applyStyle(StyleUtils.getStyle(owner).get());
    }

    @Override
    public void applyStyle(Style style) {
        setBackground(fill.get(style));
    }

}

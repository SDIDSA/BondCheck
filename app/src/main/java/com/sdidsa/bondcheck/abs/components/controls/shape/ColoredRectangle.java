package com.sdidsa.bondcheck.abs.components.controls.shape;

import android.content.Context;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class ColoredRectangle extends Rectangle implements Styleable {
    private final StyleToColor fill;

    public ColoredRectangle(Context owner) {
        this(owner, null);
    }

    public ColoredRectangle(Context owner, StyleToColor fill) {
        super(owner);
        this.fill = fill;

        applyStyle(ContextUtils.getStyle(owner));
    }

    @Override
    public void applyStyle(Style style) {
        if(fill != null)
            setFill(fill.get(style));
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}

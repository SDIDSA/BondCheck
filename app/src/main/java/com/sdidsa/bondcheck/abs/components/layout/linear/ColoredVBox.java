package com.sdidsa.bondcheck.abs.components.layout.linear;

import android.content.Context;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.style.StyleToColor;

public class ColoredVBox extends VBox implements Styleable {
    private StyleToColor fill;

    public ColoredVBox(Context owner) {
        this(owner, Style.BACK_PRI);
    }

    public ColoredVBox(Context owner, StyleToColor fill) {
        super(owner);
        this.fill = fill;

        applyStyle(ContextUtils.getStyle(owner));
    }

    public void setFill(StyleToColor fill) {
        this.fill = fill;
        applyStyle(ContextUtils.getStyle(owner).get());
    }

    @Override
    public void applyStyle(Style style) {
        setBackground(fill.get(style));
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}

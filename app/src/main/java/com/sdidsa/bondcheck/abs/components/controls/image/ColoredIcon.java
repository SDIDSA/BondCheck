package com.sdidsa.bondcheck.abs.components.controls.image;

import android.content.Context;

import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.data.property.Property;

public class ColoredIcon extends ColorIcon implements Styleable {
    private StyleToColor fill;
    private StyleToColor back;

    public ColoredIcon(Context owner) {
        this(owner, Style.ACCENT, -1);
    }

    public ColoredIcon(Context owner, StyleToColor color , int id) {
        this(owner, color, null, id);
    }

    public ColoredIcon(Context owner, StyleToColor fill, StyleToColor back , int id) {
        super(owner, id);
        this.fill = fill;
        setBack(back);

        applyStyle(ContextUtils.getStyle(owner));
    }

    public ColoredIcon(Context owner, StyleToColor fill, StyleToColor back ,int id, float size) {
        this(owner, fill, back, id);
        setSize(size);
    }

    public ColoredIcon(Context owner, StyleToColor fill ,int id, float size) {
        this(owner, fill, null, id, size);
    }

    public void setFill(StyleToColor fill) {
        this.fill = fill;
        applyStyle(ContextUtils.getStyle(owner).get());
    }

    public void setBack(StyleToColor back) {
        this.back = back;
        applyStyle(ContextUtils.getStyle(owner).get());
    }

    @Override
    public void applyStyle(Style style) {
        if(fill != null) {
            setFill(fill.get(style));
        }
        if(back != null) {
            setBackgroundColor(back.get(style));
        }
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}

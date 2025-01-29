package com.sdidsa.bondcheck.abs.components.controls.text;

import android.content.Context;

import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;

public class ColoredLabel extends Label implements Styleable {
    private StyleToColor fill;
    private final StyleToColor back;

    public ColoredLabel(Context context) {
        this(context, Style.TEXT_NORM, "Label text");
    }

    public ColoredLabel(Context owner, StyleToColor fill, String key) {
        this(owner, fill, null, key);
    }

    public ColoredLabel(Context owner, StyleToColor fill, StyleToColor back, String key) {
        super(owner, key);

        this.fill = fill;
        this.back = back;

        applyStyle(StyleUtils.getStyle(owner));
    }

    public void setFill(StyleToColor fill) {
        this.fill = fill;
        applyStyle(StyleUtils.getStyle(owner).get());
    }

    @Override
    public void applyStyle(Style style) {
        if(fill != null) {
            setFill(fill.get(style));
        }
        if(back != null) {
            setBackground(back.get(style));
        }
    }

}

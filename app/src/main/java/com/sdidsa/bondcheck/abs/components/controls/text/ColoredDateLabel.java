package com.sdidsa.bondcheck.abs.components.controls.text;

import android.content.Context;

import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;

public class ColoredDateLabel extends DateLabel implements Styleable {
    private final StyleToColor fill;
    private final StyleToColor back;

    public ColoredDateLabel(Context context) {
        this(context, Style.TEXT_NORM);
    }

    public ColoredDateLabel(Context owner, StyleToColor fill) {
        this(owner, fill, null);
    }

    public ColoredDateLabel(Context owner, StyleToColor fill, StyleToColor back) {
        super(owner);

        this.fill = fill;
        this.back = back;

        applyStyle(StyleUtils.getStyle(owner));
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

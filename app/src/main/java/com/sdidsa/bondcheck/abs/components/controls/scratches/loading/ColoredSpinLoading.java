package com.sdidsa.bondcheck.abs.components.controls.scratches.loading;

import android.content.Context;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;

public class ColoredSpinLoading extends SpinLoading implements Styleable {
    private final StyleToColor fill;

    public ColoredSpinLoading(Context context) {
        this(context, Style.TEXT_SEC);
    }

    public ColoredSpinLoading(Context owner, StyleToColor fill) {
        super(owner);
        this.fill = fill;
        applyStyle(StyleUtils.getStyle(owner));
    }

    public ColoredSpinLoading(Context owner, StyleToColor fill, float size) {
        this(owner, fill);
        setSize(size);
    }

    @Override
    public void applyStyle(Style style) {
        setFill(fill.get(style));
    }

}

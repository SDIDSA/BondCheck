package com.sdidsa.bondcheck.abs.components.layout;

import android.content.Context;

import androidx.annotation.ColorInt;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.style.StyleToColor;

public class ColoredStackPane extends StackPane implements Styleable {
    private StyleToColor fill;
    private final StyleToColor stroke;
    private final int strokeWidth;

    public ColoredStackPane(Context owner) {
        this(owner, null, null, -1);
    }

    public ColoredStackPane(Context owner, StyleToColor fill, StyleToColor stroke, int strokeWidth) {
        super(owner);
        this.fill = fill;
        this.stroke = stroke;
        this.strokeWidth = strokeWidth;

        applyStyle(ContextUtils.getStyle(owner));
    }

    public void setFill(StyleToColor fill) {
        this.fill = fill;
        applyStyle(ContextUtils.getStyle(owner).get());
    }

    public void setFill(@ColorInt int color, StyleToColor fill) {
        this.fill = fill;
        setBackground(color);
    }

    public ColoredStackPane(Context owner, StyleToColor fill) {
        this(owner, fill, null, -1);
    }

    @Override
    public void applyStyle(Style style) {
        if(fill != null) {
            setBackground(fill.get(style));
        }
        if(stroke != null) {
            setBorder(stroke.get(style), strokeWidth);
        }
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}

package com.sdidsa.bondcheck.abs.components.controls.scratches;

import android.content.Context;

import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;

public class ColoredSeparator extends Separator implements Styleable {

    private final StyleToColor color;

    public ColoredSeparator(Context owner) {
        this(owner, Orientation.HORIZONTAL, 0, Style.TEXT_MUT);
    }

    public ColoredSeparator(Context owner, Orientation orientation, float margin, StyleToColor color) {
        super(owner, orientation, margin);
        this.color = color;

        applyStyle(StyleUtils.getStyle(owner));
    }

    @Override
    public void applyStyle(Style style) {
        setColor(color.get(style));
    }

}

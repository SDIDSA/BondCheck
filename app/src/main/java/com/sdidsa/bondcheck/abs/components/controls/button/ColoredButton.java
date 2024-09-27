package com.sdidsa.bondcheck.abs.components.controls.button;

import android.content.Context;
import android.graphics.Color;

import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.data.property.Property;

public class ColoredButton extends Button implements Styleable {
    private StyleToColor fill;
    private StyleToColor textFill;
    private StyleToColor border;
    private float borderWidth;

    public ColoredButton(Context context) {
        this(context, s -> Color.BLACK, s -> Color.WHITE, "Button Text");
    }

    public ColoredButton(Context owner,
                         StyleToColor fill,
                         StyleToColor textFill,
                         String text) {
        super(owner, text);

        this.fill = fill;
        this.textFill = textFill;

        setElevation(ContextUtils.dipToPx(5, owner));

        applyStyle(ContextUtils.getStyle(owner));
    }

    public void setTextFill(StyleToColor textFill) {
        this.textFill = textFill;
        applyStyle(ContextUtils.getStyle(owner).get());
        for(int i = 0; i < content.getChildCount(); i++) {
            if(content.getChildAt(i) instanceof ColoredIcon icon) {
                icon.setFill(textFill);
            }
        }
    }

    public void setFill(StyleToColor fill) {
        this.fill = fill;
        applyStyle(ContextUtils.getStyle(owner).get());
    }

    public void setBorder(StyleToColor border, float width) {
        this.border = border;
        this.borderWidth = width;
        applyStyle(ContextUtils.getStyle(owner).get());
    }

    @Override
    public void applyStyle(Style style) {
        setFill(fill.get(style));
        setTextFill(textFill.get(style));
        if(border != null) {
            setBorder(border.get(style), borderWidth);
        }
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}

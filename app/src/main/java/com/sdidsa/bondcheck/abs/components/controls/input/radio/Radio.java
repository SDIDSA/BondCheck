package com.sdidsa.bondcheck.abs.components.controls.input.radio;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.controls.shape.Rectangle;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.data.property.Property;

public class Radio extends StackPane implements Styleable {
    private final GradientDrawable background;

    private final Rectangle checkMark;
    private final Property<Boolean> checked = new Property<>(false);

    private float size;
    public Radio(Context owner) {
        super(owner);

        background = new GradientDrawable();
        background.setColor(Color.TRANSPARENT);
        background.setCornerRadius(ContextUtils.dipToPx(5, owner));

        setBackground(background);

        setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        setClipToOutline(true);

        checkMark = new Rectangle(owner);
        checkMark.setAlpha(0);

        addView(checkMark);

        setSize(20);

        ContextUtils.alignInFrame(checkMark, Alignment.CENTER);

        applyStyle(ContextUtils.getStyle(owner));
    }

    private Animation showCheck = null;
    private Animation hideCheck = null;

    public Radio(Context owner, float size) {
        this(owner);
        setSize(size);
    }

    public void setChecked(boolean checked) {
        if(checked == isChecked()) return;
        this.checked.set(checked);
        if(checked) {
            if(showCheck == null) {
                showCheck = Animation.fadeInUp(owner, .5f, checkMark)
                        .setInterpolator(Interpolator.OVERSHOOT);
            }
            if(hideCheck != null) hideCheck.stop();
            showCheck.stop();
            showCheck.start();
        }else {
            if(hideCheck == null) {
                hideCheck = Animation.fadeOutDown(owner, .1f, checkMark)
                        .setInterpolator(Interpolator.EASE_OUT);
            }
            if(showCheck != null) showCheck.stop();
            hideCheck.stop();
            hideCheck.start();
        }
        applyStyle(ContextUtils.getStyle(owner).get());
    }

    public boolean isChecked() {
        return checked.get();
    }

    public Property<Boolean> checkedProperty() {
        return checked;
    }

    public void setSize(float size) {
        this.size = size;
        int sizePx = ContextUtils.dipToPx(size, owner);

        ViewGroup.LayoutParams old = getLayoutParams();
        if(old == null) {
            setLayoutParams(new ViewGroup.LayoutParams(sizePx, sizePx));
        }else {
            old.width = sizePx;
            old.height = sizePx;
            requestLayout();
        }

        background.setCornerRadius(size * 2);
        float s = size / 2;
        checkMark.setSize(s, s);
        checkMark.setRadius(s);

        applyStyle(ContextUtils.getStyle(owner).get());
    }

    @Override
    public void applyStyle(Style style) {
        background.setStroke(ContextUtils.dipToPx(size / 10, owner), style.getTextSecondary());
        background.setColor(Color.TRANSPARENT);
        checkMark.setFill(style.getTextSecondary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}

package com.sdidsa.bondcheck.abs.animation.view;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

import android.view.View;
import android.widget.TextView;

import com.sdidsa.bondcheck.abs.animation.base.ViewAnimation;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class FontSizeAnimation extends ViewAnimation {

    private final TextView view;

    public FontSizeAnimation(TextView view, float to) {
        super(view, to);
        this.view = view;
    }

    @Override
    protected void apply(View view, float v) {
        this.view.setTextSize(COMPLEX_UNIT_PX , ContextUtils.spToPx(v, view.getContext()));
    }

    @Override
    protected float getFrom(View view) {
        return ContextUtils.pxToSp(this.view.getTextSize(), view.getContext());
    }
}

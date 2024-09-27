package com.sdidsa.bondcheck.abs.animation.view;

import android.view.View;

import com.sdidsa.bondcheck.abs.animation.base.ViewAnimation;

public class WidthAnimation extends ViewAnimation {

    public WidthAnimation(View view, float to) {
        super(view, to);
    }

    @Override
    protected float getFrom(View view) {
        return view.getWidth();
    }

    @Override
    protected void apply(View view, float v) {
        view.getLayoutParams().width = (int) v;
        view.setLayoutParams(view.getLayoutParams());
    }
}

package com.sdidsa.bondcheck.abs.animation.view.scale;

import android.view.View;

import com.sdidsa.bondcheck.abs.animation.base.ViewAnimation;

public class ScaleXYAnimation extends ViewAnimation {
    public ScaleXYAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    public ScaleXYAnimation(View view, float to) {
        super(view, to);
    }

    public ScaleXYAnimation(View view, float from, float to) {
        super(view, from, to);
    }

    @Override
    protected float getFrom(View view) {
        return view.getScaleY();
    }

    @Override
    protected void apply(View view, float v) {
        view.setScaleY(v);
        view.setScaleX(v);
    }
}

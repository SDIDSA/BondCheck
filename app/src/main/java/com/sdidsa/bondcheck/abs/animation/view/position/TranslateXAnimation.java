package com.sdidsa.bondcheck.abs.animation.view.position;

import android.view.View;

import com.sdidsa.bondcheck.abs.animation.base.ViewAnimation;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class TranslateXAnimation extends ViewAnimation {

    private float from = -1, to = -1;

    public TranslateXAnimation(View view, float to) {
        super(view, to);
    }

    public TranslateXAnimation(View view,float from, float to) {
        super(view, from, to);
        setFrom(from);
    }

    public TranslateXAnimation(long duration, View view, float to) {
        super(duration, view, to);
    }

    @Override
    public void init() {
        super.init();

        if(from == -1) from = getFrom();
        if(to == -1) to = getTo();

        int d = ContextUtils.getLocaleDirection(getView());
        setTo(to * d);
        setFrom(from * d);
    }

    @Override
    protected float getFrom(View view) {
        return view.getTranslationX() * ContextUtils.getLocaleDirection(getView());
    }

    @Override
    protected void apply(View view, float v) {
        view.setTranslationX(v);
    }
}

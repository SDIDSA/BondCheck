package com.sdidsa.bondcheck.abs.animation.easing;

public class Linear implements Interpolator {
    @Override
    public float getInterpolation(float v) {
        return v;
    }
}

package com.sdidsa.bondcheck.abs.components.layout.abs;

import android.content.Context;

import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class CornerUtils {
    public static float[] cornerRadius(Context owner,
                                       float topLeft,
                                       float topRight,
                                       float bottomRight,
                                       float bottomLeft) {
        int topLeftPx = ContextUtils.dipToPx(topLeft, owner);
        int topRightPx = ContextUtils.dipToPx(topRight, owner);
        int bottomRightPx = ContextUtils.dipToPx(bottomRight, owner);
        int bottomLeftPx = ContextUtils.dipToPx(bottomLeft, owner);
        return new float[]{
                topLeftPx, topLeftPx,
                topRightPx, topRightPx,
                bottomRightPx, bottomRightPx,
                bottomLeftPx, bottomLeftPx
        };
    }

    public static float[] cornerRadius(Context owner, float radius) {
        return cornerRadius(owner, radius, radius, radius, radius);
    }

    public static float[] cornerTopRadius(Context owner, float radius) {
        return cornerRadius(owner, radius, radius, 0, 0);
    }

    public static float[] cornerBottomRadius(Context owner, float radius) {
        return cornerRadius(owner, 0, 0, radius, radius);
    }

    public static float[] cornerRightRadius(Context owner, float radius) {
        return cornerRadius(owner, 0, radius, radius, 0);
    }

    public static float[] cornerLeftRadius(Context owner, float radius) {
        return cornerRadius(owner, radius, 0, 0, radius);
    }

    public static float[] cornerTopLeftRadius(Context owner, float radius) {
        return cornerRadius(owner, radius, 0, 0, 0);
    }

    public static float[] cornerTopRightRadius(Context owner, float radius) {
        return cornerRadius(owner, 0, radius, 0, 0);
    }

    public static float[] cornerBottomRightRadius(Context owner, float radius) {
        return cornerRadius(owner, 0, 0, radius, 0);
    }

    public static float[] cornerBottomLeftRadius(Context owner, float radius) {
        return cornerRadius(owner, 0, 0, 0, radius);
    }
}

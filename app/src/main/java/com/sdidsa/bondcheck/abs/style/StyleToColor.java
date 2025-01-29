package com.sdidsa.bondcheck.abs.style;

import androidx.annotation.ColorInt;

import com.sdidsa.bondcheck.abs.App;

public interface StyleToColor {
    @ColorInt int get(Style style);

    static StyleToColor adjustAlpha(StyleToColor color, float factor) {
        return style -> App.adjustAlpha(color.get(style), factor);
    }
}

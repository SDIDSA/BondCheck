package com.sdidsa.bondcheck.abs.components.controls.scratches;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;

import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class Separator extends View {
    private final Context owner;

    private Orientation orientation;
    private final float margin;

    private int thickness = 1;

    public Separator(Context owner) {
        this(owner, Orientation.HORIZONTAL, 0);
    }

    public Separator(Context owner, Orientation orientation, float margin) {
        super(owner);
        this.owner = owner;

        setAlpha(.4f);

        this.orientation = orientation;
            this.margin = margin;
        apply();
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        apply();
    }

    public Separator setThickness(int thickness) {
        this.thickness = thickness;
        apply();
        return this;
    }

    private void apply() {
        boolean isVert = orientation == Orientation.VERTICAL;
        boolean hor = !isVert;

        int thickInt = ContextUtils.dipToPx(thickness, owner);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                hor ? LinearLayout.LayoutParams.WRAP_CONTENT : thickInt,
                hor ? thickInt : LinearLayout.LayoutParams.WRAP_CONTENT);


        int marginPx = ContextUtils.dipToPx(margin, owner);
        params.leftMargin = isVert ? 0 : marginPx;
        params.rightMargin = params.leftMargin;
        params.topMargin = isVert ? marginPx : 0;
        params.bottomMargin = params.topMargin;

        params.weight = 1;
        setLayoutParams(params);
    }

    public void setColor(@ColorInt int color) {
        setBackgroundColor(color);
    }


}

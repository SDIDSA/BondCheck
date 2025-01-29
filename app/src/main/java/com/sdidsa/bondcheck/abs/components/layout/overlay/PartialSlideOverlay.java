package com.sdidsa.bondcheck.abs.components.layout.overlay;

import android.content.Context;
import android.view.Gravity;

import androidx.core.graphics.Insets;

import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;

public abstract class PartialSlideOverlay extends SlideOverlay {
    private final float margin = 15;

    public PartialSlideOverlay(Context owner, double heightFactor) {
        super(owner);
        list.setCornerRadius(20);
        setHeightFactor(heightFactor);
    }

    public PartialSlideOverlay(Context owner, int height) {
        super(owner);
        list.setCornerRadius(20);
        setHeight(height);
    }

    @Override
    public void setHeightFactor(double heightFactor) {
        super.setHeightFactor(heightFactor);
    }

    private Insets insets;
    @Override
    public void applySystemInsets(Insets insets) {
        this.insets = insets;
        LayoutParams params = (LayoutParams) list.getLayoutParams();
        int marg = SizeUtils.dipToPx(margin, owner);

        params.gravity = Gravity.BOTTOM;
        params.bottomMargin = insets.bottom + marg;
        params.rightMargin = marg;
        params.leftMargin = marg;
        list.setLayoutParams(params);
    }

    @Override
    protected void setHeight(int height) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height);
        int marg = SizeUtils.dipToPx(margin, owner);
        params.gravity = Gravity.BOTTOM;
        params.bottomMargin = (insets == null ? 0 : insets.bottom) + marg;
        params.rightMargin = marg;
        params.leftMargin = marg;
        list.setLayoutParams(params);
    }
}

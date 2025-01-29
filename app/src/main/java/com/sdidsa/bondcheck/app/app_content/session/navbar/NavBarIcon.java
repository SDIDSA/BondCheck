package com.sdidsa.bondcheck.app.app_content.session.navbar;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.components.controls.image.ColorIcon;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;

public class NavBarIcon extends StackPane implements Styleable {
    private final ColorIcon fill;
    private final ColorIcon outline;

    public NavBarIcon(Context owner) {
        this(owner, -1, -1, 24);
    }

    public NavBarIcon(Context owner, @DrawableRes int fillRes, @DrawableRes int outlineRes,
                      float size) {
        super(owner);

        fill = new ColorIcon(owner, fillRes, size);
        outline = new ColorIcon(owner, outlineRes, size);

        addCentered(outline);
        addCentered(fill);

        fill.setAlpha(0f);

        setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        setSize(size);

        applyStyle(StyleUtils.getStyle(owner));
    }

    @Override
    public void setPadding(float padding) {
        fill.setPadding(padding);
        outline.setPadding(padding);
    }

    public Animation fadeToSelected(boolean selected) {
        return fadeTo(selected ? fill : outline);
    }

    private Animation fadeTo(ColorIcon to) {
        return fadeFromTo(to == fill ? outline : fill, to);
    }

    private Animation fadeFromFillToOutline;
    private Animation fadeFromOutlineToFill;
    private Animation fadeFromTo(ColorIcon from, ColorIcon to) {
        if(from == fill) {
            if(fadeFromFillToOutline == null) {
                fadeFromFillToOutline = createFadeFromTo(from, to);
            }
            return fadeFromFillToOutline;
        }else {
            if(fadeFromOutlineToFill == null) {
                fadeFromOutlineToFill = createFadeFromTo(from, to);
            }
            return fadeFromOutlineToFill;
        }
    }

    private Animation createFadeFromTo(ColorIcon from, ColorIcon to) {
        return new ParallelAnimation(300)
                .addAnimation(Animation.fadeOut(from))
                .addAnimation(Animation.fadeIn(to))
                .setInterpolator(Interpolator.OVERSHOOT);
    }

    public void setHeight(float height) {
        getLayoutParams().height = Math.max(SizeUtils.dipToPx(height, owner), 0);
        setLayoutParams(getLayoutParams());
    }

    public void setWidth(float width) {
        getLayoutParams().width = Math.max(0, SizeUtils.dipToPx(width, owner));
        setLayoutParams(getLayoutParams());
    }

    public void setSize(float size) {
        setWidth(size);
        setHeight(size);
    }

    @Override
    public void applyStyle(Style style) {
        fill.setFill(style.getTextNormal());
        outline.setFill(style.getTextSecondary());
    }

}

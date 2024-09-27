package com.sdidsa.bondcheck.abs.components.controls.input;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateXAnimation;
import com.sdidsa.bondcheck.abs.animation.view.position.TranslateYAnimation;
import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.data.property.Property;

import java.util.function.Consumer;

public class Switch extends StackPane implements Styleable {
    private static final Interpolator interpolator = Interpolator.OVERSHOOT;

    private final GradientDrawable trackBack;
    private final GradientDrawable thumbBack;
    private final StackPane thumb;

    private final float sizeDp;
    private final int sizePx;

    private Consumer<Boolean> onChange;
    private Consumer<Boolean> postChange;

    public Switch(Context owner) {
        this(owner, 24);
    }

    public Switch(Context owner, float sizeDp) {
        super(owner);
        this.sizeDp = sizeDp;
        sizePx = ContextUtils.dipToPx(sizeDp, owner);

        setLayoutParams(new LinearLayout.LayoutParams((int) (sizePx * 2.5f), sizePx));

        trackBack = new GradientDrawable();
        trackBack.setCornerRadius(sizePx / 4f);
        View track = new View(owner);
        track.setBackground(trackBack);
        track.setLayoutParams(new LayoutParams((int) (sizePx * 1.5f), sizePx / 2));
        ContextUtils.setMarginHorizontal(track, owner, sizeDp / 2);
        ContextUtils.alignInFrame(track, Alignment.CENTER);

        thumbBack = new GradientDrawable();
        thumbBack.setCornerRadius(sizePx / 2f);
        thumb = new StackPane(owner);
        thumb.setBackground(thumbBack);
        thumb.setElevation(sizeDp / 3);
        thumb.setLayoutParams(new LayoutParams(sizePx, sizePx));

        addView(track);
        addView(thumb);

        setOnClickListener((v) -> toggle());

        applyStyle(ContextUtils.getStyle(owner));
    }

    private ColoredIcon oldIcon;
    public void setIcon(@DrawableRes int res) {
        setIcon(res, 0);
    }

    public void setIcon(@DrawableRes int res, float padding) {
        ColoredIcon icon = new ColoredIcon(owner, Style.BACK_PRI, res);
        icon.setSize(sizeDp);
        icon.setAlpha(0f);
        icon.setTranslationY(sizePx);
        icon.setPadding(padding);

        ParallelAnimation set = new ParallelAnimation(400)
                .addAnimation(new TranslateYAnimation(icon, 0))
                .addAnimation(new AlphaAnimation(icon, 1))
                .setInterpolator(interpolator);

        if(oldIcon != null) {
            final ColoredIcon old = oldIcon;
            set.addAnimation(new TranslateYAnimation(old, -sizePx))
                    .addAnimation(new AlphaAnimation(old, 0))
                    .setOnFinished(() -> thumb.removeView(old));
        }
        thumb.addView(icon);

        oldIcon = icon;
        set.start();
    }

    private boolean state = false;
    public void toggle() {
        if(state) {
            disable();
        }else {
            enable();
        }
    }

    public void enable() {
        state = true;

        if(onChange != null) onChange.accept(true);

        new TranslateXAnimation(400, thumb, sizePx * 1.5f)
                .setInterpolator(interpolator)
                .setOnFinished(() -> {
                    if(postChange != null) postChange.accept(true);
                })
                .start();
    }

    public void disable() {
        state = false;

        if(onChange != null) onChange.accept(false);

        new TranslateXAnimation(400, thumb, 0)
                .setInterpolator(interpolator)
                .setOnFinished(() -> {
                    if(postChange != null) postChange.accept(false);
                })
                .start();
    }

    public void setOnChange(Consumer<Boolean> onChange) {
        this.onChange = onChange;
    }

    public void setPostChange(Consumer<Boolean> postChange) {
        this.postChange = postChange;
    }

    @Override
    public void applyStyle(Style style) {
        trackBack.setColor(style.getTextMuted());
        thumbBack.setColor(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}

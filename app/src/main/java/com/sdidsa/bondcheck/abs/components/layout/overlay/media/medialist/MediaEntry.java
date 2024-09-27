package com.sdidsa.bondcheck.abs.components.layout.overlay.media.medialist;

import android.content.Context;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.padding.UnifiedPaddingAnimation;
import com.sdidsa.bondcheck.abs.components.controls.image.Image;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.data.media.Media;
import com.sdidsa.bondcheck.abs.data.property.Property;

public class MediaEntry extends Image implements Styleable {
    private final int size;
    private final Animation press;
    private final Animation release;

    public MediaEntry(Context owner) {
        this(owner, 256);
    }

    public MediaEntry(Context owner, int size) {
        super(owner);
        setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        this.size = size;
        setSize(ContextUtils.pxToDip(size, owner));

        press = new ParallelAnimation(300)
                .addAnimations(new UnifiedPaddingAnimation(view,
                        ContextUtils.dipToPx(10, owner)))
                .addAnimations(new AlphaAnimation(view, .8f))
                .setInterpolator(Interpolator.EASE_OUT);
        release = new ParallelAnimation(300)
                .addAnimations(new UnifiedPaddingAnimation(view, 0))
                .addAnimations(new AlphaAnimation(view, 1f))
                .setInterpolator(Interpolator.EASE_OUT);

        setImagePadding(1);

        view.setScaleType(ImageView.ScaleType.CENTER_CROP);

        setPadding(0, 0, 0, 0);

        applyStyle(ContextUtils.getStyle(owner));
    }

    public void select() {
        release.stop();
        press.start();
    }

    public void deselect() {
        press.stop();
        release.start();
    }

    public void load(Media media) {
        setImageResource(R.drawable.empty);
        media.getThumbnail(
                getOwner(),
                size,
                this::setImageBitmap,
                () -> setImageResource(R.drawable.problem)
        );
    }

    @Override
    public void applyStyle(Style style) {
        setBackgroundColor(style.getBackgroundPrimary());
        view.setBackgroundColor(style.getAccent());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
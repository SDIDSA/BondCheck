package com.sdidsa.bondcheck.abs.components.controls.image;

import android.content.Context;
import android.graphics.Bitmap;

import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredSpinLoading;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class NetImage extends Image implements Styleable {
    private final ColoredSpinLoading loading;
    private final StyleToColor fill;

    public NetImage(Context owner) {
        this(owner, null);
    }

    public NetImage(Context owner, StyleToColor fill) {
        super(owner);

        this.fill = fill;

        loading = new ColoredSpinLoading(owner, Style.TEXT_SEC);

        applyStyle(ContextUtils.getStyle(owner));
    }

    public void startLoading() {
        view.setAlpha(.2f);
        removeView(loading);
        addCentered(loading);
        loading.startLoading();
    }

    public void stopLoading() {
        view.setAlpha(1f);
        removeView(loading);
        loading.stopLoading();
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        stopLoading();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        stopLoading();
    }

    public void setImageUrl(String url) {
        startLoading();
        ImageProxy.getImage(owner, url, this::setImageBitmap);
    }

    public void setImageThumbUrl(String url, int size) {
        startLoading();
        ImageProxy.getImageThumb(owner, url, size, this::setImageBitmap);
    }

    @Override
    public void setSize(float size) {
        super.setSize(size);

        loading.setSize(size / 2);
    }

    @Override
    public void applyStyle(Style style) {
        if(fill == null) return;
        setBackgroundColor(fill.get(style));
    }

    @Override
    public void applyStyle(Property<Style> style) {
        if(fill == null) return;
        Styleable.bindStyle(this, style);
    }
}

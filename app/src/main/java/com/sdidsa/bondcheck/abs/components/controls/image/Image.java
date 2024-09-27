package com.sdidsa.bondcheck.abs.components.controls.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;

import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.locale.Localized;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class Image extends StackPane implements Localized {
    protected final Context owner;
    private Runnable onClick;

    private final GradientDrawable fore;
    private final GradientDrawable backFill;

    private boolean autoMirror;
    protected final AppCompatImageView view;

    private float radius = 0;

    public Image(Context owner, @DrawableRes int res) {
        super(owner);
        this.owner = owner;

        view = new AppCompatImageView(owner);

        setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        addCentered(view);

        view.setLayoutParams(
                new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));

        fore = new GradientDrawable();
        backFill = new GradientDrawable();
        view.setForeground(fore);
        view.setClipToOutline(true);
        view.setOutlineProvider(new OutlineProvider());

        setCornerRadius(0);

        setClipToOutline(false);
        setClipChildren(false);
        setClipToPadding(false);

        setBackground(backFill);

        if(res != -1) setImageResource(res);

        applyLocale(ContextUtils.getLocale(owner));
    }

    public void setImageAlpha(float alpha) {
        view.setAlpha(alpha);
    }

    public void setImageResource(int resId) {
        if(resId != -1)
            view.setImageResource(resId);

        updateLayoutDirection();
    }

    public Image setAutoMirror(boolean autoMirror) {
        this.autoMirror = autoMirror;
        updateLayoutDirection();
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Image> T setImagePadding(float val) {
        LayoutParams p = (LayoutParams) view.getLayoutParams();
        int v = ContextUtils.dipToPx(val, owner);
        p.leftMargin = v;
        p.topMargin = v;
        p.rightMargin = v;
        p.bottomMargin = v;
        view.setLayoutParams(p);
        return (T) this;
    }

    public void setImageBitmap(Bitmap bitmap) {
        view.setImageBitmap(bitmap);
        updateLayoutDirection();
    }

    public void setImageDrawable(Drawable d) {
        view.setImageDrawable(d);
        updateLayoutDirection();
    }

    public Drawable getDrawable() {
        return view.getDrawable();
    }

    @Override
    public void setBackgroundColor(int color) {
        backFill.setColor(color);
    }

    public void setBorder(int color, float width) {
        backFill.setStroke(ContextUtils.dipToPx(width, owner), color);
    }

    public Image(Context owner) {
        this(owner, -1);
    }

    public void setCornerRadius(float radius) {
        this.radius = radius;
        fore.setCornerRadius(ContextUtils.dipToPx(radius, owner));
        backFill.setCornerRadius(ContextUtils.dipToPx(radius, owner));
    }

    public void setRadiusNoClip(float radius) {
        fore.setCornerRadius(ContextUtils.dipToPx(radius, owner));
        backFill.setCornerRadius(ContextUtils.dipToPx(radius, owner));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateLayoutDirection();
        }
    }

    private void updateLayoutDirection() {
        applyLocale(ContextUtils.getLocale(owner).get());
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
        setOnClickListener(e -> fire());
    }

    public void setHeight(float height) {
        getLayoutParams().height = Math.max(ContextUtils.dipToPx(height, owner), 0);
        requestLayout();
    }

    public void setWidth(float width) {
        getLayoutParams().width = Math.max(0, ContextUtils.dipToPx(width, owner));
        requestLayout();
    }

    private float size;
    public void setSize(float size) {
        this.size = size;
        setWidth(size);
        setHeight(size);
    }

    public float getSize() {
        return size;
    }

    public void fire() {
        if (onClick != null)
            onClick.run();
    }

    public Context getOwner() {
        return owner;
    }

    @Override
    public void applyLocale(Locale locale) {
        view.setScaleX(autoMirror && locale.isRtl() ? -1 : 1);
    }

    @Override
    public void applyLocale(Property<Locale> locale) {
        Localized.bindLocale(this, locale);
    }

    private class OutlineProvider extends ViewOutlineProvider {

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(new Rect(0,0,
                    view.getWidth(), view.getHeight()), ContextUtils.dipToPx(radius, owner));
        }

    }
}

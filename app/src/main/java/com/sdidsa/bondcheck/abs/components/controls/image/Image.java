package com.sdidsa.bondcheck.abs.components.controls.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.abs.components.layout.abs.CornerUtils;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.locale.Localized;
import com.sdidsa.bondcheck.abs.utils.view.LocaleUtils;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;

public class Image extends StackPane implements Localized {
    protected final Context owner;
    private Runnable onClick;

    private final GradientDrawable backFill;

    private boolean autoMirror;
    protected final ImageView view;

    public Image(Context owner, @DrawableRes int res) {
        super(owner);
        this.owner = owner;

        view = new ImageView(owner);

        setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        addCentered(view);

        view.setLayoutParams(
                new LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT));

        backFill = new GradientDrawable();
        view.setClipToOutline(true);
        view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);

        setCornerRadius(0);

        setClipToOutline(false);
        setClipChildren(false);
        setClipToPadding(false);

        setBackground(backFill);

        if(res != -1) setImageResource(res);
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
        int v = SizeUtils.dipToPx(val, owner);
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
        backFill.setStroke(SizeUtils.dipToPx(width, owner), color);
    }

    public Image(Context owner) {
        this(owner, -1);
    }

    public void setCornerRadius(float radius) {
        setCornerRadius(CornerUtils.cornerRadius(owner, radius));
    }

    public void setCornerRadius(float[] radius) {
        view.setCornerRadii(radius);
        backFill.setCornerRadii(radius);
    }

    public void setRadiusNoClip(float radius) {
        backFill.setCornerRadius(SizeUtils.dipToPx(radius, owner));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateLayoutDirection();
        }
    }

    private void updateLayoutDirection() {
        applyLocale(LocaleUtils.getLocale(owner));
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
        setOnClickListener(e -> fire());
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

    public void setSize(float width, float height) {
        setWidth(width);
        setHeight(height);
    }

    protected float viewAlpha = 1.0f;
    public void setViewAlpha(float alpha) {
        viewAlpha = alpha;
        view.setAlpha(alpha);
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

}

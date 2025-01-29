package com.sdidsa.bondcheck.abs.components.controls.image;

import android.content.Context;
import android.graphics.Bitmap;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.controls.scratches.loading.ColoredSpinLoading;
import com.sdidsa.bondcheck.abs.data.observable.ChangeListener;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.StyleToColor;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.SizeUtils;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;
import com.sdidsa.bondcheck.models.Gender;
import com.sdidsa.bondcheck.models.responses.UserResponse;

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

        applyStyle(StyleUtils.getStyle(owner));
    }

    public void startLoading() {
        view.setAlpha(.2f);
        removeView(loading);
        addCentered(loading);
        loading.startLoading();
    }

    public void stopLoading() {
        view.setAlpha(viewAlpha);
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

    private String url;

    public void setImageUrl(String url) {
        this.url = url;
        startLoading();
        ImageProxy.getImage(owner, url, this::setImageBitmap);
    }

    public void setImageThumbUrl(String url, float size) {
        this.url = url;
        startLoading();
        setSize(size);
        ImageProxy.getImageThumb(owner, url,
                SizeUtils.dipToPx(size, owner),
                this::setImageBitmap);
    }

    public void setImageThumbUrl(String url, float width, float height) {
        this.url = url;
        startLoading();
        setSize(width, height);
        ImageProxy.getImageThumb(owner, url,
                SizeUtils.dipToPx(width, owner),
                SizeUtils.dipToPx(height, owner),
                this::setImageBitmap);
    }

    public String getUrl() {
        return url;
    }

    private UserResponse user;

    private ChangeListener<String> genderListener;
    private ChangeListener<String> avatarListener;

    public void bindToUser(UserResponse user) {
        unbind();
        this.user = user;
        avatarListener = (ov, nv) -> {
            if (nv == null) {
                if (user.getGender() != null) {
                    if (user.genderValue() == Gender.Female) {
                        setImageResource(R.drawable.avatar_female);
                    } else {
                        setImageResource(R.drawable.avatar_male);
                    }
                }
            } else {
                Platform.runLater(() ->
                        ImageProxy.getImageThumb(owner, nv, getLayoutParams().width,
                                this::setImageBitmap));
            }
        };

        genderListener = (ov, nv) -> {
            if (nv != null && user.getAvatar() == null) {
                if (user.genderValue() == Gender.Female) {
                    setImageResource(R.drawable.avatar_female);
                } else {
                    setImageResource(R.drawable.avatar_male);
                }
            }
        };

        user.avatar().addListener(avatarListener);
        user.gender().addListener(genderListener);
    }

    public void unbind() {
        if(user == null) return;
        if(genderListener != null) {
            user.gender().removeListener(genderListener);
        }
        if(avatarListener != null) {
            user.avatar().removeListener(avatarListener);
        }
    }

    @Override
    public void setSize(float size) {
        super.setSize(size);

        loading.setSize(Math.min(size / 2, 72));
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);

        loading.setSize(Math.min((width + height) / 4, 72));
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

package com.sdidsa.bondcheck.abs.components.layout.linear;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.core.content.res.ResourcesCompat;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.Gradient;
import com.sdidsa.bondcheck.abs.components.layout.GradientStop;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.data.property.Property;

import java.util.List;

public class LinearBox extends LinearLayout {
    private final Property<Float> spacing;
    protected final Context owner;

    private final GradientDrawable background;

    private final GradientDrawable foreground;

    public LinearBox(Context owner) {
        super(owner);
        this.owner = owner;

        background = new GradientDrawable();
        foreground = new GradientDrawable();
        setBackground(background);
        setForeground(foreground);

        spacing = new Property<>(0f);
        spacing.addListener((ov, nv) -> applySpacing(nv));

        setClipChildren(false);
        setClipToPadding(false);
        setClipToOutline(false);
    }

    private void applySpacing(double spacing) {
        int rs = ContextUtils.dipToPx(spacing, owner);
        GradientDrawable divider = (GradientDrawable) ResourcesCompat.getDrawable(
                owner.getResources(), R.drawable.divider_shape,null);
        if(divider==null) {
            return;
        }
        if(this instanceof HBox) {
            divider.setSize(rs, 1);
        } else {
            divider.setSize(1, rs);
        }
        setDividerPadding(0);
        setDividerDrawable(divider);
        setShowDividers(SHOW_DIVIDER_MIDDLE);
    }

    public void setSpacing(float spacing) {
        this.spacing.set(spacing);

        if(this instanceof HBox) {
            ErrorHandler.handle(new RuntimeException("you shouldn't set the spacing on " + getClass().getSimpleName() +
                    " (it won't work in RTL)"), "setting spacing of linear box");
        }
    }

    public void setPadding(float padding) {
        ContextUtils.setPaddingUnified(this, padding, owner);
    }

    public void setBackground(int color) {
        background.setColor(color);
    }

    public void setBackgroundGradient(Gradient gradient) {
        List<GradientStop> stops = gradient.getStops();
        int[] colors = new int[stops.size()];
        float[] positions = new float[stops.size()];

        for (int i = 0; i < stops.size(); i++) {
            GradientStop stop = stops.get(i);
            colors[i] = stop.color();
            positions[i] = stop.position();
        }

        background.setOrientation(gradient.getOrientation());
        background.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        background.setColors(colors, positions);
    }

    public void setBorder(@ColorInt int color, float width) {
        background.setStroke(ContextUtils.dipToPx(width, owner), color);
    }

    public void setCornerRadius(float radius) {
        background.setCornerRadius(ContextUtils.dipToPx(radius, owner));
        foreground.setCornerRadius(ContextUtils.dipToPx(radius, owner));
    }

    public void setCornerRadiusBottom(float radius) {
        int val = ContextUtils.dipToPx(radius, owner);
        background.setCornerRadii(new float[]{
                0, 0,
                0, 0,
                val, val,
                val, val
        });
        foreground.setCornerRadii(new float[]{
                0, 0,
                0, 0,
                val, val,
                val, val
        });
    }

    public void setAlignment(Alignment alignment) {
        setGravity(alignment.getGravity());
    }

    public Context getOwner() {
        return owner;
    }



    @Override
    public void addView(View child) {
        if(Thread.currentThread() != Looper.getMainLooper().getThread()
                && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"),
                    "adding view to " + getClass().getSimpleName());
        if(child.getParent() == this) {
            return;
        }
        if(child.getParent() != null) {
            ((ViewGroup)child.getParent()).removeView(child);
        }
        try {
            super.addView(child);
        } catch (Exception x) {
            ErrorHandler.handle(x, "adding view to " + getClass().getSimpleName());
        }
    }

    public void addViews(View... views) {
        for(View view:views) {
            addView(view);
        }
    }

    @Override
    public void removeView(View view) {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"),
                    "removing view from " + getClass().getSimpleName());
        try {
            super.removeView(view);
        } catch (Exception x) {
            ErrorHandler.handle(x, "removing view from " + getClass().getSimpleName());
        }
    }

    @Override
    public void removeAllViews() {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"),
                    "removing views from " + getClass().getSimpleName());
        try {
            super.removeAllViews();
        } catch (Exception x) {
            ErrorHandler.handle(x, "removing views from " + getClass().getSimpleName());
        }
    }

    @Override
    public void addView(View child, int index) {
        if(Thread.currentThread() != Looper.getMainLooper().getThread() && isAttachedToWindow())
            ErrorHandler.handle(new RuntimeException("modifying ui from the wrong thread"),
                    "adding view to " + getClass().getSimpleName());
        try {
            super.addView(child, index);
        } catch (Exception x) {
            ErrorHandler.handle(x, "adding view from " + getClass().getSimpleName());
        }
    }
}

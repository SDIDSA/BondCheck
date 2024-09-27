package com.sdidsa.bondcheck.app.app_content.session.content.settings.shared;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.animation.base.Animation;
import com.sdidsa.bondcheck.abs.animation.combine.ParallelAnimation;
import com.sdidsa.bondcheck.abs.animation.easing.Interpolator;
import com.sdidsa.bondcheck.abs.animation.view.AlphaAnimation;
import com.sdidsa.bondcheck.abs.animation.view.ElevationAnimation;
import com.sdidsa.bondcheck.abs.animation.view.LinearHeightAnimation;
import com.sdidsa.bondcheck.abs.animation.view.RotateAnimation;
import com.sdidsa.bondcheck.abs.animation.view.scale.ScaleXYAnimation;
import com.sdidsa.bondcheck.abs.components.controls.image.ColorIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;

import java.util.ArrayList;

public class SettingsGroup extends VBox implements Styleable {
    public static final float SCALE_DOWN = .935f;
    private final Settings parent;

    private final Label title;
    private final ColorIcon arrow;
    private final ColorIcon icon;
    private final VBox settingsBox;
    private final ArrayList<Setting> settings;
    private final HBox top;

    public SettingsGroup(Context owner) {
        this(owner, null, "Group Title", -1);
    }

    public SettingsGroup(Context owner, Settings parent, String key, @DrawableRes int iconRes) {
        super(owner);
        this.parent = parent;

        setCornerRadius(20);
        setLayoutParams(new LayoutParams(-1, -2));

        settings = new ArrayList<>();

        top = new HBox(owner);
        top.setGravity(Gravity.CENTER);

        icon = new ColorIcon(owner, iconRes);
        icon.setSize(24);
        ContextUtils.setMarginRight(icon, owner, 15);

        title = new Label(owner, key);
        title.setFont(new Font(18, FontWeight.MEDIUM));

        arrow = new ColorIcon(owner, R.drawable.right_arrow);
        arrow.setSize(18);
        arrow.setRotation(90);

        settingsBox = new VBox(owner);
        settingsBox.setPadding(10);
        settingsBox.setLayoutParams(new LayoutParams(-1, 0));
        settingsBox.setClipToOutline(true);

        top.setAlpha(.7f);
        setScaleX(SCALE_DOWN);
        setScaleY(SCALE_DOWN);

        top.addView(icon);
        top.addView(title);
        top.setPadding(20);
        top.addView(ContextUtils.spacer(owner, Orientation.HORIZONTAL));
        top.addView(arrow);

        setOnClickListener(e -> open());

        addView(top);
        addView(settingsBox);
        settingsBox.setVisibility(GONE);

        applyStyle(ContextUtils.getStyle(owner));
    }

    private int settingsHeight() {
        return settingsBox.getPaddingTop() + settingsBox.getPaddingBottom()
                + settings.stream().mapToInt(View::getHeight).sum();
    }

    public void addSetting(Setting setting) {
        settings.add(setting);
        settingsBox.addView(setting);
    }

    private boolean open = false;

    public boolean isOpen() {
        return parent.getOpen() == this;
    }

    public void openIfClosed() {
        if(!isOpen()) {
            open();
        }
    }

    public void open() {
        open(null);
    }

    private Animation openAnim;
    public void open(Runnable prePost) {
        Runnable post = () -> Platform.runLater(() -> {
            if(prePost != null) prePost.run();
            parent.scrollTo(this);
        });

        SettingsGroup old;
        if(parent.getOpen() != null) {
            if(parent.getOpen() == this) {
                close();
                return;
            } else {
                old = parent.getOpen();
            }
        }else {
            old = null;
        }

        open = true;
        if(close != null && close.isRunning()) {
            close.stop();
        }
        settingsBox.setVisibility(VISIBLE);
        if(openAnim == null) {
            Platform.waitWhile(() -> !settingsBox.isLaidOut(), () -> {
                openAnim = new ParallelAnimation(400)
                        .addAnimation(new RotateAnimation(arrow, 270))
                        .addAnimation(new LinearHeightAnimation(settingsBox, 0)
                                .setLateToInt(this::settingsHeight))
                        .addAnimation(new AlphaAnimation(top, 1f))
                        .addAnimation(new ScaleXYAnimation(this, 1))
                        .addAnimation(new ElevationAnimation(this,
                                ContextUtils.dipToPx(20, owner)))
                        .setInterpolator(Interpolator.OVERSHOOT);

                if(old != null) old.close();
                openAnim.setOnFinished(post);
                openAnim.start();
                parent.setOpen(this);
            });
        }else {
            if(old != null) old.close();
            openAnim.setOnFinished(post);
            openAnim.start();
            parent.setOpen(this);
        }

    }

    public String getKey() {
        return title.getKey();
    }

    private Animation close;
    public void close() {
        if(!open) return;
        open = false;

        if(close == null) {
            close = new ParallelAnimation(400)
                    .addAnimation(new RotateAnimation(arrow, 90))
                    .addAnimation(new LinearHeightAnimation(settingsBox, 0))
                    .addAnimation(new ElevationAnimation(this, 0))
                    .addAnimation(new AlphaAnimation(top, .7f))
                    .addAnimation(new ScaleXYAnimation(this, SCALE_DOWN))
                    .setInterpolator(Interpolator.OVERSHOOT)
                    .setOnFinished(() -> settingsBox.setVisibility(GONE));
        }
        if(openAnim != null && openAnim.isRunning()) {
            openAnim.stop();
        }
        close.start();

        parent.setOpen(null);
    }

    public int calcY() {
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        return loc[1];
    }

    @Override
    public void applyStyle(Style style) {
        icon.setFill(style.getTextNormal());
        title.setFill(style.getTextNormal());
        arrow.setFill(style.getTextNormal());
        setBackground(style.getBackgroundSecondary());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
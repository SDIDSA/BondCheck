package com.sdidsa.bondcheck.app.app_content.session.content.settings.shared;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.sdidsa.bondcheck.abs.components.controls.button.Button;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class Setting extends Button implements Styleable {
    public Setting(Context owner) {
        this(owner, "Settings Item");
    }

    public Setting(Context owner, String key) {
        super(owner, key);
        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ContextUtils.dipToPx(56, owner)));
        setPadding(10);

        content.setGravity(Gravity.CENTER);

        setFont(new Font(16));

        applyStyle(ContextUtils.getStyle(owner));
    }

    @Override
    public void applyStyle(Style style) {
        setFill(style.getBackgroundSecondary());
        setTextFill(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}
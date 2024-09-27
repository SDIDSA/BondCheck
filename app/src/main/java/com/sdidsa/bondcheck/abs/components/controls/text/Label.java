package com.sdidsa.bondcheck.abs.components.controls.text;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;

import com.sdidsa.bondcheck.abs.components.layout.StackPane;

import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.abs.CornerUtils;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.locale.Localized;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.data.property.Property;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Label extends androidx.appcompat.widget.AppCompatTextView implements Localized {
    protected final Context owner;
    private String key;
    private final HashMap<Integer, String> params = new HashMap<>();

    private final GradientDrawable background;

    public Label(Context owner) {
        this(owner, "");
    }

    public Label(Context owner, String key) {
        super(owner);
        this.owner = owner;
        this.key = key;

        setFont(Font.DEFAULT);

        setLayoutParams(new StackPane.LayoutParams(StackPane.LayoutParams.WRAP_CONTENT, StackPane.LayoutParams.WRAP_CONTENT));
        applyLocale(ContextUtils.getLocale(owner));

        background = new GradientDrawable();
        setBackground(background);
    }

    public void setCornerRadius(float radius) {
        background.setCornerRadii(CornerUtils.cornerRadius(owner, radius));
    }

    public void setCornerRadius(float[] radius) {
        background.setCornerRadii(radius);
    }

    public void setPadding(float padding) {
        ContextUtils.setPaddingUnified(this, padding, owner);
    }

    @SuppressWarnings("unchecked")
    public <T extends Label> T setFont(Font font) {
        setTypeface(font.getFont());
        setTextSize(TypedValue.COMPLEX_UNIT_SP, font.getSize());
        return (T) this;
    }

    public void setKey(String key) {
        this.key = key;
        applyLocale(ContextUtils.getLocale(owner).get());
    }

    public void setKey(String key, String...params) {
        this.key = key;
        for(int i = 0; i < params.length; i++){
            this.params.put(i, params[i]);
        }
        applyLocale(ContextUtils.getLocale(owner).get());
    }

    public String getKey() {
        return key;
    }

    public void addParam(int i, String param) {
        params.put(i, param);
        applyLocale(ContextUtils.getLocale(owner).get());
    }

    public void setLineSpacing(float spacing) {
        setLineSpacing(ContextUtils.dipToPx(spacing, owner), 1);
    }

    public void centerText() {
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setGravity(Gravity.CENTER);
    }

    @Override
    public void applyLocale(Locale locale) {
        if (key != null && !key.isEmpty()) {
            AtomicReference<String> val = new AtomicReference<>(locale.get(key));
            params.forEach((index, param) ->
                    val.set(val.get().replace("{$" + index + "}", param)));
            setText(val.get());
        }
    }

    public void setBackground(int color) {
        background.setColor(color);
    }

    public void applyLocale(Property<Locale> locale) {
        Localized.bindLocale(this, locale);
    }

    public void setFill(int fill) {
        setTextColor(fill);
    }
}

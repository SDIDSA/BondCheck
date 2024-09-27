package com.sdidsa.bondcheck.abs.components.controls.input;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.Alignment;
import com.sdidsa.bondcheck.abs.components.layout.StackPane;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.locale.Locale;
import com.sdidsa.bondcheck.abs.locale.Localized;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.data.observable.Observable;
import com.sdidsa.bondcheck.abs.data.property.Property;

public class MinimalInputField extends StackPane implements Styleable, Localized {
    protected final EditText input;
    private final GradientDrawable background;
    private final Property<String> value;
    private final HBox preInput;

    private final String promptKey;

    public MinimalInputField(Context owner) {
        this(owner,"Input Prompt");
    }

    public MinimalInputField(Context owner, String promptKey) {
        super(owner);
        background = new GradientDrawable();
        this.promptKey = promptKey;
        setRadius(12);

        value = new Property<>("");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.weight = 1;

        input = new EditText(owner);
        input.setLayoutParams(params);
        input.setTypeface(Font.DEFAULT.getFont());
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        input.setShowSoftInputOnFocus(true);
        input.setBackground(null);
        input.setLines(1);
        input.setSingleLine(true);
        input.setEnabled(true);

        preInput = new HBox(owner);
        preInput.setAlignment(Alignment.TOP_LEFT);
        preInput.addView(input);
        ContextUtils.setPadding(preInput, 15, 5, 4, 4, owner);

        addView(preInput);

        InputUtils.bindToProperty(input, value);

        setBackground(background);

        applyStyle(ContextUtils.getStyle(owner));
        applyLocale(ContextUtils.getLocale(owner));
    }

    public EditText getInput() {
        return input;
    }

    public void setMultiline(int lines) {
        input.setMaxLines(lines);
        input.setSingleLine(lines == 1);
    }

    public void addPostInput(View view) {
        preInput.addView(view);
    }

    public void setFont(Font font) {
        input.setTypeface(font.getFont());
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, font.getSize());
    }

    public Observable<String> valueProperty() {
        return value;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        input.setText(value);
        input.setSelection(input.getText().length());
    }

    public void setRadius(float radius) {
        background.setCornerRadius(ContextUtils.dipToPx(radius, owner));
    }

    public void setBackgroundColor(int color) {
        background.setColor(color);
    }

    @Override
    public void setBackground(int color) {
        setBackgroundColor(color);
    }

    @Override
    public void applyStyle(Style style) {
        setBackgroundColor(style.getBackgroundSecondary());

        input.setTextColor(style.getTextNormal());
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void applyLocale(Locale locale) {
        input.setHint(locale.get(promptKey));

        input.setGravity(Gravity.CENTER_VERTICAL | (locale.isRtl() ? Gravity.RIGHT : Gravity.LEFT));
    }

    @Override
    public void applyLocale(Property<Locale> locale) {
        Localized.bindLocale(this, locale);
    }
}

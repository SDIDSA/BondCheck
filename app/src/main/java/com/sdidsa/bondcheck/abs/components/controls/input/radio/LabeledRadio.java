package com.sdidsa.bondcheck.abs.components.controls.input.radio;

import android.content.Context;
import android.text.method.TransformationMethod;
import android.view.Gravity;

import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.layout.linear.HBox;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class LabeledRadio extends HBox implements Styleable {

    private final Radio radio;
    private final Label label;

    public LabeledRadio(Context owner) {
        this(owner, "Radio Button text");
    }
    public LabeledRadio(Context owner, String text) {
        super(owner);
        setGravity(Gravity.CENTER);
        setPadding(7);

        radio = new Radio(owner, 22);
        label = new Label(owner, text);
        label.setFont(new Font(18));


        addView(label);
        addView(ContextUtils.spacer(owner, Orientation.HORIZONTAL));
        addView(radio);

        setOnClickListener(e -> setChecked(true));

        setDefaultFocusHighlightEnabled(false);

        applyStyle(ContextUtils.getStyle(owner));
    }

    public void setFont(Font font) {
        label.setFont(font);
    }

    public void setChecked(boolean checked) {
        radio.setChecked(checked);
    }

    public Radio getRadio() {
        return radio;
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public void applyStyle(Style style) {
        setOnFocusChangeListener((v, focused) ->
                label.setFill(
                        focused ?
                                style.getTextNormal() :
                                style.getTextSecondary()));
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }

    public void setTransformationMethod(TransformationMethod method) {
        label.setTransformationMethod(method);
    }

    public boolean isChecked() {
        return radio.isChecked();
    }
}

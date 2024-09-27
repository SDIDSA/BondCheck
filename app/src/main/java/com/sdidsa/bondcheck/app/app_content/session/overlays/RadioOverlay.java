package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;

import com.sdidsa.bondcheck.abs.components.controls.button.Button;
import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.input.radio.LabeledRadio;
import com.sdidsa.bondcheck.abs.components.controls.input.radio.Radio;
import com.sdidsa.bondcheck.abs.components.controls.input.radio.RadioGroup;
import com.sdidsa.bondcheck.abs.components.controls.text.Label;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.controls.text.transformationMethods.Capitalize;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.data.property.Property;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.data.ConcurrentArrayList;
import com.sdidsa.bondcheck.abs.style.Styleable;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

import java.util.function.Consumer;

public class RadioOverlay extends PartialSlideOverlay implements Styleable {
    protected final VBox root;
    private final Label text;
    private final ConcurrentArrayList<LabeledRadio> buttons;
    private final RadioGroup group;

    private Consumer<String> onSave;

    public RadioOverlay(Context owner) {
        this(owner, "Header text");
    }

    public RadioOverlay(Context owner, String header) {
        super(owner, -2);

        list.setPadding(20);
        list.setSpacing(30);
        list.setGravity(Gravity.TOP | Gravity.CENTER);

        text = new Label(owner, header);
        text.centerText();
        text.setFont(new Font(20, FontWeight.MEDIUM));
        text.setLineSpacing(10);

        buttons = new ConcurrentArrayList<>();
        group = new RadioGroup();

        root = new VBox(owner);
        root.setSpacing(10);
        root.setGravity(Gravity.TOP | Gravity.CENTER);

        list.addView(text);

        list.addView(root);

        addOnShowing(() -> applyStyle(ContextUtils.getStyle(owner).get()));

        Button save = new ColoredButton(owner, Style.ACCENT, s -> Color.WHITE, "save");
        save.setFont(new Font(18, FontWeight.MEDIUM));

        list.addView(save);

        save.setOnClick(() -> {
            if(onSave != null && group.selected().get() != null) {
                Radio selected = group.selected().get();
                if(selected.getParent() instanceof LabeledRadio labeled) {
                    onSave.accept(labeled.getLabel().getKey());
                }
            }
        });

        group.selected().addListener((ov, nv) -> applyStyle(ContextUtils.getStyle(owner).get()));

        applyStyle(ContextUtils.getStyle(owner));
    }

    public void setOnSave(Consumer<String> onSave) {
        this.onSave = onSave;
    }

    public void select(String value) {
        for(LabeledRadio lab : buttons) {
            if(lab.getLabel().getKey().equalsIgnoreCase(value)) {
                lab.setChecked(true);
                break;
            }
        }
    }

    public void addButton(String text) {
        LabeledRadio button = new LabeledRadio(owner, text);
        button.setFont(new Font(20));
        button.setCornerRadius(15);
        button.setPadding(15);
        button.setTransformationMethod(new Capitalize());
        root.addView(button);
        buttons.add(button);
        group.addRadio(button.getRadio());

        applyStyle(ContextUtils.getStyle(owner).get());
    }

    @Override
    public void applyStyle(Style style) {
        text.setFill(style.getTextNormal());

        for (LabeledRadio button : buttons) {
            button.setBackground(button.isChecked() ?
                    style.getBackgroundSecondary() : Color.TRANSPARENT);
        }
    }

    @Override
    public void applyStyle(Property<Style> style) {
        Styleable.bindStyle(this, style);
    }
}

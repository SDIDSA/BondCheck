package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;

import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.input.MinimalInputField;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.data.observable.Observable;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;

import java.util.function.Consumer;

public class InputSlideOverlay extends PartialSlideOverlay {
    protected final VBox root;

    private final MinimalInputField input;
    private final ColoredButton save;

    public InputSlideOverlay(Context owner) {
        this(owner, "Overlay header");
    }
    public InputSlideOverlay(Context owner, String header) {
        super(owner, -2);

        root = new VBox(owner);
        root.setSpacing(30);
        root.setPadding(20);
        root.setGravity(Gravity.TOP | Gravity.CENTER);

        ColoredLabel text = new ColoredLabel(owner, Style.TEXT_NORM, header);
        text.centerText();
        text.setFont(new Font(20, FontWeight.MEDIUM));
        text.setLineSpacing(10);

        input = new MinimalInputField(owner, header);
        input.setFont(new Font(18));

        save = new ColoredButton(owner, Style.ACCENT,
                s -> Color.WHITE, "save");
        save.setFont(new Font(18, FontWeight.MEDIUM));

        root.addView(text);
        root.addView(input);
        root.addView(save);

        list.addView(root);

        addOnShown(() -> {
            input.getInput().requestFocus();
            ContextUtils.showKeyboard(owner, input.getInput());
        });

        addOnHidden(() -> ContextUtils.hideKeyboard(owner));
    }

    public void setMultiLine(int lines) {
        input.setMultiline(lines);
    }

    public void setOnSave(Consumer<String> action) {
        save.setOnClick(() -> {
            try {
                action.accept(input.getValue().trim());
            } catch (Exception e) {
                ErrorHandler.handle(e, "handling input overlay result");
            }
        });
    }

    public Observable<String> valueProperty() {
        return input.valueProperty();
    }

    public void enableAction(boolean enabled) {
        save.setEnabled(enabled);
    }

    public void setValue(String val) {
        input.setValue(val);
    }

    public void startLoading() {
        save.startLoading();
    }

    public void stopLoading() {
        save.stopLoading();
    }
}

package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;

import com.sdidsa.bondcheck.abs.components.controls.button.ColoredButton;
import com.sdidsa.bondcheck.abs.components.controls.input.InputField;
import com.sdidsa.bondcheck.abs.components.controls.input.PasswordField;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.font.FontWeight;
import com.sdidsa.bondcheck.abs.components.layout.linear.VBox;
import com.sdidsa.bondcheck.abs.components.layout.overlay.PartialSlideOverlay;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;

import java.util.function.BiConsumer;

public class ChangePasswordOverlay extends PartialSlideOverlay {
    protected final VBox root;

    private final InputField current;
    private final InputField newPass;
    private final ColoredButton save;

    public ChangePasswordOverlay(Context owner) {
        super(owner, -2);

        root = new VBox(owner);
        root.setSpacing(20);
        root.setPadding(20);
        root.setGravity(Gravity.TOP | Gravity.CENTER);

        ColoredLabel text = new ColoredLabel(owner, Style.TEXT_NORM, "change_pass");
        text.centerText();
        text.setFont(new Font(20, FontWeight.MEDIUM));
        text.setLineSpacing(10);

        current = createField("current_pass");
        newPass = createField("new_pass");

        save = new ColoredButton(owner, Style.ACCENT,
                s -> Color.WHITE, "save");
        save.setFont(new Font(18, FontWeight.MEDIUM));

        root.addView(text);
        root.addView(current);
        root.addView(newPass);
        root.addView(save);

        list.addView(root);

        addOnShown(() -> {
            current.getInput().requestFocus();
            ContextUtils.showKeyboard(owner, current.getInput());
        });

        addOnShowing(() -> {
            enableAction(false);
            current.setValue("");
            newPass.setValue("");
        });

        Runnable check = () -> enableAction(
                !current.getValue().isBlank() && !newPass.getValue().isBlank() &&
                !newPass.getValue().trim().equals(current.getValue().trim()));

        current.valueProperty().addListener(check);
        newPass.valueProperty().addListener(check);

        addOnHidden(() -> ContextUtils.hideKeyboard(owner));
    }

    private PasswordField createField(String hint) {
        PasswordField res = new PasswordField(owner, hint);
        res.setFont(new Font(18));

        return res;
    }

    public void setOnSave(BiConsumer<String, String> action) {
        save.setOnClick(() -> {
            try {
                action.accept(current.getValue().trim(), newPass.getValue().trim());
            } catch (Exception e) {
                ErrorHandler.handle(e, "handling input overlay result");
            }
        });
    }

    public void enableAction(boolean enabled) {
        save.setEnabled(enabled);
    }

    public void startLoading() {
        save.startLoading();
    }

    public void stopLoading() {
        save.stopLoading();
    }
}

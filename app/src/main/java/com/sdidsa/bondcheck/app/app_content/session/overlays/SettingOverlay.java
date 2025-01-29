package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;
import com.sdidsa.bondcheck.abs.components.layout.overlay.MultipleOptionOverlay;
import com.sdidsa.bondcheck.abs.components.layout.overlay.OverlayOption;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.view.StyleUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingOverlay extends MultipleOptionOverlay {

    public SettingOverlay(Context owner) {
        this(owner, "Setting key", () -> "Value", (s) -> {});
    }

    public SettingOverlay(Context owner, String key, Supplier<String> get,
                          Consumer<String> set, OverlayOption...options) {
        super(owner, "set_" + key, s -> get.get().equalsIgnoreCase(s));

        for(OverlayOption option : options) {
            addButton(option, () -> {
                try {
                    set.accept(option.text());
                    Platform.runLater(() ->
                            applyStyle(StyleUtils.getStyle(owner)));
                } catch (Exception e) {
                    ErrorHandler.handle(e, "setting " + key + " to " + option);
                }
            });
        }
    }
}
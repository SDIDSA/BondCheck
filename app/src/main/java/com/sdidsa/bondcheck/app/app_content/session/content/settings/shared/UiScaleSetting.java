package com.sdidsa.bondcheck.app.app_content.session.content.settings.shared;

import android.content.Context;

import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.transformationMethods.Capitalize;
import com.sdidsa.bondcheck.abs.components.layout.overlay.OverlayOption;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.display.UiScaleOverlay;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UiScaleSetting extends Setting {

    private final String key;
    private final Supplier<String > get;
    private final Consumer<String> set;

    private final OverlayOption[] options;

    private UiScaleOverlay overlay;

    public UiScaleSetting(Context owner) {
        this(owner, "Multiple Choice", () -> "value", (v) -> {});
    }

    public UiScaleSetting(Context owner, String key, Supplier<String> get,
                          Consumer<String> set, OverlayOption... options) {
        super(owner, key);
        this.key = key;
        this.get = get;
        this.set = set;
        this.options = options;

        ColoredLabel value = new ColoredLabel(owner, Style.TEXT_NORM, get.get().toLowerCase());
        value.setTransformationMethod(new Capitalize());
        addPostLabel(ContextUtils.spacer(owner, Orientation.HORIZONTAL));
        addPostLabel(value);

        value.setFont(new Font(16));

        setOnClick(() -> {
            if(overlay == null) {
                overlay = setOverlay();
            }
            overlay.show();
        });
    }

    public UiScaleOverlay setOverlay() {
        return new UiScaleOverlay(owner, key, get, set, options);
    }
}

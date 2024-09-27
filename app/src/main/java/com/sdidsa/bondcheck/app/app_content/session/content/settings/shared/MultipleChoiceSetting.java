package com.sdidsa.bondcheck.app.app_content.session.content.settings.shared;

import android.content.Context;

import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.components.controls.text.ColoredLabel;
import com.sdidsa.bondcheck.abs.components.controls.text.font.Font;
import com.sdidsa.bondcheck.abs.components.controls.text.transformationMethods.Capitalize;
import com.sdidsa.bondcheck.abs.components.layout.overlay.OverlayOption;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.overlays.SettingOverlay;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MultipleChoiceSetting extends Setting {
    private final ColoredLabel value;

    private final String key;
    private final Supplier<String > get;
    private final Consumer<String> set;

    private final OverlayOption[] options;

    private SettingOverlay overlay;

    public MultipleChoiceSetting(Context owner) {
        this(owner, "Multiple Choice", () -> "value", (v) -> {});
    }

    public MultipleChoiceSetting(Context owner, String key, Supplier<String> get,
                                 Consumer<String> set, OverlayOption... options) {
        super(owner, key);
        this.key = key;
        this.get = get;
        this.set = set;
        this.options = options;

        value = new ColoredLabel(owner, Style.TEXT_NORM, get.get().toLowerCase());
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

    public SettingOverlay setOverlay() {
        return new SettingOverlay(owner, key, get, v -> {
            set.accept(v);
            value.setKey(v);
        }, options);
    }
}

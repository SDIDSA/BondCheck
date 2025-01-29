package com.sdidsa.bondcheck.app.app_content.session.content.settings.shared;

import android.content.Context;

import com.sdidsa.bondcheck.abs.components.controls.input.checkBox.CheckBox;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.utils.view.SpacerUtils;

import java.util.function.Consumer;

public class CheckSetting extends Setting {

    private final CheckBox check;

    public CheckSetting(Context owner) {
        this(owner, "Check Setting", null);
    }

    public CheckSetting(Context owner, String key, Consumer<Boolean> onChange) {
        super(owner, key);

        check = new CheckBox(owner, 24);

        addPostLabel(SpacerUtils.spacer(owner, Orientation.HORIZONTAL));
        addPostLabel(check);

        check.checkedProperty().addListener((ov, nv) -> {
            if(onChange != null)
                onChange.accept(nv);
        });

        setOnClick(check::toggle);
    }

    public CheckSetting setChecked(boolean val) {
        check.setChecked(val);
        return this;
    }
}

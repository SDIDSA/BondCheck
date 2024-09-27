package com.sdidsa.bondcheck.app.app_content.session.content.settings.shared;

import android.content.Context;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.abs.components.controls.image.ColoredIcon;
import com.sdidsa.bondcheck.abs.components.controls.scratches.Orientation;
import com.sdidsa.bondcheck.abs.style.Style;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;

public class ActionSetting extends Setting {
    public ActionSetting(Context owner) {
        this(owner, "Action setting", -1, null);
    }
    public ActionSetting(Context owner, String key, @DrawableRes int iconRes, Runnable action) {
        super(owner, key);

        setOnClick(action);

        ColoredIcon icon = new ColoredIcon(owner, Style.TEXT_SEC, iconRes, 20);

        addPostLabel(ContextUtils.spacer(owner, Orientation.HORIZONTAL));
        addPostLabel(icon);
    }
}

package com.sdidsa.bondcheck.app.app_content.session.content.settings.about;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.SettingsGroup;

public class AboutGroup extends SettingsGroup {
    public AboutGroup(Context owner) {
        this(owner, null);
    }

    public AboutGroup(Context owner, Settings parent) {
        super(owner, parent, "about_settings", R.drawable.about);

        //TODO add settings
    }
}

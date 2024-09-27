package com.sdidsa.bondcheck.app.app_content.session.content.settings.notifications;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.CheckSetting;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.SettingsGroup;

public class NotificationsGroup extends SettingsGroup {
    public NotificationsGroup(Context owner) {
        this(owner, null);
    }

    public NotificationsGroup(Context owner, Settings parent) {
        super(owner, parent, "notification_settings", R.drawable.bell);

        addSetting(new CheckSetting(owner, "on_screen_notify",
                v -> Store.setNotifyOnScreen(v, null))
                .setChecked(Store.isNotifyOnScreen()));

        addSetting(new CheckSetting(owner, "on_mic_notify",
                v -> Store.setNotifyOnMic(v, null))
                .setChecked(Store.isNotifyOnMic()));

        addSetting(new CheckSetting(owner, "on_location_notify",
                v -> Store.setNotifyOnLocation(v, null))
                .setChecked(Store.isNotifyOnLocation()));
    }
}

package com.sdidsa.bondcheck.app.app_content.session.permission;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.style.Style;

public class WhyNotifications extends WhyPermissionOverlay {
    public WhyNotifications(Context owner) {
        super(owner, "Show Notifications",
                "BondCheck sends notifications to keep you informed about important " +
                        "events, such as location or screen-sharing requests from your partner.",
                () -> PermissionCheck.requestNotificationPermission(owner));

        addLayer(R.drawable.notif_permission_frame, Style.TEXT_SEC);
        addLayer(R.drawable.notif_permission_screen, Style.BACK_SEC);
        addLayer(R.drawable.notif_permission_line1, Style.TEXT_SEC);
        addLayer(R.drawable.notif_permission_line2, Style.TEXT_SEC);
        addLayer(R.drawable.notif_permission_line3, Style.TEXT_SEC);
    }
}
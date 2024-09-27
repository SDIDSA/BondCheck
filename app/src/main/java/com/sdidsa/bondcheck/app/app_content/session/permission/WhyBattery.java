package com.sdidsa.bondcheck.app.app_content.session.permission;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.style.Style;

public class WhyBattery extends WhyPermissionOverlay {
    public WhyBattery(Context owner) {
        super(owner, "Ignore Battery Optimizations",
                "This helps the app run smoothly in the background, so your partner can " +
                        "receive accurate real-time updates without delays or interruptions.",
                () -> PermissionCheck.requestBatteryPermission(owner));

        addLayer(R.drawable.battery_permission_shade, Style.TEXT_SEC);
        addLayer(R.drawable.battery_permission_frame, Style.TEXT_SEC);
        addLayer(R.drawable.battery_permission_screen, Style.BACK_SEC);
        addLayer(R.drawable.battery_permission_back, Style.TEXT_SEC);
        addLayer(R.drawable.battery_permission_content, Style.BACK_PRI);
    }
}

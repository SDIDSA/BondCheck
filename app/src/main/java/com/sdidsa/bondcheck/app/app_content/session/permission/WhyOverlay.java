package com.sdidsa.bondcheck.app.app_content.session.permission;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.style.Style;

public class WhyOverlay extends WhyPermissionOverlay {
    public WhyOverlay(Context owner) {
        super(owner, "Draw Over Other Apps",
                "BondCheck requires this permission to display a small indicator to " +
                        "keep you informed that the app is active.",
                () -> PermissionCheck.requestOverlayPermission(owner));

        addLayer(R.drawable.overlay_permission_frame, Style.TEXT_SEC);
        addLayer(R.drawable.overlay_permission_screen, Style.BACK_SEC);
        addLayer(R.drawable.overlay_permission_content, Style.TEXT_NORM, .4f);
        addLayer(R.drawable.overlay_permission_floating, Style.ACCENT);
        addLayer(R.drawable.overlay_permission_inside, Style.WHITE);
    }
}

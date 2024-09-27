package com.sdidsa.bondcheck.app.app_content.session.permission;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.style.Style;

public class WhyUsage extends WhyPermissionOverlay {
    public WhyUsage(Context owner) {
        super(owner, "Usage access",
                "BondCheck will censor certain apps of your choice, this permission " +
                        "is needed to identify which apps are currently in use.",
                () -> PermissionCheck.requestUsageAccess(owner));

        addLayer(R.drawable.usage_permission_frame, Style.TEXT_SEC);
        addLayer(R.drawable.usage_permission_screen, Style.BACK_TER);
        addLayer(R.drawable.usage_permission_content, Style.TEXT_SEC);
        addLayer(R.drawable.usage_permission_overlay, Style.BACK_SEC);
        addLayer(R.drawable.usage_permission_foregroud, Style.TEXT_NORM);

        skip.setOnClick(() -> {
            PermissionCheck.setSkipUsageStats(true);
            hide();
        });
        skip.setVisibility(VISIBLE);
    }
}

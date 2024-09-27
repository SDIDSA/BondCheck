package com.sdidsa.bondcheck.app.app_content.session.permission;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.style.Style;

public class WhyLocation extends WhyPermissionOverlay {
    public WhyLocation(Context owner) {
        super(owner, "Location access",
                "BondCheck needs access to your location to help you both stay " +
                        "accountable and build trust by sharing your current location when needed.",
                () -> PermissionCheck.requestLocationPermission(owner));

        addLayer(R.drawable.location_permission_frame, Style.TEXT_SEC);
        addLayer(R.drawable.location_permission_screen, Style.BACK_SEC);
        addLayer(R.drawable.location_permission_map, Style.TEXT_MUT, .3f);
        addLayer(R.drawable.location_permission_back, Style.BACK_SEC);
        addLayer(R.drawable.location_permission_marker, Style.TEXT_NORM);

        skip.setOnClick(() -> {
            PermissionCheck.setSkipLocation(true);
            hide();
        });
        skip.setVisibility(VISIBLE);
    }
}

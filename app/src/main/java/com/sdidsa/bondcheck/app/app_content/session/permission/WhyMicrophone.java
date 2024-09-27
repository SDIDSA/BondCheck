package com.sdidsa.bondcheck.app.app_content.session.permission;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.style.Style;

public class WhyMicrophone extends WhyPermissionOverlay {
    public WhyMicrophone(Context owner) {
        super(owner, "Microphone access",
                "BondCheck allows your partner to request live audio from you, " +
                        "ensuring your safety, as well as transparency in your relationship.",
                () -> PermissionCheck.requestMicrophonePermission(owner));

        addLayer(R.drawable.mic_permission_frame, Style.TEXT_SEC);
        addLayer(R.drawable.mic_permission_screen, Style.BACK_SEC);
        addLayer(R.drawable.mic_permission_controls, Style.TEXT_SEC);
        addLayer(R.drawable.mic_permission_track, Style.TEXT_MUT);
        addLayer(R.drawable.mic_permission_accent, Style.TEXT_NORM);

        skip.setOnClick(() -> {
            PermissionCheck.setSkipMicrophone(true);
            hide();
        });
        skip.setVisibility(VISIBLE);
    }
}

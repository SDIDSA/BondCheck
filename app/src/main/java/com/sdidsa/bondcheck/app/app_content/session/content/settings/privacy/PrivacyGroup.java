package com.sdidsa.bondcheck.app.app_content.session.content.settings.privacy;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.ActionSetting;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.SettingsGroup;
import com.sdidsa.bondcheck.app.app_content.session.overlays.CensorAppsOverlay;
import com.sdidsa.bondcheck.app.app_content.session.overlays.RadioOverlay;
import com.sdidsa.bondcheck.app.app_content.session.permission.PermissionCheck;
import com.sdidsa.bondcheck.app.app_content.session.permission.WhyUsage;

public class PrivacyGroup extends SettingsGroup {
    public static final String KEEP_CONSENT = "keep_consent";
    public static final String ASK_EVERY_TIME = "ask_consent";

    private RadioOverlay consent;
    private CensorAppsOverlay censor;

    public PrivacyGroup(Context owner) {
        this(owner, null);
    }

    public PrivacyGroup(Context owner, Settings parent) {
        super(owner, parent, "privacy_settings", R.drawable.shield);

        addSetting(new ActionSetting(owner, "enable_disable_sharing",
                R.drawable.resource_switch, () -> {

        }));

        addSetting(new ActionSetting(owner, "screen_share_consent", R.drawable.time, () -> {
            if(consent == null) {
                consent = new RadioOverlay(owner, "screen_share_consent");
                consent.addButton(KEEP_CONSENT);
                consent.addButton(ASK_EVERY_TIME);

                consent.addOnShowing(() ->
                        consent.select(Store.getScreenConsent()));

                consent.setOnSave(val ->
                        Store.setScreenConsent(val, saved -> consent.hide()));
            }

            consent.show();
        }));

        addSetting(new ActionSetting(owner, "censor_apps", R.drawable.eye_off_filled, () -> {
            if(!PermissionCheck.hasUsageAccessPermission(owner)) {
                new WhyUsage(owner).show();
                return;
            }
            
            if(censor == null) {
                censor = new CensorAppsOverlay(owner);
            }
            censor.show();
        }));
    }
}

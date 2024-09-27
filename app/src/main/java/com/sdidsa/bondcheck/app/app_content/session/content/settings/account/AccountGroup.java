package com.sdidsa.bondcheck.app.app_content.session.content.settings.account;

import android.content.Context;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;
import com.sdidsa.bondcheck.app.app_content.session.overlays.BondSettingsOverlay;
import com.sdidsa.bondcheck.app.app_content.session.overlays.EditProfileOverlay;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.ActionSetting;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.shared.SettingsGroup;
import com.sdidsa.bondcheck.app.app_content.session.overlays.ChangePasswordOverlay;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.requests.PasswordChangeRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;

import retrofit2.Call;

public class AccountGroup extends SettingsGroup {
    private EditProfileOverlay profile;
    private BondSettingsOverlay bond;
    private ChangePasswordOverlay password;

    private final ActionSetting userProfile;
    private final ActionSetting bondSettings;

    public AccountGroup(Context context) {
        this(context, null);
    }

    public AccountGroup(Context owner, Settings parent) {
        super(owner, parent, "account_settings", R.drawable.user_s);

        userProfile = new ActionSetting(owner,"user_profile", R.drawable.edit,() -> {
            if(profile == null) {
                profile = new EditProfileOverlay(owner);
            }
            profile.show();
        });

        bondSettings = new ActionSetting(owner, "bond_settings", R.drawable.bond, () -> {
            if(bond == null) {
                bond = new BondSettingsOverlay(owner);
            }

            bond.show();
        });

        addSetting(userProfile);
        addSetting(bondSettings);

        addSetting(new ActionSetting(owner,"change_pass", R.drawable.lock, () -> {
            if(password == null) {
                password = new ChangePasswordOverlay(owner);
                password.setOnSave((ov, nv) -> {
                    password.startLoading();
                    Call<GenericResponse> call = App.api(owner).changePassword(
                            new PasswordChangeRequest(ov, nv));

                    Service.enqueue(call, resp -> {
                        password.stopLoading();
                        switch (resp.code()) {
                            case 200 -> {
                                ContextUtils.toast(owner, "password changed");
                                password.hide();
                            }
                            case 400 -> ContextUtils.toast(owner,
                                    "Current password is incorrect");
                            case 401 -> ContextUtils.toast(owner,
                                    "New password is invalid");
                            case 404 -> ContextUtils.toast(owner, "User not found ?");
                            default -> ContextUtils.toast(owner, "problem_string");
                        }
                    });
                });
            }

            password.show();
        }));
    }

    public void showUserProfile() {
        userProfile.fire();
    }
    public void showBondSettings() {
        bondSettings.fire();
    }
}

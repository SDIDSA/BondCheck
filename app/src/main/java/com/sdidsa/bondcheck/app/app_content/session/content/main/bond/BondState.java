package com.sdidsa.bondcheck.app.app_content.session.content.main.bond;

import androidx.annotation.DrawableRes;

import com.sdidsa.bondcheck.R;
import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.settings.Settings;
import com.sdidsa.bondcheck.app.app_content.session.overlays.ViewProfileOverlay;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.responses.GenericResponse;

import java.util.function.Consumer;

import retrofit2.Call;

public enum BondState {

    NO_BOND(
            "no_bond",
            "pending_button",
            "bond_search",
            R.drawable.heart_broken,
            owner -> owner.getPendingBondsOverlay().show(),
            owner -> owner.getMakeBondOverlay().show()),
    RECEIVED(
            "They sent you a bond request",
            "",
            "",
            R.drawable.heart_pulse,
            null,
            null),
    BOND_ACTIVE(
            "bond_active",
            "bond_profile",
            "settings",
            R.drawable.heart,
            owner ->
                    ViewProfileOverlay.getInstance(owner.getOwner())
                            .show(owner.getOther_user(), owner.getBondState()),
            owner -> {
                Settings settings = Fragment.getInstance(owner.getOwner(), Settings.class);
                assert settings != null;
                settings.getAccountGroup().showBondSettings();
            }),
    REQUEST_SENT(
            "bond_sent_pending",
            "bond_profile",
            "cancel",
            R.drawable.heart_pulse,
            owner ->
                    ViewProfileOverlay.getInstance(owner.getOwner())
                            .show(owner.getOther_user(), owner.getBondState()),
            source -> {
                Call<GenericResponse> call = App.api(source.getOwner())
                        .cancelRequest();

                source.getAction2().startLoading();
                Service.enqueue(call, resp -> {
                    source.getAction2().stopLoading();

                    switch (resp.code()) {
                        case 200 -> {
                            ContextUtils.toast(source.getOwner(), "Request canceled");
                            source.fetch();
                        }
                        case 400 -> ContextUtils.toast(source.getOwner(),
                                "something wrong with your session");
                        case 404 -> {
                            ContextUtils.toast(source.getOwner(),
                                    "request not found");
                            source.fetch();
                        }
                        case 500 -> ContextUtils.toast(source.getOwner(),
                                "server error...");
                    }
                });
            });

    private final String status;
    private final String action1;
    private final String action2;
    private final @DrawableRes int icon;

    private final Consumer<BondStatus> onAction1;
    private final Consumer<BondStatus> onAction2;

    BondState(String status, String action1, String action2, int icon, Consumer<BondStatus> onAction1, Consumer<BondStatus> onAction2) {
        this.status = status;
        this.action1 = action1;
        this.action2 = action2;
        this.icon = icon;
        this.onAction1 = onAction1;
        this.onAction2 = onAction2;
    }

    public String getStatus() {
        return status;
    }

    public String getAction1() {
        return action1;
    }

    public String getAction2() {
        return action2;
    }

    public int getIcon() {
        return icon;
    }

    public Consumer<BondStatus> getOnAction1() {
        return onAction1;
    }

    public Consumer<BondStatus> getOnAction2() {
        return onAction2;
    }
}

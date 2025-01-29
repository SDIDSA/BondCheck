package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.layout.fragment.Fragment;
import com.sdidsa.bondcheck.abs.components.layout.overlay.MultipleOptionOverlay;
import com.sdidsa.bondcheck.abs.utils.view.ContextUtils;
import com.sdidsa.bondcheck.app.app_content.session.content.main.Main;
import com.sdidsa.bondcheck.app.app_content.session.content.main.bond.BondState;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.responses.GenericResponse;

import retrofit2.Call;

public class DestroyBondOverlay extends MultipleOptionOverlay {
    private Runnable onSuccess;

    public DestroyBondOverlay(Context owner) {
        super(owner, "destroy_header",
                (s) -> s.equals("destroy_no"));

        addButton("destroy_yes", () -> {
            startLoading("destroy_yes");

            Call<GenericResponse> call = App.api(owner).destroy();
            Service.enqueue(call, resp -> {
                stopLoading("destroy_yes");
                int code = resp.code();

                if(code == 200) {
                    addOnHiddenOnce(() -> {
                        Main main = Fragment.getInstance(owner, Main.class);
                        assert main != null;
                        main.getBondStatus().setBondStatus(BondState.NO_BOND);
                        ContextUtils.toast(owner, "bond_destroyed");
                    });
                    if(onSuccess != null) {
                        onSuccess.run();
                    }
                    hide();
                } else if(code == 404) {
                    ContextUtils.toast(owner, "bond_not_found");
                }else {
                    ContextUtils.toast(owner, "problem_string");
                }
            });
        });

        addButton("destroy_no", this::hide);
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }
}

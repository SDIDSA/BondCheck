package com.sdidsa.bondcheck.app.app_content.session.overlays;

import android.content.Context;
import android.content.Intent;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.components.layout.overlay.MultipleOptionOverlay;
import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.app.app_content.auth.login.Login;
import com.sdidsa.bondcheck.app.services.Action;
import com.sdidsa.bondcheck.app.services.SocketService;
import com.sdidsa.bondcheck.http.services.Service;
import com.sdidsa.bondcheck.models.responses.GenericResponse;

import retrofit2.Call;

public class HomeExitOverlay extends MultipleOptionOverlay {
    public HomeExitOverlay(Context owner) {
        super(owner, "exit_header", (s) -> s.equals("exit_stay"));

        addButton("log_out", () -> {
            startLoading("log_out");

            Call<GenericResponse> call = App.api(owner).logout();
            Service.enqueue(call, resp -> {
                stopLoading("log_out");
                int code = resp.code();
                owner.sendBroadcast(App.broadcast(Action.STOP_SERVICE));
                if(code == 200) {
                    Store.setJwtToken("",
                            token -> ContextUtils.setToken(owner, token));
                    Intent killService = new Intent(owner, SocketService.class);
                    owner.stopService(killService);
                    addOnHidden(() -> ContextUtils.loadPage(owner, Login.class));
                    hide();
                } else {
                    ContextUtils.toast(owner, "problem_string");
                }
            });
        });

        addButton("exit_quit", () -> {
            hide();
            ContextUtils.finishAndRemoveTask(owner);
        });

        addButton("exit_stay", this::hide);
    }
}

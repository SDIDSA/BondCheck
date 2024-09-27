package com.sdidsa.bondcheck.http;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sdidsa.bondcheck.abs.utils.ContextUtils;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.abs.utils.Store;
import com.sdidsa.bondcheck.app.app_content.auth.login.Login;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

public class AuthInterceptor implements Interceptor {
    private String token;

    private final Context owner;

    public AuthInterceptor(Context owner, String token) {
        this.token = token;
        this.owner = owner;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Context getOwner() {
        return owner;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) {
        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token);

        Request newRequest = builder.build();

        Platform.sleepReal(700);

        try {
            Response r = chain.proceed(newRequest);
            if (r.code() == 469 || r.code() == 470) {
                clearToken();

                chain.call().cancel();
            }

            return r;
        }catch(IOException ex) {
            ErrorHandler.handle(ex, "executing api call to " + newRequest.url());
            return new Response.Builder()
                    .code(500)
                    .request(newRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .message("can't get to the server")
                    .body(ResponseBody.create(MediaType.get("application/json"), "{'error': 'can't get to the server'}"))
                    .build();
        }
    }

    private void clearToken() {
        ContextUtils.toast(owner, "Your session has expired");
        ContextUtils.loadPage(owner, Login.class, -1);
        Store.setJwtToken("", null);
    }
}
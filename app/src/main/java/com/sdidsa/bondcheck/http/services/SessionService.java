package com.sdidsa.bondcheck.http.services;

import android.content.Context;
import android.util.Log;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.abs.utils.ErrorHandler;
import com.sdidsa.bondcheck.abs.utils.Platform;
import com.sdidsa.bondcheck.models.requests.AssetRequest;
import com.sdidsa.bondcheck.models.requests.PasswordChangeRequest;
import com.sdidsa.bondcheck.models.requests.StringRequest;
import com.sdidsa.bondcheck.models.requests.UserIdRequest;
import com.sdidsa.bondcheck.models.responses.CachedUser;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.responses.UserResponse;

import java.util.HashMap;
import java.util.function.Consumer;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;

public interface SessionService extends Service {
    String SESSION = "/api/session";

    @POST(SESSION + "/logout")
    Call<GenericResponse> logout();

    @Multipart
    @POST(SESSION + "/setAvatar")
    Call<GenericResponse> setAvatar(
            @Part MultipartBody.Part avatarFile
    );

    @POST(SESSION + "/setGender")
    Call<GenericResponse> setGender(@Body StringRequest request);

    @POST(SESSION + "/setBio")
    Call<GenericResponse> setBio(@Body StringRequest request);

    @POST(SESSION + "/setUsername")
    Call<GenericResponse> setUsername(@Body StringRequest request);

    @POST(SESSION + "/getUser")
    Call<UserResponse> getUserInternal(@Body UserIdRequest request);

    @POST(SESSION + "/changePassword")
    Call<GenericResponse> changePassword(@Body PasswordChangeRequest request);

    @POST(SESSION + "/getAsset")
    @Streaming
    Call<ResponseBody> getAsset(@Body AssetRequest request);

    HashMap<String, CachedUser> userCache = new HashMap<>();

    static void getUser(Context context, String id, Consumer<UserResponse> onUser) {
        getUser(context, id, onUser, true);
    }

    static void getUser(Context context, String id, Consumer<UserResponse> onUser, boolean cache) {
        CachedUser user = userCache.get(id);

        if(user == null) {
            user = new CachedUser(new UserResponse());
            userCache.put(id, user);
        }

        final CachedUser finalUser = user;
        if(user.isExpired(60 * 2) || !cache) {
            Call<UserResponse> call = App.api(context).getUserInternal(new UserIdRequest(id));
            Service.enqueue(call, resp -> {
                if(resp.isSuccessful()) {
                    finalUser.setUser(resp.body());
                    Platform.runLater(() -> {
                        finalUser.setUser(resp.body());
                        onUser.accept(finalUser.getUser());
                    });
                }else {
                    ErrorHandler.handle(
                            new RuntimeException("can't fetch user data"),
                            "fetching user data from api");
                }
            });
        }else {
            onUser.accept(user.getUser());
        }
    }
}

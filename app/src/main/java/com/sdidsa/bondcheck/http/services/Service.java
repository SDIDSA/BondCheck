package com.sdidsa.bondcheck.http.services;

import androidx.annotation.NonNull;

import com.sdidsa.bondcheck.BuildConfig;
import com.sdidsa.bondcheck.abs.utils.Platform;

import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public interface Service {
    String BASE_URL = "http://" +
            BuildConfig.LOCAL_IP +
            ":3000/";

    static <T> void enqueue(Call<T> call,
                            Consumer<Response<T>> onSuccess) {

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                Platform.runLater(() -> onSuccess.accept(response));
            }
            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable throwable) {
                //Failure is handled in the interceptor
            }
        });
    }
}

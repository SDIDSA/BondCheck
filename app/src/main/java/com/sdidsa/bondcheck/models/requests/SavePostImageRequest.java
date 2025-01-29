package com.sdidsa.bondcheck.models.requests;

import android.content.Context;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.models.responses.GenericResponse;

import java.io.File;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class SavePostImageRequest {
    private final File data;

    public SavePostImageRequest(File file) {
        this.data = file;
    }

    public Call<GenericResponse> savePostImage(Context owner) {
        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "data",
                data.getName(),
                RequestBody.create(Objects.requireNonNull(MediaType.parse("image/png")), data));

        return App.api(owner).savePostImage(part);
    }
}

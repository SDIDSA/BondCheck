package com.sdidsa.bondcheck.models.requests;

import android.content.Context;

import com.sdidsa.bondcheck.abs.App;
import com.sdidsa.bondcheck.models.DBLocation;
import com.sdidsa.bondcheck.models.responses.GenericResponse;

import java.io.File;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class SaveItemRequest {
    private final String requester;
    private final String app;
    private final String latitude;
    private final String longitude;
    private final File data;

    public SaveItemRequest(String requester, File file, String app, DBLocation location) {
        this.requester = requester;
        this.app = app;
        this.data = file;
        if(location != null) {
            this.latitude = String.valueOf(location.latitude());
            this.longitude = String.valueOf(location.longitude());
        }else {
            this.latitude = null;
            this.longitude = null;
        }
    }

    public SaveItemRequest(String requester, File url, DBLocation location) {
        this(requester, url, null, location);
    }

    public SaveItemRequest(String requester, DBLocation location) {
        this(requester, null, null, location);
    }

    public Call<GenericResponse> saveRecord(Context owner) {
        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "data",
                data.getName(),
                RequestBody.create(Objects.requireNonNull(MediaType.parse("video/mp4")), data)
        );

        return App.api(owner).saveRecord(
                RequestBody.create(MultipartBody.FORM, requester),
                RequestBody.create(MultipartBody.FORM, latitude),
                RequestBody.create(MultipartBody.FORM, longitude),
                part);
    }

    public Call<GenericResponse> saveScreenshot(Context owner) {
        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "data",
                data.getName(),
                RequestBody.create(Objects.requireNonNull(MediaType.parse("image/png")), data)
        );

        return App.api(owner).saveScreenshot(
                RequestBody.create(MultipartBody.FORM, requester),
                RequestBody.create(MultipartBody.FORM, latitude),
                RequestBody.create(MultipartBody.FORM, longitude),
                RequestBody.create(MultipartBody.FORM, app),
                part);
    }
}

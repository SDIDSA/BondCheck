package com.sdidsa.bondcheck.http.services;

import com.sdidsa.bondcheck.models.requests.SaveItemRequest;
import com.sdidsa.bondcheck.models.requests.StringRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.responses.LocationResponse;
import com.sdidsa.bondcheck.models.responses.RecordResponse;
import com.sdidsa.bondcheck.models.responses.RelatedItemsResponse;
import com.sdidsa.bondcheck.models.responses.ScreenshotResponse;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface MainService extends Service {
    String MAIN = "/api/main";

    @POST(MAIN + "/requestScreenshot")
    Call<GenericResponse> requestScreenshot(@Body StringRequest value);

    @POST(MAIN + "/requestMic")
    Call<GenericResponse> requestMic(@Body StringRequest value);

    @POST(MAIN + "/requestLocation")
    Call<GenericResponse> requestLocation(@Body StringRequest value);

    @Multipart
    @POST(MAIN + "/saveScreenshot")
    Call<GenericResponse> saveScreenshot(
            @Part("requester") RequestBody requester,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part("app") RequestBody app,
            @Part MultipartBody.Part recordFile
    );

    @Multipart
    @POST(MAIN + "/saveRecord")
    Call<GenericResponse> saveRecord(
            @Part("requester") RequestBody requester,
            @Part("latitude") RequestBody latitude,
            @Part("longitude") RequestBody longitude,
            @Part MultipartBody.Part recordFile
    );

    @POST(MAIN + "/saveLocation")
    Call<GenericResponse> saveLocation(@Body SaveItemRequest request);

    @POST(MAIN + "/getScreenshots")
    Call<List<ScreenshotResponse>> getScreenshots();

    @POST(MAIN + "/getRecords")
    Call<List<RecordResponse>> getRecords();

    @POST(MAIN + "/relatedItems")
    Call<RelatedItemsResponse> relatedItems(@Body JSONObject request);

    @POST(MAIN + "/getLocations")
    Call<List<LocationResponse>> getLocations();
}

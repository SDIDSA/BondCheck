package com.sdidsa.bondcheck.http.services;

import com.sdidsa.bondcheck.models.requests.SavePostRequest;
import com.sdidsa.bondcheck.models.requests.StringRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.responses.PostResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface PostService extends Service {
    String POST = "/api/post";

    @Multipart
    @POST(POST + "/savePostImage")
    Call<GenericResponse> savePostImage(@Part MultipartBody.Part imageFile);

    @POST(POST + "/savePost")
    Call<PostResponse> savePost(@Body SavePostRequest request);

    @POST(POST + "/getPosts")
    Call<List<PostResponse>> getPosts(@Body StringRequest request);
}

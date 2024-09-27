package com.sdidsa.bondcheck.http.services;

import com.sdidsa.bondcheck.models.requests.TokenRequest;
import com.sdidsa.bondcheck.models.requests.UserRequest;
import com.sdidsa.bondcheck.models.responses.GenericResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService extends Service {
    String AUTH = "/api/auth";

    @POST(AUTH + "/register")
    Call<GenericResponse> register(@Body UserRequest userRequest);

    @POST(AUTH + "/login")
    Call<GenericResponse> login(@Body UserRequest userRequest);

    @POST(AUTH + "/validateToken")
    Call<GenericResponse> validateToken(@Body TokenRequest token);
}

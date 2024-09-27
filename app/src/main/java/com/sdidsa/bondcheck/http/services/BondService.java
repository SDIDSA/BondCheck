package com.sdidsa.bondcheck.http.services;

import com.sdidsa.bondcheck.models.requests.BondObject;
import com.sdidsa.bondcheck.models.requests.StringRequest;
import com.sdidsa.bondcheck.models.responses.CheckBondResponse;
import com.sdidsa.bondcheck.models.responses.GenericResponse;
import com.sdidsa.bondcheck.models.responses.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface BondService extends Service {
    String BOND = "/api/bond";

    @POST(BOND + "/checkBond")
    Call<CheckBondResponse> checkBond();

    @POST(BOND + "/searchUsers")
    Call<List<UserResponse>> searchUsers(@Body StringRequest value);

    @POST(BOND + "/sendRequest")
    Call<GenericResponse> sendRequest(@Body StringRequest value);

    @POST(BOND + "/cancelRequest")
    Call<GenericResponse> cancelRequest();

    @POST(BOND + "/pending")
    Call<List<BondObject>> pending();

    @POST(BOND + "/acceptBond")
    Call<GenericResponse> acceptBond(@Body StringRequest value);

    @POST(BOND + "/destroy")
    Call<GenericResponse> destroy();
}

package com.sdidsa.bondcheck.http;

import com.sdidsa.bondcheck.http.services.AuthService;
import com.sdidsa.bondcheck.http.services.BondService;
import com.sdidsa.bondcheck.http.services.MainService;
import com.sdidsa.bondcheck.http.services.SessionService;

public interface ApiService extends
        BondService,
        AuthService,
        SessionService,
        MainService {

}
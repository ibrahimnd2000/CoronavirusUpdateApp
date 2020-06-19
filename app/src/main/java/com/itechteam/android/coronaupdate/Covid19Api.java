package com.itechteam.android.coronaupdate;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Covid19Api {
    @GET("summary")
    Call<Summary> getSummary();
}

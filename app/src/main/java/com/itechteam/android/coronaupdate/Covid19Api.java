package com.itechteam.android.coronaupdate;


import retrofit2.Call;
import retrofit2.http.GET;

public interface Covid19Api {
    @Retry
    @GET("summary")
    Call<Summary> getSummary();
}

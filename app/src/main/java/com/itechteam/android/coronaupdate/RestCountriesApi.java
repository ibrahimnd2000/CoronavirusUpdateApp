package com.itechteam.android.coronaupdate;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RestCountriesApi {
    @GET
    Call<List<CountriesFlag>> getCountryFlags(@Url String url);
}

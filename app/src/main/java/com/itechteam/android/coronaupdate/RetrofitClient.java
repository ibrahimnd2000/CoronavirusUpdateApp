package com.itechteam.android.coronaupdate;

import com.itechteam.android.coronaupdate.RetryCallAdapterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitClient {
    private static final String BASE_URL = "https://api.covid19api.com/";
    private static Retrofit retrofit = null;
    public static Covid19Api getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit
                    .Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(RetryCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(Covid19Api.class);
    }
}

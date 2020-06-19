package com.itechteam.android.coronaupdate;

import com.google.gson.annotations.SerializedName;

public class CountriesFlag {
    @SerializedName("flag")
    private String flag;
    @SerializedName("name")
    private String name;

    public String getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }
}

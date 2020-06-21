package com.itechteam.android.coronaupdate;

import com.google.gson.annotations.SerializedName;

public class CountriesFlag {
    @SerializedName("flag")
    private String flag;

    public CountriesFlag(String flag) {
        this.flag = flag;
    }

    public String getFlag() {
        return flag;
    }
}

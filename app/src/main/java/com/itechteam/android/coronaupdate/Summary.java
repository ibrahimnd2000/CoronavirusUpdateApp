package com.itechteam.android.coronaupdate;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Summary implements Serializable {
    @SerializedName("Global")
    private Global globalList;
    @SerializedName("Countries")
    private List<Countries> countriesList;
    @SerializedName("Date")
    private Date date;

    public class Global {
        @SerializedName("NewConfirmed")
        private int newConfirmed;
        @SerializedName("TotalConfirmed")
        private int totalConfirmed;
        @SerializedName("NewDeaths")
        private int newDeaths;
        @SerializedName("TotalDeaths")
        private int totalDeaths;
        @SerializedName("NewRecovered")
        private int newRecovered;
        @SerializedName("TotalRecovered")
        private int totalRecovered;

        public int getNewConfirmed() {
            return newConfirmed;
        }

        public int getTotalConfirmed() {
            return totalConfirmed;
        }

        public int getNewDeaths() {
            return newDeaths;
        }

        public int getTotalDeaths() {
            return totalDeaths;
        }

        public int getNewRecovered() {
            return newRecovered;
        }

        public int getTotalRecovered() {
            return totalRecovered;
        }
    }

    public class Countries implements Serializable {
        @SerializedName("Country")
        private String country;
        @SerializedName("CountryCode")
        private String countryCode;
        @SerializedName("Slug")
        private String slug;
        @SerializedName("NewConfirmed")
        private int newConfirmed;
        @SerializedName("TotalConfirmed")
        private int totalConfirmed;
        @SerializedName("NewDeaths")
        private int newDeaths;
        @SerializedName("TotalDeaths")
        private int totalDeaths;
        @SerializedName("NewRecovered")
        private int newRecovered;
        @SerializedName("TotalRecovered")
        private int totalRecovered;
        @SerializedName("Date")
        private Date date;

        public String getCountry() {
            return country;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public String getSlug() {
            return slug;
        }

        public int getNewConfirmed() {
            return newConfirmed;
        }

        public int getTotalConfirmed() {
            return totalConfirmed;
        }

        public int getNewDeaths() {
            return newDeaths;
        }

        public int getTotalDeaths() {
            return totalDeaths;
        }

        public int getNewRecovered() {
            return newRecovered;
        }

        public int getTotalRecovered() {
            return totalRecovered;
        }

        public Date getDate() {
            return date;
        }
    }

    public Global getGlobalList() {
        return globalList;
    }

    public List<Countries> getCountriesList() {
        return countriesList;
    }

    public Date getDate() {
        return date;
    }
}

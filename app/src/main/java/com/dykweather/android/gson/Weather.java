package com.dykweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by 1 on 2017/11/26.
 */

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;


}



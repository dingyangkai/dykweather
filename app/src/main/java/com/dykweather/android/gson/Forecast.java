package com.dykweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by 1 on 2017/11/26.
 */

public class Forecast {
    public String date;
    @SerializedName("tmp")
public Temperature temperature;
    @SerializedName("cond")
    public More more;
    public class Temperature{
        public String max;
        public String min;

    }
    public class More{
        @SerializedName("txt_d")
        public String info;
    }
}

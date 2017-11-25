package com.dykweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * Created by 1 on 2017/11/24.
 */

public class County extends DataSupport {
    private int id;
    private String countyName;
    private int cityId;
    private String weatherId;
    public int getId(){
        return id;

    }
    public void setId(int id){
        this.id=id;

    }
    public String getCountyName(){
        return countyName;
    }
    public void setCountyName(String countyName){
        this.countyName=countyName;

    }
    public String getWeatherId(){
        return weatherId;

    }
    public void setWeatherId(String weatherId){
        this.weatherId=weatherId;
    }
    public int getCityId(){
        return cityId;
    }
    public void setCityId(int cityId){
        this.cityId=cityId;
    }
}

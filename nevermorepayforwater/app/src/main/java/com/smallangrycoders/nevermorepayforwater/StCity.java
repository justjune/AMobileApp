package com.smallangrycoders.nevermorepayforwater;

import java.time.LocalDateTime;
import java.util.Date;

public class StCity {
    private String name; // название
    private String temp;  // температура
    private int flagResource; // ресурс флага
    private String lat ;
    private String lon ;


    private LocalDateTime syncDate;//Дата синхронизации


    public StCity(String name, String temp, String lat ,String lon , int flag, LocalDateTime syncDate){

        this.name=name;
        this.temp=temp;
        this.flagResource=flag;
        this.syncDate = syncDate;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemp() {
        return this.temp;
    }
    public String getStrLat() {
        return String.valueOf(this.lat);
    }
    public String getStrLon() {
        return String.valueOf(this.lon);
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public int getFlagResource() {
        return this.flagResource;
    }

    public void setFlagResource(int flagResource) {
        this.flagResource = flagResource;
    }
    public LocalDateTime getSyncDate(){
        return this.syncDate;
    }

    public void setSyncDate(LocalDateTime syncDate){
        this.syncDate = syncDate;
    }
}

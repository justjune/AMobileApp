package com.smallangrycoders.nevermorepayforwater;

import java.io.Serializable;
import java.time.LocalDateTime;

public class StCity implements Serializable {
    private long id;
    private String name;
    private String temp;
    private int flagResource;
    private String lat ;
    private String lon ;
    private LocalDateTime syncDate;

    //Getters
    public String getName() {
        return this.name;
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
    public int getFlagResource() {
        return this.flagResource;
    }
    public LocalDateTime getSyncDate(){
        return this.syncDate;
    }

   //Setters
    public void setId(long id) {
       this.id = id;
   }
    public void setName(String name) {
        this.name = name;
    }
    public void setTemp(String temp) {
        this.temp = temp;
    }
    public void setStrLat(String strLat) {
        this.lat = strLat;
    }
    public void setStrLon(String strLon) {
        this.lon = strLon;
    }
    public void setFlagResource(int flagResource) {
        this.flagResource = flagResource;
    }
    public void setSyncDate(LocalDateTime syncDate){
        this.syncDate = syncDate;
    }

    public StCity(long id, String name, String temp, String lat ,String lon , int flag, LocalDateTime syncDate){
        setId(id);
        setName(name);
        setTemp(temp);
        setFlagResource(flag);
        setSyncDate(syncDate);
        setStrLat(lat);
        setStrLon(lon);
    }

    public long getId() {
        return this.id;
    }
}

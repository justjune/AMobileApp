package com.smallangrycoders.nevermorepayforwater;

import java.io.Serializable;
import java.time.LocalDateTime;

public class City implements Serializable {
    private long id;
    private String name;
    private String temp;
    private int flagResource;
    private String lat;
    private String lon;
    private LocalDateTime dateTime;

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getTemp() {
        return this.temp;
    }

    public String getLatitude() {
        return String.valueOf(this.lat);
    }

    public String getLongtitude() {
        return String.valueOf(this.lon);
    }

    public int getFlagResource() {
        return this.flagResource;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

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

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public City(long id, String name, String temp, String lat, String lon, int flag, LocalDateTime dateTime) {
        setId(id);
        setName(name);
        setTemp(temp);
        setFlagResource(flag);
        setDateTime(dateTime);
        setStrLat(lat);
        setStrLon(lon);
    }
}

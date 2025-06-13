package com.smallangrycoders.nevermorepayforwater;

import java.time.LocalDateTime;

public class HeatRecord {
    private LocalDateTime date;
    private double tempIn;
    private double tempOut;
    private double volume;

    public HeatRecord(LocalDateTime date, double tempIn, double tempOut, double volume) {
        this.date = date;
        this.tempIn = tempIn;
        this.tempOut = tempOut;
        this.volume = volume;
    }

    // Геттеры
    public LocalDateTime getDate() { return date; }
    public double getTempIn() { return tempIn; }
    public double getTempOut() { return tempOut; }
    public double getVolume() { return volume; }
}
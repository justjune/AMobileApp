package com.smallangrycoders.nevermorepayforwater;

import java.util.Date;

public class StHeatEntry {
    public long date;
    public double radiatorTemp;
    public double sourceTemp;

    public StHeatEntry(Date date, double radiatorTemp, double sourceTemp) {
        this.date = date.getTime();
        this.radiatorTemp = radiatorTemp;
        this.sourceTemp = sourceTemp;
    }

    public StHeatEntry(long date, double radiatorTemp, double sourceTemp) {
        this.date = date;
        this.radiatorTemp = radiatorTemp;
        this.sourceTemp = sourceTemp;
    }
}

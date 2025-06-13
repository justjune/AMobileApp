package com.smallangrycoders.nevermorepayforwater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Objects;

public class DBCities {


    private static final String TAG          = "DBCities";
    private static final String DB_NAME      = "cities.db";
    private static final int    DB_VERSION   = 2;                     // ↑


    private static final String T_CITIES     = "cities";

    private static final String C_ID         = "id";
    private static final String C_NAME       = "name";
    private static final String C_TEMPR      = "tempr";
    private static final String C_LAT        = "lat";
    private static final String C_LON        = "lon";
    private static final String C_FLAG       = "flag2";
    private static final String C_SYNC       = "syncdate";
    private static final String C_VOLUME     = "volume_m3_month";     // NEW

    private static final int I_ID    = 0;
    private static final int I_NAME  = 1;
    private static final int I_TEMPR = 2;
    private static final int I_LAT   = 3;
    private static final int I_LON   = 4;
    private static final int I_FLAG  = 5;
    private static final int I_SYNC  = 6;
    private static final int I_VOL   = 7;


    private static final String T_HEAT  = "heat_log";

    private static final String H_ID    = "id";
    private static final String H_CITY  = "city_id";
    private static final String H_DATE  = "date";          // YYYY-MM-DD
    private static final String H_TBAT  = "t_bat";
    private static final String H_TSRC  = "t_src";


    private static SQLiteDatabase db;

    public DBCities(Context ctx) {
        db = new OpenHelper(ctx.getApplicationContext()).getWritableDatabase();
    }

    public static void insert(String name, String temp,
                              String lat, String lon,
                              int flag, LocalDateTime sync) {
        ContentValues cv = new ContentValues();
        cv.put(C_NAME,  name);
        cv.put(C_TEMPR, temp);
        cv.put(C_LAT,   lat);
        cv.put(C_LON,   lon);
        cv.put(C_FLAG,  flag);
        cv.put(C_SYNC,  String.valueOf(sync));
        cv.put(C_VOLUME, 0);                          // пока 0

        try {
            db.insertOrThrow(T_CITIES, null, cv);
        } catch (SQLException ex) {
            Log.e(TAG, "Insert city error", ex);
            throw ex;
        }
    }

    public int update(StCity st) {
        ContentValues cv = new ContentValues();
        cv.put(C_NAME,  st.getName());
        cv.put(C_TEMPR, st.getTemp());
        cv.put(C_LAT,   st.getStrLat());
        cv.put(C_LON,   st.getStrLon());
        cv.put(C_FLAG,  st.getFlagResource());
        cv.put(C_SYNC,  st.getSyncDate().toString());

        return db.update(T_CITIES, cv,
                C_ID + "=?", new String[]{String.valueOf(st.getId())});
    }

    public void deleteAll() {
        db.delete(T_CITIES, null, null);
    }

    public ArrayList<StCity> selectAll() {
        ArrayList<StCity> list = new ArrayList<>();
        try (Cursor cur = db.query(T_CITIES, null,
                null, null, null, null, null)) {

            if (cur.moveToFirst()) {
                do {
                    long   id   = cur.getLong(I_ID);
                    String name = cur.getString(I_NAME);
                    String temp = cur.getString(I_TEMPR);
                    String lat  = cur.getString(I_LAT);
                    String lon  = cur.getString(I_LON);
                    int    flag = cur.getInt(I_FLAG);

                    LocalDateTime sync = null;
                    String syncStr = cur.getString(I_SYNC);
                    if (!Objects.equals(syncStr, "null")) {
                        sync = LocalDateTime.parse(syncStr);
                    }

                    list.add(new StCity(id, name, temp, lat, lon, flag, sync));

                } while (cur.moveToNext());
            }
        }
        return list;
    }


    public void setCityVolume(long cityId, double m3PerMonth) {
        ContentValues cv = new ContentValues();
        cv.put(C_VOLUME, m3PerMonth);
        db.update(T_CITIES, cv, C_ID + "=?",
                new String[]{String.valueOf(cityId)});
    }
    public double getCityVolume(long cityId) {
        try (Cursor c = db.query(T_CITIES,
                new String[]{C_VOLUME},
                C_ID + "=?", new String[]{String.valueOf(cityId)},
                null, null, null)) {
            if (c.moveToFirst()) return c.getDouble(0);
        }
        return 0;
    }

    public void insertHeat(long cityId, LocalDate date,
                           double tBat, double tSrc) {

        ContentValues cv = new ContentValues();
        cv.put(H_CITY, cityId);
        cv.put(H_DATE, date.toString());
        cv.put(H_TBAT, tBat);
        cv.put(H_TSRC, tSrc);

        db.insertWithOnConflict(T_HEAT, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public double calcHeat(long cityId, YearMonth ym) {

        String first = ym.atDay(1).toString();
        String last  = ym.atEndOfMonth().toString();

        double sumDelta = 0;

        try (Cursor cur = db.query(T_HEAT,
                new String[]{H_TBAT, H_TSRC},
                H_CITY + "=? AND " + H_DATE + " BETWEEN ? AND ?",
                new String[]{String.valueOf(cityId), first, last},
                null, null, null)) {

            while (cur.moveToNext()) {
                sumDelta += cur.getDouble(0) - cur.getDouble(1);
            }
        }

        double vMonth   = getCityVolume(cityId);              // м³ / мес
        int    days     = ym.lengthOfMonth();
        double vDayLit  = vMonth * 1000 / days;               // литров / день

        return sumDelta * vDayLit;                            // ккал
    }


    private static class OpenHelper extends SQLiteOpenHelper {

        OpenHelper(Context ctx) { super(ctx, DB_NAME, null, DB_VERSION); }

        @Override public void onCreate(SQLiteDatabase db) {

            String createCities = "CREATE TABLE " + T_CITIES + " (" +
                    C_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    C_NAME   + " TEXT," +
                    C_TEMPR  + " TEXT," +
                    C_LAT    + " TEXT," +
                    C_LON    + " TEXT," +
                    C_FLAG   + " INT," +
                    C_SYNC   + " TEXT," +
                    C_VOLUME + " REAL DEFAULT 0" +
                    ");";

            String createHeat = "CREATE TABLE " + T_HEAT + " (" +
                    H_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    H_CITY + " INTEGER," +
                    H_DATE + " TEXT," +
                    H_TBAT + " REAL," +
                    H_TSRC + " REAL," +
                    "UNIQUE(" + H_CITY + "," + H_DATE + ")" +
                    ");";

            db.execSQL(createCities);
            db.execSQL(createHeat);
        }

        @Override public void onUpgrade(SQLiteDatabase db,
                                        int oldVer, int newVer) {
            db.execSQL("DROP TABLE IF EXISTS " + T_CITIES);
            db.execSQL("DROP TABLE IF EXISTS " + T_HEAT);
            onCreate(db);
        }
    }
}

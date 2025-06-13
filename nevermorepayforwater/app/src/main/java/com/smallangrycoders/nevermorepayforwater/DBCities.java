package com.smallangrycoders.nevermorepayforwater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DBCities {
    private static final String DATABASE_NAME = "cities.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "cities";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TEMPR = "tempr";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_LON = "lon";
    private static final String COLUMN_FLAG2 = "flag2";
    private static final String COLUMN_SYNCDATE = "syncdate";

    private static final String HEAT_TABLE = "heat_data";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TEMP_IN = "temp_in";
    private static final String COLUMN_TEMP_OUT = "temp_out";
    private static final String COLUMN_VOLUME = "volume";

    private static final int NUM_COLUMN_ID = 0;
    private static final int NUM_COLUMN_NAME = 1;
    private static final int NUM_COLUMN_TEMPR = 2;
    private static final int NUM_COLUMN_LAT = 3;
    private static final int NUM_COLUMN_LON = 4;
    private static final int NUM_COLUMN_FLAG2 = 5;
    private static final int NUM_COLUMN_SYNCDATE = 6;

    private static SQLiteDatabase stcDataBase;

    public DBCities(Context context) {
        OpenHelper mOpenHelper = new OpenHelper(context);
        stcDataBase = mOpenHelper.getWritableDatabase();
    }

    public static void insert(String name, String temp, String lat, String lon, int flag, LocalDateTime syncDate) {
        ContentValues cv=new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_TEMPR, temp);
        cv.put(COLUMN_LAT, lat);
        cv.put(COLUMN_LON,lon);
        cv.put(COLUMN_FLAG2, flag);
        cv.put(COLUMN_SYNCDATE, String.valueOf(syncDate));
        stcDataBase.insert(TABLE_NAME, null, cv);
    }

    public int update(City stc) {
        ContentValues cv=new ContentValues();
        cv.put(COLUMN_NAME, stc.getName());
        cv.put(COLUMN_TEMPR, stc.getTemp());
        cv.put(COLUMN_LAT, stc.getLatitude());
        cv.put(COLUMN_LON,stc.getLongtitude());
        cv.put(COLUMN_FLAG2, stc.getFlagResource());
        cv.put(COLUMN_SYNCDATE,stc.getDateTime().toString());

        return stcDataBase.update(TABLE_NAME, cv, COLUMN_ID + " = ?",new String[] { String.valueOf(stc.getId())});
    }

    public void insertHeatData(LocalDateTime date, double tempIn, double tempOut, double volume) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DATE, date.toString());
        cv.put(COLUMN_TEMP_IN, tempIn);
        cv.put(COLUMN_TEMP_OUT, tempOut);
        cv.put(COLUMN_VOLUME, volume);
        stcDataBase.insert(HEAT_TABLE, null, cv);
    }

    public List<HeatRecord> getAllHeatData() {
        List<HeatRecord> records = new ArrayList<>();
        Cursor cursor = stcDataBase.query(HEAT_TABLE, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            LocalDateTime date = LocalDateTime.parse(cursor.getString(1));
            double tempIn = cursor.getDouble(2);
            double tempOut = cursor.getDouble(3);
            double volume = cursor.getDouble(4);
            records.add(new HeatRecord(date, tempIn, tempOut, volume));
        }
        cursor.close();
        return records;
    }

    public ArrayList<City> selectAll() {
        Cursor mCursor = stcDataBase.query(TABLE_NAME, null, null, null, null, null, null);

        ArrayList<City> arr = new ArrayList<City>();
        mCursor.moveToFirst();
        if (!mCursor.isAfterLast()) {
            do {
                long id = mCursor.getLong(NUM_COLUMN_ID);
                String name = mCursor.getString(NUM_COLUMN_NAME);
                String temp = mCursor.getString(NUM_COLUMN_TEMPR);
                String lat = mCursor.getString(NUM_COLUMN_LAT);
                String lon = mCursor.getString(NUM_COLUMN_LON);
                int flag = mCursor.getInt(NUM_COLUMN_FLAG2);
                LocalDateTime syncDate = null;
                if (!Objects.equals(mCursor.getString(NUM_COLUMN_SYNCDATE), "null")) {
                    syncDate = LocalDateTime.parse(mCursor.getString(NUM_COLUMN_SYNCDATE));
                }
                arr.add(new City(id,  name,  temp,  lat , lon ,  flag,  syncDate));
            } while (mCursor.moveToNext());
        }
        return arr;
    }
    private class OpenHelper extends SQLiteOpenHelper {

        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            String query = "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME+ " TEXT, " +
                    COLUMN_TEMPR + " TEXT, " +
                    COLUMN_LAT + " TEXT,"+
                    COLUMN_LON + " TEXT,"+
                    COLUMN_FLAG2 + " INT,"+
                    COLUMN_SYNCDATE+" TEXT);";
            db.execSQL(query);

            String heatQuery = "CREATE TABLE " + HEAT_TABLE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_TEMP_IN + " REAL, " +
                    COLUMN_TEMP_OUT + " REAL, " +
                    COLUMN_VOLUME + " REAL);";
            db.execSQL(heatQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}


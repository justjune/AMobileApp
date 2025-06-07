package com.smallangrycoders.nevermorepayforwater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class DBCities {
    private static final String DATABASE_NAME = "cities.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "cities";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TEMPR = "tempr";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_LON = "lon";
    private static final String COLUMN_FLAG2 = "flag2";
    private static final String COLUMN_SYNCDATE = "syncdate";

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

    public void insert(String name, String temp, String lat, String lon, int flag, LocalDateTime syncDate) {
        if (stcDataBase == null || !stcDataBase.isOpen()) {
            Log.e("DBCities", "Database is not available");
            return;
        }

        try {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_NAME, name);
            cv.put(COLUMN_TEMPR, temp);
            cv.put(COLUMN_LAT, lat);
            cv.put(COLUMN_LON, lon);
            cv.put(COLUMN_FLAG2, flag);
            cv.put(COLUMN_SYNCDATE, syncDate != null ? syncDate.toString() : null);

            long result = stcDataBase.insert(TABLE_NAME, null, cv);
            if (result == -1) {
                Log.e("DBCities", "Failed to insert record - possible constraint violation");
            }
        } catch (SQLiteException e) {
            Log.e("DBCities", "Database error", e);
        } catch (Exception e) {
            Log.e("DBCities", "Unexpected error", e);
        }
    }

    public int update(StCity stc) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, stc.getName());
        cv.put(COLUMN_TEMPR, stc.getTemp());
        cv.put(COLUMN_LAT, stc.getStrLat());
        cv.put(COLUMN_LON, stc.getStrLon());
        cv.put(COLUMN_FLAG2, stc.getFlagResource());
        cv.put(COLUMN_SYNCDATE, stc.getSyncDate().toString());

        return stcDataBase.update(TABLE_NAME, cv, COLUMN_ID + " = ?", new String[]{String.valueOf(stc.getId())});
    }

    public void deleteAll() {
        stcDataBase.delete(TABLE_NAME, null, null);
    }

    public ArrayList<StCity> selectAll() {
        Cursor mCursor = stcDataBase.query(TABLE_NAME, null, null, null, null, null, null);

        ArrayList<StCity> arr = new ArrayList<StCity>();
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
                arr.add(new StCity(id, name, temp, lat, lon, flag, syncDate));
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
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_TEMPR + " TEXT, " +
                    COLUMN_LAT + " TEXT," +
                    COLUMN_LON + " TEXT," +
                    COLUMN_FLAG2 + " INT," +
                    COLUMN_SYNCDATE + " TEXT);";
            db.execSQL(query);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}

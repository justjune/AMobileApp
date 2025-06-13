package com.smallangrycoders.nevermorepayforwater;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class DBCities {
    private static final String TAG = "DBCities";
    private static final String DATABASE_NAME = "cities.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "cities";

    // Колонки
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TEMPR = "tempr";
    private static final String COLUMN_LAT = "lat";
    private static final String COLUMN_LON = "lon";
    private static final String COLUMN_FLAG2 = "flag2";
    private static final String COLUMN_SYNCDATE = "syncdate";

    // Индексы колонок
    private static final int NUM_COLUMN_ID = 0;
    private static final int NUM_COLUMN_NAME = 1;
    private static final int NUM_COLUMN_TEMPR = 2;
    private static final int NUM_COLUMN_LAT = 3;
    private static final int NUM_COLUMN_LON = 4;
    private static final int NUM_COLUMN_FLAG2 = 5;
    private static final int NUM_COLUMN_SYNCDATE = 6;

    private SQLiteDatabase stcDataBase;
    private final OpenHelper mOpenHelper;

    public DBCities(Context context) {
        mOpenHelper = new OpenHelper(context);
    }

    public void open() throws SQLException {
        stcDataBase = mOpenHelper.getWritableDatabase();
    }

    public void close() {
        if (stcDataBase != null && stcDataBase.isOpen()) {
            stcDataBase.close();
        }
    }

    public boolean delete(long id) {
        SQLiteDatabase db = null;
        try {
            db = mOpenHelper.getWritableDatabase();
            db.beginTransaction();

            int rowsAffected = db.delete(TABLE_NAME,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});

            db.setTransactionSuccessful();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Error deleting record: " + e.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.endTransaction();
                if (db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    public long insert(String name, String temp, String lat, String lon, int flag, LocalDateTime syncDate) {
        SQLiteDatabase db = null;
        try {
            db = mOpenHelper.getWritableDatabase();
            db.beginTransaction();

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_NAME, name);
            cv.put(COLUMN_TEMPR, temp);
            cv.put(COLUMN_LAT, lat);
            cv.put(COLUMN_LON, lon);
            cv.put(COLUMN_FLAG2, flag);
            cv.put(COLUMN_SYNCDATE, String.valueOf(syncDate));

            long id = db.insert(TABLE_NAME, null, cv);
            db.setTransactionSuccessful();
            return id;
        } catch (SQLException e) {
            Log.e(TAG, "Error inserting record: " + e.getMessage());
            return -1;
        } finally {
            if (db != null) {
                db.endTransaction();
                if (db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    public boolean update(StCity stc) {
        SQLiteDatabase db = null;
        try {
            db = mOpenHelper.getWritableDatabase();
            db.beginTransaction();

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_NAME, stc.getName());
            cv.put(COLUMN_TEMPR, stc.getTemp());
            cv.put(COLUMN_LAT, stc.getStrLat());
            cv.put(COLUMN_LON, stc.getStrLon());
            cv.put(COLUMN_FLAG2, stc.getFlagResource());
            cv.put(COLUMN_SYNCDATE, stc.getSyncDate().toString());

            int rowsAffected = db.update(TABLE_NAME, cv,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(stc.getId())});

            db.setTransactionSuccessful();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Error updating record: " + e.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.endTransaction();
                if (db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    public boolean deleteAll() {
        SQLiteDatabase db = null;
        try {
            db = mOpenHelper.getWritableDatabase();
            db.beginTransaction();

            int rowsAffected = db.delete(TABLE_NAME, null, null);
            db.setTransactionSuccessful();
            return rowsAffected >= 0;
        } catch (SQLException e) {
            Log.e(TAG, "Error deleting all records: " + e.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.endTransaction();
                if (db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    public ArrayList<StCity> selectAll() {
        SQLiteDatabase db = null;
        Cursor mCursor = null;
        ArrayList<StCity> arr = new ArrayList<>();

        try {
            db = mOpenHelper.getReadableDatabase();
            mCursor = db.query(TABLE_NAME, null, null, null, null, null, null);

            if (mCursor != null && mCursor.moveToFirst()) {
                do {
                    long id = mCursor.getLong(NUM_COLUMN_ID);
                    String name = mCursor.getString(NUM_COLUMN_NAME);
                    String temp = mCursor.getString(NUM_COLUMN_TEMPR);
                    String lat = mCursor.getString(NUM_COLUMN_LAT);
                    String lon = mCursor.getString(NUM_COLUMN_LON);
                    int flag = mCursor.getInt(NUM_COLUMN_FLAG2);
                    LocalDateTime syncDate = null;

                    if (!Objects.equals(mCursor.getString(NUM_COLUMN_SYNCDATE), "null")) {
                        try {
                            syncDate = LocalDateTime.parse(mCursor.getString(NUM_COLUMN_SYNCDATE));
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing date: " + e.getMessage());
                        }
                    }
                    arr.add(new StCity(id, name, temp, lat, lon, flag, syncDate));
                } while (mCursor.moveToNext());
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error selecting records: " + e.getMessage());
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return arr;
    }

    private static class OpenHelper extends SQLiteOpenHelper {
        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                String query = "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_NAME + " TEXT NOT NULL, " +
                        COLUMN_TEMPR + " TEXT, " +
                        COLUMN_LAT + " TEXT, " +
                        COLUMN_LON + " TEXT, " +
                        COLUMN_FLAG2 + " INTEGER DEFAULT 1, " +
                        COLUMN_SYNCDATE + " TEXT);";
                db.execSQL(query);
            } catch (SQLException e) {
                Log.e(TAG, "Error creating table: " + e.getMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.beginTransaction();
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                Log.e(TAG, "Error upgrading database: " + e.getMessage());
            } finally {
                db.endTransaction();
            }
        }
    }
}
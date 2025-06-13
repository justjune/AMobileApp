package com.smallangrycoders.nevermorepayforwater;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

public class DBHeatEntries extends SQLiteOpenHelper {
    private static final String DB_NAME = "heat_entries.db";
    private static final int DB_VERSION = 1;

    public DBHeatEntries(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE entries (date INTEGER PRIMARY KEY, radiator REAL, source REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {}

    public void insert(StHeatEntry entry) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", entry.date);
        values.put("radiator", entry.radiatorTemp);
        values.put("source", entry.sourceTemp);
        db.insert("entries", null, values);
    }

    public List<StHeatEntry> getAll() {
        List<StHeatEntry> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM entries ORDER BY date DESC", null);
        while (cursor.moveToNext()) {
            long date = cursor.getLong(0);
            double radiator = cursor.getDouble(1);
            double source = cursor.getDouble(2);
            list.add(new StHeatEntry(date, radiator, source));
        }
        cursor.close();
        return list;
    }

    public void clearAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("entries", null, null); // исправлено имя таблицы
        db.close();
    }
}

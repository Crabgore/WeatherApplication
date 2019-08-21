package com.geekbrains.android_1.weatherapplication.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class CitiesTable {
    private final static String TABLE_NAME = "Cities";
    private final static String COLUMN_ID = "_id";
    private final static String COLUMN_TITLE = "title";
    private final static String COLUMN_CURRENT_TEMP = "current_temp";
    private final static String COLUMN_WEATHER_TYPE = "weather_type";
    private final static String COLUMN_WIND_SPEED = "wind_speed";
    private final static String COLUMN_PRESSURE = "pressure";
    private final static String COLUMN_HUMIDITY = "humidity";
    private final static String COLUMN_OTHER = "other";

    static void createTable(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_TITLE + " TEXT," + COLUMN_CURRENT_TEMP
                + " INTEGER," + COLUMN_WEATHER_TYPE + " INTEGER," + COLUMN_WIND_SPEED
                + " INTEGER," + COLUMN_PRESSURE + " INTEGER," + COLUMN_HUMIDITY + " INTEGER);");
    }

    static void onUpgrade(SQLiteDatabase database) {
        database.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_OTHER
                + " TEXT DEFAULT 'Default'");
    }

    public static void addNewCityWeather(String title, SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);

        database.insert(TABLE_NAME, null, values);
    }

    public static void deleteCity(String title, SQLiteDatabase database) {
        database.delete(TABLE_NAME, COLUMN_TITLE + " = " + "'" + title + "'", null);
    }

    public static void editCityWeather(String title, String currentTemp, String weatherType,
                                String windSpeed, String pressure, String humidity, SQLiteDatabase database) {
        database.execSQL("UPDATE " + TABLE_NAME + " set " + COLUMN_CURRENT_TEMP + " = " + currentTemp + ", "
                + COLUMN_WEATHER_TYPE + " = " + "'" + weatherType + "'" + ", " + COLUMN_WIND_SPEED
                + " = " + windSpeed + ", " +  COLUMN_PRESSURE + " = " + pressure + ", "
                + COLUMN_HUMIDITY + " = " + humidity + " WHERE " + COLUMN_TITLE + " = " + "'" + title + "'" + ";");
    }

    public static ArrayList<String> getAllNotes(SQLiteDatabase database) {
        Cursor cursor = database.query(TABLE_NAME, null, null, null,
                null, null, null);
        return getResultFromCursor(cursor);
    }

    private static ArrayList<String> getResultFromCursor(Cursor cursor) {
        ArrayList<String> result = null;

        if(cursor != null && cursor.moveToFirst()) {
            result = new ArrayList<>(cursor.getCount());

            int noteIdx = cursor.getColumnIndex(COLUMN_TITLE);
            do {
                result.add(cursor.getString(noteIdx));
            } while (cursor.moveToNext());
        }

        try {
            assert cursor != null;
            cursor.close(); } catch (Exception ignored) {}
        return result == null ? new ArrayList<String>(0) : result;
    }
}

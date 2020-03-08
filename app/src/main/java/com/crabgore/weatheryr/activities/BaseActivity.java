package com.crabgore.weatheryr.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class BaseActivity extends AppCompatActivity {

    public static SharedPreferences mSettings;

    public final static String APP_PREFERENCES_TEMP_UNIT = "TEMP_UNIT";
    public final static String APP_PREFERENCES_WIND_SPEED_UNIT = "WIND_SPEED_UNIT";
    public final static String APP_PREFERENCES_SHOW_WIND_SPEED = "SHOW_WIND_SPEED";
    public final static String APP_PREFERENCES_SHOW_PRESSURE = "SHOW_PRESSURE";
    public final static String APP_PREFERENCES_SHOW_HUMIDITY = "SHOW_HUMIDITY";
    public final static String CHOSEN_CITY = "CHOSEN_CITY";
    public final static String CHOSEN_CITY_ID = "CHOSEN_CITY_ID";
    public final static String DAY_NIGHT = "DAY_NIGHT";
    public final static String PERMISSION = "PERMISSION";

    public FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        applyTheme();

        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    protected void applyTheme() {
        if (mSettings.getString(DAY_NIGHT, "no").equals("auto")) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_AUTO_TIME);
        } else if (mSettings.getString(DAY_NIGHT, "no").equals("no")) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        } else if (mSettings.getString(DAY_NIGHT, "no").equals("yes")) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    public static void saveInFile(Context context, String cityName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("CityName", cityName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        File file = new File(context.getFilesDir() + File.separator + "cityname.json");
        try {
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(jsonObject.toString());
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

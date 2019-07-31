package com.geekbrains.android_1.weatherapplication.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.geekbrains.android_1.weatherapplication.R;

public abstract class BaseActivity extends AppCompatActivity {

    public static SharedPreferences mSettings;

    public final static String APP_PREFERENCES = "mysettings";
    public final static String APP_PREFERENCES_TEMP_UNIT = "TEMP_UNIT";
    public final static String APP_PREFERENCES_WIND_SPEED_UNIT = "WIND_SPEED_UNIT";
    public final static String APP_PREFERENCES_SHOW_WIND_SPEED = "SHOW_WIND_SPEED";
    public final static String APP_PREFERENCES_SHOW_PRESSURE = "SHOW_PRESSURE";
    public final static String CHOSEN_CITY = "CHOSEN_CITY";
    public final static String CHOSEN_CITY_ID = "CHOSEN_CITY_ID";
    private final static String NameSharedPreferences = "WEATHERAPP";
    private final static String IsDarkTheme = "IS_DARK_THEME";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        if (isDarkTheme()){
            setTheme(R.style.DarkTheme);
        }
        else setTheme(R.style.AppTheme);
    }

    protected boolean isDarkTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(NameSharedPreferences, MODE_PRIVATE);
        return sharedPreferences.getBoolean(IsDarkTheme, true);
    }

    protected void setDarkTheme(boolean isDarkTheme){
        SharedPreferences sharedPreferences = getSharedPreferences(NameSharedPreferences, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IsDarkTheme, isDarkTheme);
        editor.apply();
    }
}

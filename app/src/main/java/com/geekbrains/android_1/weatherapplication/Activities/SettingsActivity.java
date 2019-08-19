package com.geekbrains.android_1.weatherapplication.Activities;

import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.geekbrains.android_1.weatherapplication.R;

public class SettingsActivity extends BaseActivity {

    private RadioGroup tempVal;
    private RadioGroup spdUnt;
    private CheckBox showWindSpeed;
    private CheckBox showPressure;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tempVal = findViewById(R.id.temp);
        spdUnt = findViewById(R.id.wind);
        showWindSpeed = findViewById(R.id.showWindSpeed);
        showPressure = findViewById(R.id.showPressure);
        spinner = findViewById(R.id.citySpinner);

        settingsCheck();

        SwitchCompat switchTheme = findViewById(R.id.dark_theme_enabler);
        switchTheme.setChecked(isDarkTheme());
        switchTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDarkTheme(isChecked);
                recreate();
            }
        });
    }

    public void settingsAccept(View view) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(APP_PREFERENCES_TEMP_UNIT, ((RadioButton)tempVal.getChildAt(0)).isChecked());
        editor.putBoolean(APP_PREFERENCES_WIND_SPEED_UNIT, ((RadioButton)spdUnt.getChildAt(0)).isChecked());
        editor.putBoolean(APP_PREFERENCES_SHOW_WIND_SPEED, showWindSpeed.isChecked());
        editor.putBoolean(APP_PREFERENCES_SHOW_PRESSURE, showPressure.isChecked());
        editor.putString(CHOSEN_CITY, spinner.getSelectedItem().toString());
        editor.putInt(CHOSEN_CITY_ID, spinner.getSelectedItemPosition());
        editor.apply();
        finish();
    }

    private void settingsCheck(){
        if (mSettings != null){
            if (mSettings.getBoolean(APP_PREFERENCES_TEMP_UNIT, true)) ((RadioButton)tempVal.getChildAt(0)).setChecked(true);
            else ((RadioButton)tempVal.getChildAt(1)).setChecked(true);
            if (mSettings.getBoolean(APP_PREFERENCES_WIND_SPEED_UNIT, true)) ((RadioButton)spdUnt.getChildAt(0)).setChecked(true);
            else ((RadioButton)spdUnt.getChildAt(1)).setChecked(true);
            if (mSettings.getBoolean(APP_PREFERENCES_SHOW_WIND_SPEED, true)) showWindSpeed.setChecked(true);
            if (mSettings.getBoolean(APP_PREFERENCES_SHOW_PRESSURE, true)) showPressure.setChecked(true);
            spinner.setSelection(mSettings.getInt(CHOSEN_CITY_ID, 0));
        }
    }
}

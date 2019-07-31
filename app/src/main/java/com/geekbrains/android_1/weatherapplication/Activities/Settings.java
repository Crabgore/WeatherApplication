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

public class Settings extends BaseActivity {

    private RadioGroup tempVal;
    private RadioGroup spdUnt;
    private CheckBox showWindSpeed;
    private CheckBox showPressure;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSettings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);

        tempVal = findViewById(R.id.temp);
        spdUnt = findViewById(R.id.wind);
        showWindSpeed = findViewById(R.id.showWindSpeed);
        showPressure = findViewById(R.id.showPressure);
        spinner = findViewById(R.id.citySpinner);

        SettingsCheck();

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

    public void settings_accept(View view) {
        SharedPreferences.Editor editor = BaseActivity.mSettings.edit();
        if (((RadioButton)tempVal.getChildAt(0)).isChecked()){
            editor.putBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true);
        } else if (((RadioButton)tempVal.getChildAt(1)).isChecked()){
            editor.putBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, false);
        }
        if (((RadioButton)spdUnt.getChildAt(0)).isChecked()){
            editor.putBoolean(BaseActivity.APP_PREFERENCES_WIND_SPEED_UNIT, true);
        } else if (((RadioButton)spdUnt.getChildAt(1)).isChecked()){
            editor.putBoolean(BaseActivity.APP_PREFERENCES_WIND_SPEED_UNIT, false);
        }
        if (showWindSpeed.isChecked()){
            editor.putBoolean(BaseActivity.APP_PREFERENCES_SHOW_WIND_SPEED, true);
        } else {
            editor.putBoolean(BaseActivity.APP_PREFERENCES_SHOW_WIND_SPEED, false);
        }
        if (showPressure.isChecked()){
            editor.putBoolean(BaseActivity.APP_PREFERENCES_SHOW_PRESSURE, true);
        } else {
            editor.putBoolean(BaseActivity.APP_PREFERENCES_SHOW_PRESSURE, false);
        }
        editor.putString(BaseActivity.CHOSEN_CITY, spinner.getSelectedItem().toString());
        editor.putInt(BaseActivity.CHOSEN_CITY_ID, spinner.getSelectedItemPosition());
        editor.apply();
        finish();
    }

    private void SettingsCheck(){
        if (BaseActivity.mSettings != null){
            if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true)) ((RadioButton)tempVal.getChildAt(0)).setChecked(true);
            else ((RadioButton)tempVal.getChildAt(1)).setChecked(true);
            if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_WIND_SPEED_UNIT, true)) ((RadioButton)spdUnt.getChildAt(0)).setChecked(true);
            else ((RadioButton)spdUnt.getChildAt(1)).setChecked(true);
            if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_WIND_SPEED, true)) showWindSpeed.setChecked(true);
            if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_PRESSURE, true)) showPressure.setChecked(true);
            spinner.setSelection(BaseActivity.mSettings.getInt(BaseActivity.CHOSEN_CITY_ID, 0));
        }
    }
}

package com.geekbrains.android_1.weatherapplication.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.geekbrains.android_1.weatherapplication.BuildConfig;
import com.geekbrains.android_1.weatherapplication.MainActivity;
import com.geekbrains.android_1.weatherapplication.R;
import com.geekbrains.android_1.weatherapplication.database.CitiesTable;
import com.geekbrains.android_1.weatherapplication.rest.OpenWeatherRepo;
import com.geekbrains.android_1.weatherapplication.rest.currentRest.entities.WeatherRequestRestModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends BaseActivity {
    private RadioGroup tempVal;
    private RadioGroup spdUnt;
    private CheckBox showWindSpeed;
    private CheckBox showPressure;
    private Spinner spinner;
    private EditText addCity;
    public static ArrayList<String> cities = CitiesTable.getAllNotes(MainActivity.database);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initUI();

        initSpinner();

        settingsCheck();
    }

    private void initUI() {
        tempVal = findViewById(R.id.temp);
        spdUnt = findViewById(R.id.wind);
        showWindSpeed = findViewById(R.id.showWindSpeed);
        showPressure = findViewById(R.id.showPressure);
        spinner = findViewById(R.id.citySpinner);
        addCity = findViewById(R.id.addCity);
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

    private void initSpinner() {
        cities = CitiesTable.getAllNotes(MainActivity.database);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
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

    public void settingsAccept(View view) {
        int count = spinner.getCount();
        if (count != 0) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean(APP_PREFERENCES_TEMP_UNIT, ((RadioButton) tempVal.getChildAt(0)).isChecked());
            editor.putBoolean(APP_PREFERENCES_WIND_SPEED_UNIT, ((RadioButton) spdUnt.getChildAt(0)).isChecked());
            editor.putBoolean(APP_PREFERENCES_SHOW_WIND_SPEED, showWindSpeed.isChecked());
            editor.putBoolean(APP_PREFERENCES_SHOW_PRESSURE, showPressure.isChecked());
            editor.putString(CHOSEN_CITY, spinner.getSelectedItem().toString());
            editor.putInt(CHOSEN_CITY_ID, spinner.getSelectedItemPosition());
            editor.apply();
            finish();
        } else Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_city), Toast.LENGTH_SHORT).show();
    }

    public void addCity(View view) {
        OpenWeatherRepo.getSingleton().getCAPI().loadWeather(addCity.getText().toString(), BuildConfig.WEATHER_API_KEY)
                .enqueue(new Callback<WeatherRequestRestModel>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherRequestRestModel> call,
                                           @NonNull Response<WeatherRequestRestModel> response) {
                        if (response.body() != null && response.isSuccessful() && response.body().cod != 404) {
                            if (!cities.contains(addCity.getText().toString())) {
                                CitiesTable.addNewCityWeather(addCity.getText().toString(), MainActivity.database);
                                addCity.getText().clear();
                                initSpinner();
                            } else Toast.makeText(getApplicationContext(), getResources().getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
                        } else Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_such_city), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherRequestRestModel> call, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), getString(R.string.network_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void delCity(View view) {
        if (spinner.getSelectedItem() != null) {
            CitiesTable.deleteCity(spinner.getSelectedItem().toString(), MainActivity.database);
            initSpinner();
        }
    }

    public void MyLoc(View view) {
        if (MainActivity.location != null) {
            String currentLocation = getAddressByLoc(MainActivity.location);
            if (cities.isEmpty() || !cities.contains(currentLocation)) {
                CitiesTable.addNewCityWeather(currentLocation, MainActivity.database);
                initSpinner();
                spinner.setSelection(cities.indexOf(currentLocation));
            } else if (cities.contains(currentLocation)) {
                spinner.setSelection(cities.indexOf(currentLocation));
            }
        }
    }

    private String getAddressByLoc(Location loc) {
        final Geocoder geo = new Geocoder(this, Locale.ENGLISH);
        List<Address> list = null;
        try {
            list = geo.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert list != null;
        String cityName = list.get(0).getLocality();
        String countryCode = list.get(0).getCountryCode();

        return cityName + "," + countryCode;
    }
}

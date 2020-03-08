package com.crabgore.weatheryr.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.crabgore.weatheryr.MainActivity;
import com.crabgore.weatheryr.R;
import com.crabgore.weatheryr.database.CitiesTable;
import com.crabgore.weatheryr.rest.OpenWeatherRepo;
import com.crabgore.weatheryr.rest.currentRest.entities.WeatherRequestRestModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
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
    private CheckBox showHumidity;
    private CheckBox dark;
    private CheckBox light;
    private CheckBox auto;
    private Spinner spinner;
    private EditText addCity;
    public static ArrayList<String> cities = CitiesTable.getAllNotes(MainActivity.database);
    SharedPreferences.Editor editor = mSettings.edit();

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
        showHumidity = findViewById(R.id.showHumidity);
        spinner = findViewById(R.id.citySpinner);
        addCity = findViewById(R.id.addCity);
        dark = findViewById(R.id.dark);
        light = findViewById(R.id.light);
        auto = findViewById(R.id.auto);

        dark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                light.setChecked(false);
                auto.setChecked(false);
            }
        });
        light.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                dark.setChecked(false);
                auto.setChecked(false);
            }
        });
        auto.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                light.setChecked(false);
                dark.setChecked(false);
            }
        });
    }

    private void initSpinner() {
        cities = CitiesTable.getAllNotes(MainActivity.database);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    private void settingsCheck() {
        if (mSettings != null) {
            if (mSettings.getBoolean(APP_PREFERENCES_TEMP_UNIT, true))
                ((RadioButton) tempVal.getChildAt(0)).setChecked(true);
            else ((RadioButton) tempVal.getChildAt(1)).setChecked(true);
            if (mSettings.getBoolean(APP_PREFERENCES_WIND_SPEED_UNIT, true))
                ((RadioButton) spdUnt.getChildAt(0)).setChecked(true);
            else ((RadioButton) spdUnt.getChildAt(1)).setChecked(true);
            if (mSettings.getBoolean(APP_PREFERENCES_SHOW_WIND_SPEED, true))
                showWindSpeed.setChecked(true);
            if (mSettings.getBoolean(APP_PREFERENCES_SHOW_PRESSURE, true))
                showPressure.setChecked(true);
            if (mSettings.getBoolean(APP_PREFERENCES_SHOW_HUMIDITY, true))
                showHumidity.setChecked(true);
            if (mSettings.getString(DAY_NIGHT, "no").equals("yes")) dark.setChecked(true);
            else dark.setChecked(false);
            if (mSettings.getString(DAY_NIGHT, "no").equals("no")) light.setChecked(true);
            else light.setChecked(false);
            if (mSettings.getString(DAY_NIGHT, "no").equals("auto")) auto.setChecked(true);
            else auto.setChecked(false);
            spinner.setSelection(mSettings.getInt(CHOSEN_CITY_ID, 0));
        }
    }

    public void settingsAccept(View view) {
        int count = spinner.getCount();
        if (count != 0) {
            Intent resultIntent = new Intent();
            editor.putBoolean(APP_PREFERENCES_TEMP_UNIT, ((RadioButton) tempVal.getChildAt(0)).isChecked());
            editor.putBoolean(APP_PREFERENCES_WIND_SPEED_UNIT, ((RadioButton) spdUnt.getChildAt(0)).isChecked());
            editor.putBoolean(APP_PREFERENCES_SHOW_WIND_SPEED, showWindSpeed.isChecked());
            editor.putBoolean(APP_PREFERENCES_SHOW_PRESSURE, showPressure.isChecked());
            editor.putBoolean(APP_PREFERENCES_SHOW_HUMIDITY, showHumidity.isChecked());
            if (dark.isChecked()) editor.putString(DAY_NIGHT, "yes");
            if (light.isChecked()) editor.putString(DAY_NIGHT, "no");
            if (auto.isChecked()) editor.putString(DAY_NIGHT, "auto");
            editor.putString(CHOSEN_CITY, spinner.getSelectedItem().toString());
            editor.putInt(CHOSEN_CITY_ID, spinner.getSelectedItemPosition());
            editor.apply();

            saveInFile(this, spinner.getSelectedItem().toString());

            setResult(RESULT_OK, resultIntent);
            finish();
        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_city), Toast.LENGTH_SHORT).show();
    }

    public void addCity(View view) {
        OpenWeatherRepo.getSingleton().getCAPI().loadWeather(addCity.getText().toString(), com.crabgore.weatheryr.BuildConfig.WEATHER_API_KEY)
                .enqueue(new Callback<WeatherRequestRestModel>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherRequestRestModel> call,
                                           @NonNull Response<WeatherRequestRestModel> response) {
                        if (response.body() != null && response.isSuccessful() && response.body().cod != 404) {
                            if (!cities.contains(addCity.getText().toString())) {
                                CitiesTable.addNewCityWeather(addCity.getText().toString(), MainActivity.database);
                                initSpinner();
                                spinner.setSelection(cities.indexOf(addCity.getText().toString()));
                                addCity.getText().clear();
                            } else
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.already_exists), Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_such_city), Toast.LENGTH_SHORT).show();
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
        if (mSettings.getBoolean(PERMISSION, true)) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            String currentLocation = getAddressByLoc(location);
                            if (cities.isEmpty() || !cities.contains(currentLocation)) {
                                CitiesTable.addNewCityWeather(currentLocation, MainActivity.database);
                                initSpinner();
                                spinner.setSelection(cities.indexOf(currentLocation));
                            } else if (cities.contains(currentLocation)) {
                                spinner.setSelection(cities.indexOf(currentLocation));
                            }
                        } else
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_location), Toast.LENGTH_SHORT).show();
                    });
        } else Toast.makeText(this, "Разрешение не предоставлено", Toast.LENGTH_SHORT).show();
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

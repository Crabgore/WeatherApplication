package com.geekbrains.android_1.weatherapplication.Fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.BuildConfig;
import com.geekbrains.android_1.weatherapplication.MainActivity;
import com.geekbrains.android_1.weatherapplication.R;
import com.geekbrains.android_1.weatherapplication.database.CitiesTable;
import com.geekbrains.android_1.weatherapplication.rest.OpenWeatherRepo;
import com.geekbrains.android_1.weatherapplication.rest.currentRest.entities.WeatherRequestRestModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherFragment extends Fragment {

    private static SharedPreferences mSettings;

    private TextView city;
    private TextView date;
    private TextView temperature;
    private TextView windSpeedUnits;
    private LinearLayout windSpeed;
    private LinearLayout pressure;
    private TextView pressureValue;
    private TextView windSpeedValue;
    private TextView willBe;
    private TextView humidityValue;
    private ImageView weatherImage;
    private TextView weatherType;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSettings = PreferenceManager.getDefaultSharedPreferences(getContext());

        View layout = inflater.inflate(R.layout.fragment_weather, container, false);

        initUI(layout);

        settingsCheck();

        setWeather(city.getText().toString());

        date.setText(checkDate());

        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean isLand = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (isLand) {
            assert getFragmentManager() != null;
            FutureFragment detail;
            detail = FutureFragment.create();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.weather, detail);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    }

    private void initUI(View layout){
        city = layout.findViewById(R.id.city);
        temperature = layout.findViewById(R.id.temperature);
        windSpeedUnits = layout.findViewById(R.id.windSpeed_units);
        windSpeed = layout.findViewById(R.id.layout3);
        pressure = layout.findViewById(R.id.layout4);
        date = layout.findViewById(R.id.date);
        pressureValue = layout.findViewById(R.id.pressure_value);
        windSpeedValue = layout.findViewById(R.id.windSpeed_value);
        willBe = layout.findViewById(R.id.willBe);
        humidityValue = layout.findViewById(R.id.humidity_value);
        weatherImage = layout.findViewById(R.id.weather_image);
        weatherType = layout.findViewById(R.id.weather_type);
    }

    private void settingsCheck(){
        if (mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_WIND_SPEED, true)) windSpeed.setVisibility(View.VISIBLE);
        else windSpeed.setVisibility(View.INVISIBLE);
        if (mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_PRESSURE, true)) pressure.setVisibility(View.VISIBLE);
        else pressure.setVisibility(View.INVISIBLE);
        city.setText(mSettings.getString(BaseActivity.CHOSEN_CITY, ""));
    }

    private void setWeather (String cityName) {
        OpenWeatherRepo.getSingleton().getCAPI().loadWeather(cityName, BuildConfig.WEATHER_API_KEY)
                .enqueue(new Callback<WeatherRequestRestModel>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherRequestRestModel> call,
                                           @NonNull Response<WeatherRequestRestModel> response) {
                        if (response.body() != null && response.isSuccessful()) {
                            renderWeather(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherRequestRestModel> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), getString(R.string.network_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderWeather(WeatherRequestRestModel body) {
        setTemp((body.main.temp - 273), (body.main.tempMax - 273), (body.main.tempMin - 273));
        setWind(body.wind.speed);
        setWeatherType(body.weather[0].main);
        pressureValue.setText(String.valueOf(Math.round(body.main.pressure)));
        humidityValue.setText(String.valueOf(Math.round(body.main.humidity)));
        if (!city.getText().toString().equals("")) {
            CitiesTable.editCityWeather(city.getText().toString(), String.valueOf(body.main.temp - 273),
                    weatherType.getText().toString(), windSpeedValue.getText().toString(),
                    pressureValue.getText().toString(), humidityValue.getText().toString(), MainActivity.database);
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setTemp(float temp, float tempMax, float tempMin) {
        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true)){
            temperature.setText(String.format("%.1f °C", temp));
            willBe.setText(getResources().getString(R.string.day) + " " + String.format("%.1f °C", tempMax)
                    + " | " + getResources().getString(R.string.night) + " " + String.format("%.1f °C", tempMin));
        } else {
            temperature.setText(String.format("%.1f °F", ((temp*1.8)+32)));
            willBe.setText(getResources().getString(R.string.day) + " " + String.format("%.1f °F", (tempMax*1.8) + 32)
                    + " | " + getResources().getString(R.string.night) + " " + String.format("%.1f °F", tempMin*1.8)+32);
        }
    }

    @SuppressLint("DefaultLocale")
    private void setWind(float speed) {
        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_WIND_SPEED_UNIT, true)){
            windSpeedValue.setText(String.format("%.0f", (Float)speed));
            windSpeedUnits.setText(getResources().getString(R.string.wind_speed_count_1));
        } else  {
            windSpeedValue.setText(String.valueOf((int)(speed*3.6)));
            windSpeedUnits.setText(getResources().getString(R.string.wind_speed_count_2));
        }
    }

    private void setWeatherType(String main) {
        String s = mSettings.getString(BaseActivity.CHOSEN_CITY, "");

        switch (main) {
            case "Clouds":
                weatherImage.setImageResource(R.drawable.cloud);
                weatherType.setText(getResources().getString(R.string.cloud));
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.cloudy));
                }
                break;
            case "Clear":
                weatherImage.setImageResource(R.drawable.sun);
                weatherType.setText(getResources().getString(R.string.clear));
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.sunny));
                }
                break;
            case "Rain":
                weatherImage.setImageResource(R.drawable.rain);
                weatherType.setText(getResources().getString(R.string.rain));
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.rainy));
                }
                break;
            case "Mist":
            case "Fog":
                weatherImage.setImageResource(R.drawable.mist);
                weatherType.setText(getResources().getString(R.string.mist));
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.misty));
                }
                break;
        }
    }

    private String checkDate(){
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
        return dateFormat.format(currentDate);
    }
}

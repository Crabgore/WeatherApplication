package com.crabgore.weatheryr.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.crabgore.weatheryr.MainActivity;
import com.crabgore.weatheryr.R;
import com.crabgore.weatheryr.activities.BaseActivity;
import com.crabgore.weatheryr.database.CitiesTable;
import com.crabgore.weatheryr.rest.OpenWeatherRepo;
import com.crabgore.weatheryr.rest.currentRest.entities.WeatherRequestRestModel;

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
    private LinearLayout humidity;
    private TextView pressureValue;
    private TextView windSpeedValue;
    private TextView willBe;
    private TextView humidityValue;
    private ImageView weatherImage;
    private TextView weatherType;
    private ProgressBar progressBar;
    private ConstraintLayout mainWeather;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSettings = PreferenceManager.getDefaultSharedPreferences(getContext());

        View layout = inflater.inflate(R.layout.fragment_weather, container, false);

        initUI(layout);

        settingsCheck();

        setWeather(city.getText().toString());

        date.setText(checkDate());

        initSwipe(layout);

        return layout;
    }

    private void initSwipe(View layout) {
        final SwipeRefreshLayout pullToRefresh = layout.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            Objects.requireNonNull(getFragmentManager())
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
            pullToRefresh.setRefreshing(false);
        });
    }

    private void initUI(View layout) {
        city = layout.findViewById(R.id.city);
        temperature = layout.findViewById(R.id.temperature);
        windSpeedUnits = layout.findViewById(R.id.windSpeed_units);
        windSpeed = layout.findViewById(R.id.layout3);
        pressure = layout.findViewById(R.id.layout4);
        humidity = layout.findViewById(R.id.layout5);
        date = layout.findViewById(R.id.date);
        pressureValue = layout.findViewById(R.id.pressure_value);
        windSpeedValue = layout.findViewById(R.id.windSpeed_value);
        willBe = layout.findViewById(R.id.willBe);
        humidityValue = layout.findViewById(R.id.humidity_value);
        weatherImage = layout.findViewById(R.id.weather_image);
        weatherType = layout.findViewById(R.id.weather_type);
        progressBar = layout.findViewById(R.id.progressBar);
        mainWeather = layout.findViewById(R.id.mainWeather);
    }

    private void settingsCheck() {
        if (mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_WIND_SPEED, true))
            windSpeed.setVisibility(View.VISIBLE);
        else windSpeed.setVisibility(View.GONE);
        if (mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_PRESSURE, true))
            pressure.setVisibility(View.VISIBLE);
        else pressure.setVisibility(View.GONE);
        if (mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_HUMIDITY, true))
            humidity.setVisibility(View.VISIBLE);
        else humidity.setVisibility(View.GONE);
        city.setText(mSettings.getString(BaseActivity.CHOSEN_CITY, ""));
    }

    private void setWeather(String cityName) {
        OpenWeatherRepo.getSingleton().getCAPI().loadWeather(cityName, com.crabgore.weatheryr.BuildConfig.WEATHER_API_KEY)
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
        setWeatherType(body.weather[0].main, body.weather[0].description);
        pressureValue.setText(String.valueOf(Math.round(body.main.pressure)));
        humidityValue.setText(String.valueOf(Math.round(body.main.humidity)));
        if (!city.getText().toString().equals("")) {
            CitiesTable.editCityWeather(city.getText().toString(), String.valueOf(body.main.temp - 273),
                    weatherType.getText().toString(), windSpeedValue.getText().toString(),
                    pressureValue.getText().toString(), humidityValue.getText().toString(), MainActivity.database);
        }

        progressBar.setVisibility(View.GONE);
        mainWeather.setVisibility(View.VISIBLE);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setTemp(float temp, float tempMax, float tempMin) {
        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true)) {
            temperature.setText(String.format("%.1f °C", temp));
            willBe.setText(getResources().getString(R.string.day) + " " + String.format("%.1f °C", tempMax)
                    + " | " + getResources().getString(R.string.night) + " " + String.format("%.1f °C", tempMin));
        } else {
            temperature.setText(String.format("%.1f °F", ((temp * 1.8) + 32)));
            willBe.setText(getResources().getString(R.string.day) + " " + String.format("%.1f °F", (tempMax * 1.8) + 32)
                    + " | " + getResources().getString(R.string.night) + " " + String.format("%.1f °F", tempMin * 1.8) + 32);
        }
    }

    @SuppressLint("DefaultLocale")
    private void setWind(float speed) {
        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_WIND_SPEED_UNIT, true)) {
            windSpeedValue.setText(String.format("%.0f", (Float) speed));
            windSpeedUnits.setText(getResources().getString(R.string.wind_speed_count_1));
        } else {
            windSpeedValue.setText(String.valueOf((int) (speed * 3.6)));
            windSpeedUnits.setText(getResources().getString(R.string.wind_speed_count_2));
        }
    }

    private void setWeatherType(String main, String description) {
        String s = mSettings.getString(BaseActivity.CHOSEN_CITY, "");

        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        switch (main) {
            case "Clouds":
                if (description.equals("overcast clouds")) {
                    weatherImage.setImageResource(R.drawable.cloud);
                } else {
                    weatherImage.setImageResource(R.drawable.sunnycloud);
                }
                weatherType.setText(getResources().getString(R.string.cloud));
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.cloudy));
                }
                break;
            case "Clear":
                if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                    weatherImage.setImageResource(R.drawable.sun);
                } else {
                    weatherImage.setImageResource(R.drawable.moon);
                }
                weatherType.setText(getResources().getString(R.string.clear));
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.cleary));
                }
                break;
            case "Rain":
                if (description.equals("heavy rain")) {
                    weatherImage.setImageResource(R.drawable.storm);
                } else {
                    weatherImage.setImageResource(R.drawable.rain);
                }
                weatherType.setText(getResources().getString(R.string.rain));
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.rainy));
                }
                break;
            case "Mist":
                weatherImage.setImageResource(R.drawable.mist);
                weatherType.setText(getResources().getString(R.string.mist));
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.misty));
                }
                break;
            case "Fog":
                weatherImage.setImageResource(R.drawable.mist);
                weatherType.setText(getResources().getString(R.string.mist));
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.foggy));
                }
                break;
            case "Drizzle":
                weatherImage.setImageResource(R.drawable.rain);
                weatherType.setText(R.string.drizzle);
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.drizzly));
                }
                break;
            case "Snow":
                weatherImage.setImageResource(R.drawable.snow);
                weatherType.setText(R.string.snow);
                if (!s.equals("")) {
                    Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(R.drawable.snowy));
                }
                break;
        }

        setGrayScale();
    }

    private void setGrayScale() {
        ColorMatrix matrix = new ColorMatrix();

        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            matrix.setSaturation(0);
            Objects.requireNonNull(getView()).getBackground().setColorFilter(new ColorMatrixColorFilter(matrix));
        } else {
            matrix.setSaturation(1);
            Objects.requireNonNull(getView()).getBackground().setColorFilter(new ColorMatrixColorFilter(matrix));
        }
    }

    private String checkDate() {
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
        return dateFormat.format(currentDate);
    }
}

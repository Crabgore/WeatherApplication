package com.geekbrains.android_1.weatherapplication.Fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.Activities.Settings;
import com.geekbrains.android_1.weatherapplication.BuildConfig;
import com.geekbrains.android_1.weatherapplication.Model.Current.WeatherRequest;
import com.geekbrains.android_1.weatherapplication.R;
import com.geekbrains.android_1.weatherapplication.WeatherData;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {

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

    private SensorManager sensorManager;
    private Sensor sensorTemp;
    private Sensor sensorHumidity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_weather, container, false);

        initUI(layout);

        settingsCheck();

        setWeather(city.getText().toString(), layout);

        getSensors();

        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        date.setText(dateText);

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
            ft.replace(R.id.weather, detail);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listenerTemp, sensorTemp);
        sensorManager.unregisterListener(listenerHumidity, sensorHumidity);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Регистрируем слушатель датчика освещенности
        sensorManager.registerListener(listenerTemp, sensorTemp,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listenerHumidity, sensorHumidity,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void settingsCheck(){
        if (BaseActivity.mSettings != null){
            if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_WIND_SPEED, true)) windSpeed.setVisibility(View.VISIBLE);
            else windSpeed.setVisibility(View.INVISIBLE);
            if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_PRESSURE, true)) pressure.setVisibility(View.VISIBLE);
            else pressure.setVisibility(View.INVISIBLE);
            city.setText(BaseActivity.mSettings.getString(BaseActivity.CHOSEN_CITY, ""));
        } else {
            Intent intent = new Intent(getContext(), Settings.class);
            startActivity(intent);
        }
    }
    private void setWeather(String cityName, final View layout){

        String url = null;

        switch (cityName) {
            case "Moscow":
            case "Москва":
                url = WeatherData.MOSCOW_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Kaliningrad":
            case "Калининград":
                url = WeatherData.Kaliningrad_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Saint Petersburg":
            case "Санкт-Петербург":
                url = WeatherData.Saint_Petersburg_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Novosibirsk":
            case "Новосибирск":
                url = WeatherData.Novosibirsk_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Krasnoyarsk":
            case "Красноярск":
                url = WeatherData.Krasnoyarsk_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Krasnodar":
            case "Краснодар":
                url = WeatherData.Krasnodar_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Arkhangelsk":
            case "Архангельск":
                url = WeatherData.Arkhangelsk_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
        }

        try {
            final URL uri = new URL(url);
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void run() {
                    try {
                        HttpsURLConnection urlConnection;
                        urlConnection = (HttpsURLConnection) uri.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setReadTimeout(10000);
                        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        final String result = in.lines().collect(Collectors.joining("\n"));
                        Gson gson = new Gson();
                        final WeatherRequest weatherRequest = gson.fromJson(result, WeatherRequest.class);

                        handler.post(new Runnable() {
                            @SuppressLint({"DefaultLocale", "SetTextI18n"})
                            @Override
                            public void run() {
                                if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true)){
                                    temperature.setText(String.format("%.1f °C", weatherRequest.getMain().getTemp()));
                                    willBe.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.day) + " " + String.format("%.1f °C", weatherRequest.getMain().getTemp_max()) + " | " + getActivity().getResources().getString(R.string.night) + " " + String.format("%.1f °C", weatherRequest.getMain().getTemp_min()));
                                } else {
                                    temperature.setText(String.format("%.1f °F", ((weatherRequest.getMain().getTemp())*1.8)+32));
                                    willBe.setText(getResources().getString(R.string.day) + " " + String.format("%.1f °F", ((weatherRequest.getMain().getTemp())*1.8) + 32) + " | " + getResources().getString(R.string.night) + " " + String.format("%.1f °F", ((weatherRequest.getMain().getTemp())*1.8)+32));
                                }
                                if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_WIND_SPEED_UNIT, true)){
                                    windSpeedValue.setText(String.format("%.0f", (Float)weatherRequest.getWind().getSpeed()));
                                    windSpeedUnits.setText(R.string.wind_speed_count_1);
                                } else  {
                                    windSpeedValue.setText(String.valueOf((int)(weatherRequest.getWind().getSpeed()*3.6)));
                                    windSpeedUnits.setText(R.string.wind_speed_count_2);
                                }
                                switch (weatherRequest.getWeather()[0].getMain()) {
                                    case "Clouds":
                                        weatherImage.setImageResource(R.drawable.cloud);
                                        weatherType.setText(R.string.cloud);
                                        layout.setBackground(getResources().getDrawable(R.drawable.cloudy));
                                        break;
                                    case "Clear":
                                        weatherImage.setImageResource(R.drawable.sun);
                                        weatherType.setText(R.string.clear);
                                        layout.setBackground(getResources().getDrawable(R.drawable.sunny));
                                        break;
                                    case "Rain":
                                        weatherImage.setImageResource(R.drawable.rain);
                                        weatherType.setText(R.string.rain);
                                        layout.setBackground(getResources().getDrawable(R.drawable.rainy));
                                        break;
                                    case "Mist":
                                    case "Fog":
                                        weatherImage.setImageResource(R.drawable.mist);
                                        weatherType.setText(R.string.mist);
                                        layout.setBackground(getResources().getDrawable(R.drawable.misty));
                                        break;
                                }
                                pressureValue.setText(((Integer)weatherRequest.getMain().getPressure()).toString());
                                humidityValue.setText(((Integer)weatherRequest.getMain().getHumidity()).toString());
                            }
                        });
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
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

    private void getSensors() {
        // Менеджер датчиков
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
//        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            sensorTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
//        }
//        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
            sensorHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
//        }
    }

    private void showTempSensors(SensorEvent event){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(event.values[0]);
        temperature.setText(stringBuilder);
    }

    private void showHumiditySensors(SensorEvent event){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(event.values[0]);
        humidityValue.setText(stringBuilder);
    }


    // Слушатель датчика освещенности
    private SensorEventListener listenerTemp = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        @Override
        public void onSensorChanged(SensorEvent event) {
            showTempSensors(event);
        }
    };

    private SensorEventListener listenerHumidity = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        @Override
        public void onSensorChanged(SensorEvent event) {
            showHumiditySensors(event);
        }
    };
}

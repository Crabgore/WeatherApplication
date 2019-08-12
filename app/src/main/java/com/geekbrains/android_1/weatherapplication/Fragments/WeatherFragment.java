package com.geekbrains.android_1.weatherapplication.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.BuildConfig;
import com.geekbrains.android_1.weatherapplication.R;
import com.geekbrains.android_1.weatherapplication.Services.WeatherRequestService;
import com.geekbrains.android_1.weatherapplication.WeatherData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {

    public final static String BROADCAST_ACTION = "my_weather_application_2";
    private ServiceFinishedReceiver receiver = new ServiceFinishedReceiver();

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

        String url = urlRequest(city.getText().toString());

        setWeather(url);

        getSensors();

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
        Objects.requireNonNull(getActivity()).registerReceiver(receiver, new IntentFilter(BROADCAST_ACTION));
        sensorManager.registerListener(listenerTemp, sensorTemp, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listenerHumidity, sensorHumidity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onStop() {
        super.onStop();
        Objects.requireNonNull(getActivity()).unregisterReceiver(receiver);
    }

    private void settingsCheck(){
        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_WIND_SPEED, true)) windSpeed.setVisibility(View.VISIBLE);
        else windSpeed.setVisibility(View.INVISIBLE);
        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_PRESSURE, true)) pressure.setVisibility(View.VISIBLE);
        else pressure.setVisibility(View.INVISIBLE);
        city.setText(BaseActivity.mSettings.getString(BaseActivity.CHOSEN_CITY, ""));
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

    private void setWeather (String url) {
        Intent intent = new Intent(getActivity(), WeatherRequestService.class);
        intent.putExtra("city_name", url);
        Objects.requireNonNull(getActivity()).startService(intent);
    }

    private String checkDate(){
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
        return dateFormat.format(currentDate);
    }

    private void getSensors() {
        sensorManager = (SensorManager) Objects.requireNonNull(getActivity()).getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            sensorTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
            sensorHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        }
    }

    private void showTempSensors(SensorEvent event){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(event.values[0]);
//        temperature.setText(stringBuilder);
    }

    private void showHumiditySensors(SensorEvent event){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(event.values[0]);
//        humidityValue.setText(stringBuilder);
    }

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

    private String urlRequest(String cityName){

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

        return url;
    }

    private class ServiceFinishedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String s = BaseActivity.mSettings.getString(BaseActivity.CHOSEN_CITY, "");

            temperature.setText(intent.getStringExtra("temperature"));
            willBe.setText(intent.getStringExtra("willBe"));
            windSpeedValue.setText(intent.getStringExtra("windSpeedValue"));
            windSpeedUnits.setText(intent.getStringExtra("windSpeedUnits"));
            weatherType.setText(intent.getStringExtra("weatherType"));
            pressureValue.setText(intent.getStringExtra("pressureValue"));
            humidityValue.setText(intent.getStringExtra("humidityValue"));
            weatherImage.setImageResource(intent.getIntExtra("weatherImage", 0));
            assert s != null;
            if (!s.equals("")) {
                Objects.requireNonNull(getView()).setBackground(getResources().getDrawable(intent.getIntExtra("layout", 0)));
            }
        }
    }
}

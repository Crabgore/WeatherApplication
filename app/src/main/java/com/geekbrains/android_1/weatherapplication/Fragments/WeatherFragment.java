package com.geekbrains.android_1.weatherapplication.Fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
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
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {


    private TextView tv;
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

    public WeatherFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Button info;

        View layout = inflater.inflate(R.layout.fragment_weather, container, false);

        tv = layout.findViewById(R.id.city);
        temperature = layout.findViewById(R.id.temperature);
        windSpeedUnits = layout.findViewById(R.id.windSpeed_units);
        windSpeed = layout.findViewById(R.id.layout3);
        pressure = layout.findViewById(R.id.layout4);
        TextView date = layout.findViewById(R.id.date);
        pressureValue = layout.findViewById(R.id.pressure_value);
        windSpeedValue = layout.findViewById(R.id.windSpeed_value);
        willBe = layout.findViewById(R.id.willBe);
        humidityValue = layout.findViewById(R.id.humidity_value);
        weatherImage = layout.findViewById(R.id.weather_image);
        weatherType = layout.findViewById(R.id.weather_type);

        SettingsCheck();

        SetWeather(tv.getText().toString());

        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMMM", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        date.setText(dateText);

        info = layout.findViewById(R.id.info);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://ru.wikipedia.org/wiki/" + tv.getText().toString();
                Uri uri = Uri.parse(url);
                Intent browser = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browser);
            }
        });

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

    private void SettingsCheck(){
        if (BaseActivity.mSettings != null){
            if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_WIND_SPEED, true)) windSpeed.setVisibility(View.VISIBLE);
            else windSpeed.setVisibility(View.INVISIBLE);
            if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_SHOW_PRESSURE, true)) pressure.setVisibility(View.VISIBLE);
            else pressure.setVisibility(View.INVISIBLE);
            tv.setText(BaseActivity.mSettings.getString(BaseActivity.CHOSEN_CITY, ""));
        }
    }

    private void SetWeather(String cityName){

        String url = null;

        if (cityName.equals("Moscow") || cityName.equals("Москва")) url = WeatherData.MOSCOW_WEATHER_URL;
        if (cityName.equals("Kaliningrad") || cityName.equals("Калининград")) url = WeatherData.Kaliningrad_WEATHER_URL;
        if (cityName.equals("Saint Petersburg") || cityName.equals("Санкт-Петербург")) url = WeatherData.Saint_Petersburg_WEATHER_URL;
        if (cityName.equals("Novosibirsk") || cityName.equals("Новосибирск")) url = WeatherData.Novosibirsk_WEATHER_URL;
        if (cityName.equals("Krasnoyarsk") || cityName.equals("Красноярск")) url = WeatherData.Krasnoyarsk_WEATHER_URL;
        if (cityName.equals("Krasnodar") || cityName.equals("Краснодар")) url = WeatherData.Krasnodar_WEATHER_URL;
        if (cityName.equals("Arkhangelsk") || cityName.equals("Архангельск")) url = WeatherData.Arkhangelsk_WEATHER_URL;

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
                                    willBe.setText(getActivity().getResources().getString(R.string.day) + " " + String.format("%.1f °C", weatherRequest.getMain().getTemp_max()) + " | " + getActivity().getResources().getString(R.string.night) + " " + String.format("%.1f °C", weatherRequest.getMain().getTemp_min()));
                                } else {
                                    temperature.setText(String.format("%.1f °F", (weatherRequest.getMain().getTemp())*32));
                                    willBe.setText(getResources().getString(R.string.day) + " " + String.format("%.1f °F", (weatherRequest.getMain().getTemp())*32) + " | " + getResources().getString(R.string.night) + " " + String.format("%.1f °F", (weatherRequest.getMain().getTemp())*32));
                                }
                                if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_WIND_SPEED_UNIT, true)){
                                    windSpeedValue.setText(((Integer)weatherRequest.getWind().getSpeed()).toString());
                                    windSpeedUnits.setText(R.string.wind_speed_count_1);
                                } else  {
                                    windSpeedValue.setText(String.valueOf((int)(weatherRequest.getWind().getSpeed()*3.6)));
                                    windSpeedUnits.setText(R.string.wind_speed_count_2);
                                }
                                if (weatherRequest.getWeather()[0].getMain().equals("Clouds")) {
                                    weatherImage.setImageResource(R.drawable.cloud);
                                    weatherType.setText(R.string.cloud);
                                    getActivity().getWindow().setBackgroundDrawableResource(R.drawable.cloudy);
                                }
                                if (weatherRequest.getWeather()[0].getMain().equals("Clear")) {
                                    weatherImage.setImageResource(R.drawable.sun);
                                    weatherType.setText(R.string.clear);
                                    getActivity().getWindow().setBackgroundDrawableResource(R.drawable.sunny);
                                }
                                if (weatherRequest.getWeather()[0].getMain().equals("Rain")) {
                                    weatherImage.setImageResource(R.drawable.rain);
                                    weatherType.setText(R.string.rain);
                                    getActivity().getWindow().setBackgroundDrawableResource(R.drawable.rainy);
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
}

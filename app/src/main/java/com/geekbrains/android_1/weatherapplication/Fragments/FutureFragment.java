package com.geekbrains.android_1.weatherapplication.Fragments;


import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.Adapters.FutureAdapter;
import com.geekbrains.android_1.weatherapplication.Model.Future.FutureWeatherRequest;
import com.geekbrains.android_1.weatherapplication.R;
import com.geekbrains.android_1.weatherapplication.WeatherData;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class FutureFragment extends Fragment {

    private ArrayList<String> dayNight = new ArrayList<>();
    private ArrayList<String> weatherType = new ArrayList<>();

    static FutureFragment create(){
        FutureFragment f = new FutureFragment();

        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }


    public FutureFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String[] data = getResources().getStringArray(R.array.days);

        View layout = inflater.inflate(R.layout.fragment_future, container, false);

        SetWeather(Objects.requireNonNull(BaseActivity.mSettings.getString(BaseActivity.CHOSEN_CITY, "")));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        initRecyclerView(data, dayNight, weatherType, layout);

        return layout;
    }

    private void initRecyclerView(String[] data, ArrayList dayNight, ArrayList weatherType, View layout){
        RecyclerView recyclerView = layout.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        FutureAdapter futureAdapter = new FutureAdapter(data, dayNight, weatherType);
        recyclerView.setAdapter(futureAdapter);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(layout.getContext(), LinearLayoutManager.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.separator));
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void SetWeather(String cityName){

        String url = null;

        if (cityName.equals("Moscow") || cityName.equals("Москва")) url = WeatherData.FUTURE_MOSCOW_WEATHER_URL;
        if (cityName.equals("Kaliningrad") || cityName.equals("Калининград")) url = WeatherData.FUTURE_Kaliningrad_WEATHER_URL;
        if (cityName.equals("Saint Petersburg") || cityName.equals("Санкт-Петербург")) url = WeatherData.FUTURE_Saint_Petersburg_WEATHER_URL;
        if (cityName.equals("Novosibirsk") || cityName.equals("Новосибирск")) url = WeatherData.FUTURE_Novosibirsk_WEATHER_URL;
        if (cityName.equals("Krasnoyarsk") || cityName.equals("Красноярск")) url = WeatherData.FUTURE_Krasnoyarsk_WEATHER_URL;
        if (cityName.equals("Krasnodar") || cityName.equals("Краснодар")) url = WeatherData.FUTURE_Krasnodar_WEATHER_URL;
        if (cityName.equals("Arkhangelsk") || cityName.equals("Архангельск")) url = WeatherData.FUTURE_Arkhangelsk_WEATHER_URL;

        try {
            final URL uri = new URL(url);
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @SuppressLint({"DefaultLocale"})
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
                        final FutureWeatherRequest futureWeatherRequest = gson.fromJson(result, FutureWeatherRequest.class);
                        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true)){
                            dayNight.add(String.format("%.1f °C", futureWeatherRequest.getList()[0].getMain().getTemp_max()) + " | " + String.format("%.1f °C", futureWeatherRequest.getList()[0].getMain().getTemp_min()));
                            dayNight.add(String.format("%.1f °C", futureWeatherRequest.getList()[1].getMain().getTemp_max()) + " | " + String.format("%.1f °C", futureWeatherRequest.getList()[1].getMain().getTemp_min()));
                            dayNight.add(String.format("%.1f °C", futureWeatherRequest.getList()[2].getMain().getTemp_max()) + " | " + String.format("%.1f °C", futureWeatherRequest.getList()[2].getMain().getTemp_min()));
                            dayNight.add(String.format("%.1f °C", futureWeatherRequest.getList()[3].getMain().getTemp_max()) + " | " + String.format("%.1f °C", futureWeatherRequest.getList()[3].getMain().getTemp_min()));
                            dayNight.add(String.format("%.1f °C", futureWeatherRequest.getList()[4].getMain().getTemp_max()) + " | " + String.format("%.1f °C", futureWeatherRequest.getList()[4].getMain().getTemp_min()));
                        } else {
                            dayNight.add(String.format("%.1f °F", (futureWeatherRequest.getList()[0].getMain().getTemp_max())*32) + " | " + String.format("%.1f °F", (futureWeatherRequest.getList()[0].getMain().getTemp_min())*32));
                            dayNight.add(String.format("%.1f °F", (futureWeatherRequest.getList()[1].getMain().getTemp_max())*32) + " | " + String.format("%.1f °F", (futureWeatherRequest.getList()[1].getMain().getTemp_min())*32));
                            dayNight.add(String.format("%.1f °F", (futureWeatherRequest.getList()[2].getMain().getTemp_max())*32) + " | " + String.format("%.1f °F", (futureWeatherRequest.getList()[2].getMain().getTemp_min())*32));
                            dayNight.add(String.format("%.1f °F", (futureWeatherRequest.getList()[3].getMain().getTemp_max())*32) + " | " + String.format("%.1f °F", (futureWeatherRequest.getList()[3].getMain().getTemp_min())*32));
                            dayNight.add(String.format("%.1f °F", (futureWeatherRequest.getList()[4].getMain().getTemp_max())*32) + " | " + String.format("%.1f °F", (futureWeatherRequest.getList()[4].getMain().getTemp_min())*32));
                        }
                        weatherType.add(futureWeatherRequest.getList()[0].getWeather()[0].getMain());
                        weatherType.add(futureWeatherRequest.getList()[1].getWeather()[0].getMain());
                        weatherType.add(futureWeatherRequest.getList()[2].getWeather()[0].getMain());
                        weatherType.add(futureWeatherRequest.getList()[3].getWeather()[0].getMain());
                        weatherType.add(futureWeatherRequest.getList()[4].getWeather()[0].getMain());
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

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
import com.geekbrains.android_1.weatherapplication.BuildConfig;
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
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.HttpsURLConnection;


/**
 * A simple {@link Fragment} subclass.
 */
public class FutureFragment extends Fragment {

    private final CountDownLatch cdl = new CountDownLatch(1);

    private String[] dayNight = new String[5];
    private String[] weatherType = new String[5];

    static FutureFragment create(){
        FutureFragment f = new FutureFragment();

        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String[] data = getResources().getStringArray(R.array.days);

        View layout = inflater.inflate(R.layout.fragment_future, container, false);

        int back = Objects.requireNonNull(getActivity()).getWindow().getStatusBarColor();
        layout.setBackgroundColor(back);

        String url = urlRequest(Objects.requireNonNull(BaseActivity.mSettings.getString(BaseActivity.CHOSEN_CITY, "")));

        setWeather(url);

        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        initRecyclerView(data, dayNight, weatherType, layout);

        return layout;
    }

    private void initRecyclerView(String[] data, String[] dayNight, String[] weatherType, View layout){
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

    private void setWeather(String url){

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
//                        final String result = in.lines().collect(Collectors.joining("\n"));
                        final String result = in.readLine();
                        Gson gson = new Gson();
                        final FutureWeatherRequest futureWeatherRequest = gson.fromJson(result, FutureWeatherRequest.class);
                        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true)){
                            for (int i = 0; i < 5; i++) {
                                dayNight[i] = String.format("%.0f °C", futureWeatherRequest.getList()[i].getMain().getTemp_max()) + " | " + String.format("%.0f °C", futureWeatherRequest.getList()[i].getMain().getTemp_min());
                            }
                        } else {
                            for (int i = 0; i < 5; i++) {
                                dayNight[i] = String.format("%.0f °F", ((futureWeatherRequest.getList()[i].getMain().getTemp_max())*1.8) + 32) + " | " + String.format("%.0f °F", ((futureWeatherRequest.getList()[i].getMain().getTemp_min())*1.8) + 32);
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            weatherType[i] = futureWeatherRequest.getList()[i].getWeather()[0].getMain();
                        }
                        cdl.countDown();
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

    private String urlRequest(String cityName){

        String url = null;

        switch (cityName) {
            case "Moscow":
            case "Москва":
                url = WeatherData.FUTURE_MOSCOW_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Kaliningrad":
            case "Калининград":
                url = WeatherData.FUTURE_Kaliningrad_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Saint Petersburg":
            case "Санкт-Петербург":
                url = WeatherData.FUTURE_Saint_Petersburg_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Novosibirsk":
            case "Новосибирск":
                url = WeatherData.FUTURE_Novosibirsk_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Krasnoyarsk":
            case "Красноярск":
                url = WeatherData.FUTURE_Krasnoyarsk_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Krasnodar":
            case "Краснодар":
                url = WeatherData.FUTURE_Krasnodar_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Arkhangelsk":
            case "Архангельск":
                url = WeatherData.FUTURE_Arkhangelsk_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
        }

        return url;
    }
}

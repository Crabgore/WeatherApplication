package com.geekbrains.android_1.weatherapplication.rest;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OpenWeatherRepo {
    private static OpenWeatherRepo singleton = null;
    private CIOpenWeather cAPI;
    private FIOpenWeather fAPI;

    private OpenWeatherRepo() {
        cAPI = createCAdapter();
        fAPI = createFAdapter();
    }

    public static OpenWeatherRepo getSingleton() {
        if(singleton == null) {
            singleton = new OpenWeatherRepo();
        }

        return singleton;
    }

    public CIOpenWeather getCAPI() {
        return cAPI;
    }

    public FIOpenWeather getFAPI() {
        return fAPI;
    }

    private CIOpenWeather createCAdapter() {
        Retrofit adapter = buildAdapter();
        return adapter.create(CIOpenWeather.class);
    }

    private FIOpenWeather createFAdapter() {
        Retrofit adapter = buildAdapter();
        return adapter.create(FIOpenWeather.class);
    }

    private Retrofit buildAdapter() {
        return new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
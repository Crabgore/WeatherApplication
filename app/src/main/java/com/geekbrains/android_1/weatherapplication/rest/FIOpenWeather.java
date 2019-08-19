package com.geekbrains.android_1.weatherapplication.rest;

import com.geekbrains.android_1.weatherapplication.rest.forecastRest.entities.WeatherForecastRequestRestModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FIOpenWeather {
    @GET("data/2.5/forecast")
    Call<WeatherForecastRequestRestModel> loadWeather(@Query("q") String city,
                                                      @Query("appid") String keyApi);
}
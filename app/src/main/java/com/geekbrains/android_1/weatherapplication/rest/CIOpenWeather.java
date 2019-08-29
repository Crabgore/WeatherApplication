package com.geekbrains.android_1.weatherapplication.rest;

import com.geekbrains.android_1.weatherapplication.rest.currentRest.entities.WeatherRequestRestModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CIOpenWeather {
    @GET("data/2.5/weather")
    Call<WeatherRequestRestModel> loadWeather(@Query("q") String city,
                                              @Query("appid") String keyApi);
}

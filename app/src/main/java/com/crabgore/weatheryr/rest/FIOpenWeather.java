package com.crabgore.weatheryr.rest;

import com.crabgore.weatheryr.rest.forecastRest.entities.WeatherForecastRequestRestModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FIOpenWeather {
    @GET("data/2.5/forecast")
    Call<WeatherForecastRequestRestModel> loadWeather(@Query("q") String city,
                                                      @Query("appid") String keyApi);
}
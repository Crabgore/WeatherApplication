package com.geekbrains.android_1.weatherapplication.rest.forecastRest.entities;

import com.google.gson.annotations.SerializedName;

public class WeatherForecastRequestRestModel {
    @SerializedName("list") public ListRestModel[] list;
}

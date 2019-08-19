package com.geekbrains.android_1.weatherapplication.rest.forecastRest.entities;

import com.google.gson.annotations.SerializedName;

public class ListRestModel {
    @SerializedName("weather") public WeatherRestModel[] weather;
    @SerializedName("main") public MainRestModel main;
}

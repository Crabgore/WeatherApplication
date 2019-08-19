package com.geekbrains.android_1.weatherapplication.rest.forecastRest.entities;

import com.google.gson.annotations.SerializedName;

public class MainRestModel {
    @SerializedName("temp_min") public float tempMin;
    @SerializedName("temp_max") public float tempMax;
}

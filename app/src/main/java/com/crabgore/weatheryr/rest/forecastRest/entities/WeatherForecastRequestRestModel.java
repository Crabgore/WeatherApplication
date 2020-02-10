package com.crabgore.weatheryr.rest.forecastRest.entities;

import com.google.gson.annotations.SerializedName;

public class WeatherForecastRequestRestModel {
    @SerializedName("list") public ListRestModel[] list;
}

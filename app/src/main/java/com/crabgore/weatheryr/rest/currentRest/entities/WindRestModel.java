package com.crabgore.weatheryr.rest.currentRest.entities;

import com.google.gson.annotations.SerializedName;

public class WindRestModel {
    @SerializedName("speed") public float speed;
    @SerializedName("deg") public float deg;
}

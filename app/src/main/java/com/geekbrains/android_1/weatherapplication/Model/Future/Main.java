package com.geekbrains.android_1.weatherapplication.Model.Future;

public class Main {

    private float temp_min;

    private float temp_max;

    public float getTemp_min ()
    {
        return temp_min - 273;
    }

    public void setTemp_min (float temp_min)
    {
        this.temp_min = temp_min;
    }

    public float getTemp_max ()
    {
        return temp_max - 273;
    }

    public void setTemp_max (float temp_max)
    {
        this.temp_max = temp_max;
    }
}

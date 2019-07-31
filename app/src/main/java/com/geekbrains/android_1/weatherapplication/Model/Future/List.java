package com.geekbrains.android_1.weatherapplication.Model.Future;

public class List {

    private Weather[] weather;

    private Main main;

    public Weather[] getWeather ()
    {
        return weather;
    }

    public void setWeather (Weather[] weather)
    {
        this.weather = weather;
    }

    public Main getMain ()
    {
        return main;
    }

    public void setMain (Main main)
    {
        this.main = main;
    }

}

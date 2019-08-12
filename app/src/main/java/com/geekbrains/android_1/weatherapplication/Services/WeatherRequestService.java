package com.geekbrains.android_1.weatherapplication.Services;


import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;
import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.Fragments.WeatherFragment;
import com.geekbrains.android_1.weatherapplication.Model.Current.WeatherRequest;
import com.geekbrains.android_1.weatherapplication.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

@SuppressLint("Registered")
public class WeatherRequestService extends IntentService {
    public WeatherRequestService() { super("WeatherRequestService"); }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String temperature = null;
        String willBe = null;
        String windSpeedValue = null;
        String windSpeedUnits = null;
        String weatherType = null;
        String pressureValue = null;
        String humidityValue = null;
        int weatherImage = 0;
        int layout = 0;

        try {
            assert intent != null;
            final URL uri = new URL(Objects.requireNonNull(intent.getExtras()).getString("city_name"));
            try {
                HttpsURLConnection urlConnection;
                urlConnection = (HttpsURLConnection) uri.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//                       final String result = in.lines().collect(Collectors.joining("\n"));
                final String result = in.readLine();
                Gson gson = new Gson();
                final WeatherRequest weatherRequest = gson.fromJson(result, WeatherRequest.class);
                if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true)){
                    temperature = String.format("%.1f °C", weatherRequest.getMain().getTemp());
                    willBe = getResources().getString(R.string.day) + " " + String.format("%.1f °C", weatherRequest.getMain().getTemp_max()) + " | " + getResources().getString(R.string.night) + " " + String.format("%.1f °C", weatherRequest.getMain().getTemp_min());
                } else {
                    temperature = String.format("%.1f °F", ((weatherRequest.getMain().getTemp())*1.8)+32);
                    willBe = getResources().getString(R.string.day) + " " + String.format("%.1f °F", ((weatherRequest.getMain().getTemp())*1.8) + 32) + " | " + getResources().getString(R.string.night) + " " + String.format("%.1f °F", ((weatherRequest.getMain().getTemp())*1.8)+32);
                }
                if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_WIND_SPEED_UNIT, true)){
                    windSpeedValue = String.format("%.0f", (Float)weatherRequest.getWind().getSpeed());
                    windSpeedUnits = getResources().getString(R.string.wind_speed_count_1);
                } else  {
                    windSpeedValue = String.valueOf((int)(weatherRequest.getWind().getSpeed()*3.6));
                    windSpeedUnits = getResources().getString(R.string.wind_speed_count_2);
                }
                switch (weatherRequest.getWeather()[0].getMain()) {
                    case "Clouds":
                        weatherImage = R.drawable.cloud;
                        weatherType = getResources().getString(R.string.cloud);
                        layout = R.drawable.cloudy;
                        break;
                    case "Clear":
                        weatherImage = R.drawable.sun;
                        weatherType = getResources().getString(R.string.clear);
                        layout = R.drawable.sunny;
                        break;
                    case "Rain":
                        weatherImage = R.drawable.rain;
                        weatherType = getResources().getString(R.string.rain);
                        layout = R.drawable.rainy;
                        break;
                    case "Mist":
                    case "Fog":
                        weatherImage = R.drawable.mist;
                        weatherType = getResources().getString(R.string.mist);
                        layout = R.drawable.misty;
                        break;
                }
                pressureValue = ((Integer)weatherRequest.getMain().getPressure()).toString();
                humidityValue = ((Integer)weatherRequest.getMain().getHumidity()).toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Intent broadcastIntent = new Intent(WeatherFragment.BROADCAST_ACTION);

        broadcastIntent.putExtra("temperature", temperature);
        broadcastIntent.putExtra("willBe", willBe);
        broadcastIntent.putExtra("windSpeedValue", windSpeedValue);
        broadcastIntent.putExtra("windSpeedUnits", windSpeedUnits);
        broadcastIntent.putExtra("weatherType", weatherType);
        broadcastIntent.putExtra("pressureValue", pressureValue);
        broadcastIntent.putExtra("humidityValue", humidityValue);
        broadcastIntent.putExtra("weatherImage", weatherImage);
        broadcastIntent.putExtra("layout", layout);

        sendBroadcast(broadcastIntent);
    }
}

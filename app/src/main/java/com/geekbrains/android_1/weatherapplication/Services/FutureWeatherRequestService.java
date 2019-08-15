package com.geekbrains.android_1.weatherapplication.Services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.Fragments.FutureFragment;
import com.geekbrains.android_1.weatherapplication.Model.Future.FutureWeatherRequest;
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
public class FutureWeatherRequestService extends IntentService {
    public FutureWeatherRequestService (){ super("FutureWeatherRequestService"); }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String[] dayNight = new String[5];
        String[] weatherType = new String[5];

        try {
            assert intent != null;
            final URL uri = new URL(Objects.requireNonNull(intent.getExtras()).getString("city_name"));
                    try {
                        HttpsURLConnection urlConnection;
                        urlConnection = (HttpsURLConnection) uri.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setReadTimeout(10000);
                        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//                        final String result = in.lines().collect(Collectors.joining("\n"));
                        final String result = in.readLine();
                        Gson gson = new Gson();
                        final FutureWeatherRequest futureWeatherRequest = gson.fromJson(result, FutureWeatherRequest.class);
                        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true)){
                            for (int i = 0; i < 5; i++) {
                                dayNight[i] = String.format("%.0f °C", futureWeatherRequest.getList()[i].getMain().getTemp_max()) + " | " + String.format("%.0f °C", futureWeatherRequest.getList()[i].getMain().getTemp_min());
                            }
                        } else {
                            for (int i = 0; i < 5; i++) {
                                dayNight[i] = String.format("%.0f °F", ((futureWeatherRequest.getList()[i].getMain().getTemp_max())*1.8) + 32) + " | " + String.format("%.0f °F", ((futureWeatherRequest.getList()[i].getMain().getTemp_min())*1.8) + 32);
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            weatherType[i] = futureWeatherRequest.getList()[i].getWeather()[0].getMain();
                        }
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Intent broadcastIntent = new Intent(FutureFragment.FUTURE_BROADCAST_ACTION);

        broadcastIntent.putExtra("dayNight", dayNight);
        broadcastIntent.putExtra("weatherType", weatherType);

        sendBroadcast(broadcastIntent);
    }
}

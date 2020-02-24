package com.crabgore.weatheryr.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.crabgore.weatheryr.MainActivity;
import com.crabgore.weatheryr.R;
import com.crabgore.weatheryr.rest.OpenWeatherRepo;
import com.crabgore.weatheryr.rest.currentRest.entities.WeatherRequestRestModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Widget extends AppWidgetProvider {
    public static String ACTION_WIDGET_START = "ActionStartWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        startActivityOnUpdate(context, appWidgetManager, appWidgetIds);

        refreshOnUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), Widget.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

            onUpdate(context, appWidgetManager, appWidgetIds);
        }

        super.onReceive(context, intent);
    }

    private void startActivityOnUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews startActivity = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent startActivityIntent = new Intent(context, MainActivity.class);
        startActivityIntent.setAction(ACTION_WIDGET_START);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, 0);
        startActivity.setOnClickPendingIntent(R.id.mainWid, configPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, startActivity);
    }

    private void refreshOnUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews refresh = new RemoteViews(context.getPackageName(), R.layout.widget);
        Intent intent = new Intent(context, Widget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        refresh.setOnClickPendingIntent(R.id.refresh_btn, actionPendingIntent);

        try {
            JSONObject jsonObject = new JSONObject(loadJSONFromAsset(context));

            OpenWeatherRepo.getSingleton().getCAPI().loadWeather((String) jsonObject.get("CityName"), com.crabgore.weatheryr.BuildConfig.WEATHER_API_KEY)
                    .enqueue(new Callback<WeatherRequestRestModel>() {
                        @Override
                        public void onResponse(@NonNull Call<WeatherRequestRestModel> call,
                                               @NonNull Response<WeatherRequestRestModel> response) {
                            if (response.body() != null && response.isSuccessful()) {
                                setWeather(response.body(), refresh, appWidgetManager, appWidgetIds);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<WeatherRequestRestModel> call, @NonNull Throwable t) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    private void setWeather(WeatherRequestRestModel body, RemoteViews refresh, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        refresh.setTextViewText(R.id.day, checkDate());
        refresh.setTextViewText(R.id.curTemp, String.format("%.0f °C", body.main.temp - 273));
        switch (body.weather[0].main) {
            case "Clouds":
                refresh.setImageViewResource(R.id.image_weather_type, R.drawable.cloud);
                break;
            case "Clear":
                refresh.setImageViewResource(R.id.image_weather_type, R.drawable.sun);
                break;
            case "Rain":
            case "Drizzle":
                refresh.setImageViewResource(R.id.image_weather_type, R.drawable.rain);
                break;
            case "Mist":
            case "Fog":
                refresh.setImageViewResource(R.id.image_weather_type, R.drawable.mist);
                break;
            case "Snow":
                refresh.setImageViewResource(R.id.image_weather_type, R.drawable.snow);
                break;
        }
        appWidgetManager.updateAppWidget(appWidgetIds, refresh);
    }

    private String checkDate() {
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MM", Locale.getDefault());
        return dateFormat.format(currentDate);
    }

    public String loadJSONFromAsset(Context context) {
        String json;
        try {
            InputStream is = new FileInputStream(context.getFilesDir() + File.separator + "cityname.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}

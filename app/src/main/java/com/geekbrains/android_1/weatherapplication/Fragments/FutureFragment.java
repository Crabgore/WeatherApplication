package com.geekbrains.android_1.weatherapplication.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.Adapters.FutureAdapter;
import com.geekbrains.android_1.weatherapplication.BuildConfig;
import com.geekbrains.android_1.weatherapplication.ExampleItem;
import com.geekbrains.android_1.weatherapplication.R;
import com.geekbrains.android_1.weatherapplication.Services.FutureWeatherRequestService;
import com.geekbrains.android_1.weatherapplication.WeatherData;

import java.util.ArrayList;
import java.util.Objects;

public class FutureFragment extends Fragment {

    private FutureAdapter futureAdapter;

    public final static String FUTURE_BROADCAST_ACTION = "my_future_weather_application_2";
    private ServiceFinishedReceiver receiver = new ServiceFinishedReceiver();

    private ArrayList<ExampleItem> mExampleList;

    static FutureFragment create(){
        FutureFragment f = new FutureFragment();

        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getContext());

        View layout = inflater.inflate(R.layout.fragment_future, container, false);

        createBackground(layout);

        String url = urlRequest(Objects.requireNonNull(mSettings.getString(BaseActivity.CHOSEN_CITY, "")));

        setWeather(url);

        createExampleList();

        initRecyclerView(mExampleList, layout);

        Objects.requireNonNull(getActivity()).registerReceiver(receiver, new IntentFilter(FUTURE_BROADCAST_ACTION));

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).registerReceiver(receiver, new IntentFilter(FUTURE_BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getActivity()).unregisterReceiver(receiver);
    }

    private void createBackground(View layout) {
        int back = Objects.requireNonNull(getActivity()).getWindow().getStatusBarColor();
        layout.setBackgroundColor(back);
    }

    private void setWeather(String url){
        Intent intent = new Intent(getActivity(), FutureWeatherRequestService.class);
        intent.putExtra("city_name", url);
        Objects.requireNonNull(getActivity()).startService(intent);
    }

    private void createExampleList(){
        mExampleList = new ArrayList<>();
    }

    private void initRecyclerView(ArrayList<ExampleItem> mExampleList, View layout){
        RecyclerView recyclerView = layout.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        futureAdapter = new FutureAdapter(mExampleList);
        recyclerView.setAdapter(futureAdapter);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(layout.getContext(), LinearLayoutManager.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.separator));
        recyclerView.addItemDecoration(itemDecoration);
    }

    private String urlRequest(String cityName){

        String url = null;

        switch (cityName) {
            case "Moscow":
            case "Москва":
                url = WeatherData.FUTURE_MOSCOW_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Kaliningrad":
            case "Калининград":
                url = WeatherData.FUTURE_Kaliningrad_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Saint Petersburg":
            case "Санкт-Петербург":
                url = WeatherData.FUTURE_Saint_Petersburg_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Novosibirsk":
            case "Новосибирск":
                url = WeatherData.FUTURE_Novosibirsk_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Krasnoyarsk":
            case "Красноярск":
                url = WeatherData.FUTURE_Krasnoyarsk_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Krasnodar":
            case "Краснодар":
                url = WeatherData.FUTURE_Krasnodar_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
            case "Arkhangelsk":
            case "Архангельск":
                url = WeatherData.FUTURE_Arkhangelsk_WEATHER_URL + BuildConfig.WEATHER_API_KEY;
                break;
        }

        return url;
    }

    private class ServiceFinishedReceiver extends BroadcastReceiver {
        String[] data;
        String[] dayNight;
        String[] weatherType;

        @Override
        public void onReceive(Context context, Intent intent) {
            data = getResources().getStringArray(R.array.days);
            dayNight = intent.getStringArrayExtra("dayNight");
            weatherType = intent.getStringArrayExtra("weatherType");

            mExampleList.clear();

            for (int i = 0; i < 5; i++) {
                mExampleList.add(new ExampleItem(data[i], dayNight[i], weatherType[i]));

            }

            futureAdapter.notifyDataSetChanged();
        }
    }
}

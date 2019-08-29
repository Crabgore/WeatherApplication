package com.geekbrains.android_1.weatherapplication.Fragments;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import com.geekbrains.android_1.weatherapplication.Activities.BaseActivity;
import com.geekbrains.android_1.weatherapplication.Adapters.FutureAdapter;
import com.geekbrains.android_1.weatherapplication.BuildConfig;
import com.geekbrains.android_1.weatherapplication.ExampleItem;
import com.geekbrains.android_1.weatherapplication.R;
import com.geekbrains.android_1.weatherapplication.rest.OpenWeatherRepo;
import com.geekbrains.android_1.weatherapplication.rest.forecastRest.entities.ListRestModel;
import com.geekbrains.android_1.weatherapplication.rest.forecastRest.entities.WeatherForecastRequestRestModel;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FutureFragment extends Fragment {

    private FutureAdapter futureAdapter;

    private ArrayList<ExampleItem> mExampleList;

    static FutureFragment create(){
        FutureFragment f = new FutureFragment();

        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getContext());

        View layout = inflater.inflate(R.layout.fragment_future, container, false);

        createBackground(layout);

        setWeather(mSettings.getString(BaseActivity.CHOSEN_CITY, ""));

        createExampleList();

        initRecyclerView(mExampleList, layout);

        return layout;
    }

    private void createBackground(View layout) {
        int back = Objects.requireNonNull(getActivity()).getWindow().getStatusBarColor();
        layout.setBackgroundColor(back);
    }

    private void setWeather(String cityName){
        OpenWeatherRepo.getSingleton().getFAPI().loadWeather(cityName,
                BuildConfig.WEATHER_API_KEY)
                .enqueue(new Callback<WeatherForecastRequestRestModel>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherForecastRequestRestModel> call,
                                           @NonNull Response<WeatherForecastRequestRestModel> response) {
                        if (response.body() != null && response.isSuccessful()) {
                            renderWeather(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherForecastRequestRestModel> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(getContext(), getString(R.string.network_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderWeather(WeatherForecastRequestRestModel body) {
        String[] data = getResources().getStringArray(R.array.days);
        String[] dayNight = setDayNight(body.list);
        String[] weatherType = setWeatherType(body.list);

        mExampleList.clear();

        for (int i = 0; i < 5; i++) {
            mExampleList.add(new ExampleItem(data[i], dayNight[i], weatherType[i]));

        }

        futureAdapter.notifyDataSetChanged();
    }

    @SuppressLint("DefaultLocale")
    private String[] setDayNight(ListRestModel[] list) {
        String[] dayNight = new String[5];
        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true)){
            for (int i = 0; i < 5; i++) {
                dayNight[i] = String.format("%.0f °C", list[i].main.tempMax - 273) + " | "
                        + String.format("%.0f °C", list[i].main.tempMin - 273);
            }
        } else {
            for (int i = 0; i < 5; i++) {
                dayNight[i] = String.format("%.0f °F", ((list[i].main.tempMax - 273)*1.8) + 32)
                        + " | " + String.format("%.0f °F", ((list[i].main.tempMin - 273)*1.8) + 32);
            }
        }
        return dayNight;
    }

    private String[] setWeatherType(ListRestModel[] list) {
        String[] weatherType = new String[5];
        for (int i = 0; i < 5; i++) {
            weatherType[i] = list[i].weather[0].main;
        }
        return weatherType;
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

        DividerItemDecoration itemDecoration = new DividerItemDecoration(layout.getContext(),
                LinearLayoutManager.VERTICAL);
        itemDecoration.setDrawable(getResources().getDrawable(R.drawable.separator));
        recyclerView.addItemDecoration(itemDecoration);
    }
}

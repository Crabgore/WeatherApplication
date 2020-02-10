package com.crabgore.weatheryr.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.crabgore.weatheryr.ExampleItem;
import com.crabgore.weatheryr.R;
import com.crabgore.weatheryr.activities.BaseActivity;
import com.crabgore.weatheryr.adapters.FutureAdapter;
import com.crabgore.weatheryr.rest.OpenWeatherRepo;
import com.crabgore.weatheryr.rest.forecastRest.entities.ListRestModel;
import com.crabgore.weatheryr.rest.forecastRest.entities.WeatherForecastRequestRestModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FutureFragment extends Fragment {
    private FutureAdapter futureAdapter;
    private ArrayList<ExampleItem> mExampleList;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(getContext());

        View layout = inflater.inflate(R.layout.fragment_future, container, false);

        progressBar = layout.findViewById(R.id.progressBar);

        layout.setBackgroundColor(Objects.requireNonNull(getActivity()).getWindow().getStatusBarColor());

        setWeather(mSettings.getString(BaseActivity.CHOSEN_CITY, ""));

        createExampleList();

        initRecyclerView(mExampleList, layout);

        initSwipe(layout);

        return layout;
    }

    private void initSwipe(View layout) {
        final SwipeRefreshLayout pullToRefresh = layout.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            Objects.requireNonNull(getFragmentManager())
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
            pullToRefresh.setRefreshing(false);
        });
    }

    private void setWeather(String cityName) {
        OpenWeatherRepo.getSingleton().getFAPI().loadWeather(cityName,
                com.crabgore.weatheryr.BuildConfig.WEATHER_API_KEY)
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
        String[] data = generateDates();
        String[] dayNight = setDayNight(body.list);
        String[] weatherType = setWeatherType(body.list);

        mExampleList.clear();

        for (int i = 0; i < 5; i++) {
            mExampleList.add(new ExampleItem(data[i], dayNight[i], weatherType[i]));

        }

        futureAdapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @SuppressLint("DefaultLocale")
    private String[] setDayNight(ListRestModel[] list) {
        String[] dayNight = new String[5];
        if (BaseActivity.mSettings.getBoolean(BaseActivity.APP_PREFERENCES_TEMP_UNIT, true)) {
            for (int i = 0; i < 5; i++) {
                dayNight[i] = String.format("%.0f °C", list[i].main.tempMax - 273) + "\n\n"
                        + String.format("%.0f °C", list[i].main.tempMin - 273);
            }
        } else {
            for (int i = 0; i < 5; i++) {
                dayNight[i] = String.format("%.0f °F", ((list[i].main.tempMax - 273) * 1.8) + 32)
                        + "\n\n" + String.format("%.0f °F", ((list[i].main.tempMin - 273) * 1.8) + 32);
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

    private void createExampleList() {
        mExampleList = new ArrayList<>();
    }

    private void initRecyclerView(ArrayList<ExampleItem> mExampleList, View layout) {
        recyclerView = layout.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        futureAdapter = new FutureAdapter(mExampleList, getContext());
        recyclerView.setAdapter(futureAdapter);
    }

    @SuppressLint("SimpleDateFormat")
    private String[] generateDates() {
        String[] dates = new String[5];
        for (int i = 0; i < 5; i++) {
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.DATE, i + 1);
            dates[i] = new SimpleDateFormat("EEE, dd MMMM").format(cal.getTime());
        }

        return dates;
    }
}

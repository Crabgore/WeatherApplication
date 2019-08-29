package com.geekbrains.android_1.weatherapplication.Adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.geekbrains.android_1.weatherapplication.ExampleItem;
import com.geekbrains.android_1.weatherapplication.R;

import java.util.ArrayList;

public class FutureAdapter extends RecyclerView.Adapter<FutureAdapter.ViewHolder> {
    private ArrayList<ExampleItem> mExampleList;

    public FutureAdapter(ArrayList<ExampleItem> exampleList) {
        mExampleList = exampleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.getDay().setText(mExampleList.get(i).getText1());
        viewHolder.getDayNight().setText(mExampleList.get(i).getText2());
        switch (mExampleList.get(i).getText3()) {
            case "Clouds":
                viewHolder.getImageWeatherType().setImageResource(R.drawable.cloud);
                viewHolder.getTextWeatherType().setText(R.string.cloud);
                break;
            case "Clear":
                viewHolder.getImageWeatherType().setImageResource(R.drawable.sun);
                viewHolder.getTextWeatherType().setText(R.string.clear);
                break;
            case "Rain":
                viewHolder.getImageWeatherType().setImageResource(R.drawable.rain);
                viewHolder.getTextWeatherType().setText(R.string.rain);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mExampleList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView day;
        private TextView dayNight;
        private ImageView imageWeatherType;
        private TextView textWeatherType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.day);
            dayNight = itemView.findViewById(R.id.day_night);
            imageWeatherType = itemView.findViewById(R.id.image_weather_type);
            textWeatherType = itemView.findViewById(R.id.text_weather_type);
        }

        TextView getDay() {
            return day;
        }

        TextView getDayNight() {
            return dayNight;
        }

        ImageView getImageWeatherType() {
            return imageWeatherType;
        }

        TextView getTextWeatherType() {
            return textWeatherType;
        }
    }
}
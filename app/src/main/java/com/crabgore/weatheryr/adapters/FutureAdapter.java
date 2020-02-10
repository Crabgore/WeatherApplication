package com.crabgore.weatheryr.adapters;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crabgore.weatheryr.ExampleItem;
import com.crabgore.weatheryr.R;

import java.util.ArrayList;


public class FutureAdapter extends RecyclerView.Adapter<FutureAdapter.ViewHolder> {
    private ArrayList<ExampleItem> mExampleList;
    private Context context;

    public FutureAdapter(ArrayList<ExampleItem> exampleList, Context context) {
        mExampleList = exampleList;
        this.context = context;
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
                viewHolder.cardView.setBackground(context.getDrawable(R.drawable.cloudy));
                break;
            case "Clear":
                viewHolder.getImageWeatherType().setImageResource(R.drawable.sun);
                viewHolder.getTextWeatherType().setText(R.string.clear);
                viewHolder.cardView.setBackground(context.getDrawable(R.drawable.cleary));
                break;
            case "Rain":
                viewHolder.getImageWeatherType().setImageResource(R.drawable.rain);
                viewHolder.getTextWeatherType().setText(R.string.rain);
                viewHolder.cardView.setBackground(context.getDrawable(R.drawable.rainy));
                break;
            case "Mist":
                viewHolder.getImageWeatherType().setImageResource(R.drawable.mist);
                viewHolder.getTextWeatherType().setText(R.string.mist);
                viewHolder.cardView.setBackground(context.getDrawable(R.drawable.misty));
                break;
            case "Fog":
                viewHolder.getImageWeatherType().setImageResource(R.drawable.mist);
                viewHolder.getTextWeatherType().setText(R.string.mist);
                viewHolder.cardView.setBackground(context.getDrawable(R.drawable.foggy));
            case "Drizzle":
                viewHolder.getImageWeatherType().setImageResource(R.drawable.rain);
                viewHolder.getTextWeatherType().setText(R.string.drizzle);
                viewHolder.cardView.setBackground(context.getDrawable(R.drawable.drizzly));
                break;
            case "Snow":
                viewHolder.getImageWeatherType().setImageResource(R.drawable.snow);
                viewHolder.getTextWeatherType().setText(R.string.snow);
                viewHolder.cardView.setBackground(context.getDrawable(R.drawable.snowy));
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
        private CardView cardView;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.day);
            dayNight = itemView.findViewById(R.id.day_night);
            imageWeatherType = itemView.findViewById(R.id.image_weather_type);
            textWeatherType = itemView.findViewById(R.id.text_weather_type);
            cardView = itemView.findViewById(R.id.cardView);
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
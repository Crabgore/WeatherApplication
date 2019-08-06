package com.geekbrains.android_1.weatherapplication.Fragments;


import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geekbrains.android_1.weatherapplication.R;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_about, container, false);

        Drawable back = Objects.requireNonNull(getActivity()).getWindow().getDecorView().getBackground();
        layout.setBackground(back);

        return layout;
    }
}

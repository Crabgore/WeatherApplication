package com.geekbrains.android_1.weatherapplication.Fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.geekbrains.android_1.weatherapplication.R;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class ShareFragment extends Fragment {

    private EditText msg;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.fragment_share, container, false);

        Drawable back = Objects.requireNonNull(getActivity()).getWindow().getDecorView().getBackground();
        layout.setBackground(back);

        msg = layout.findViewById(R.id.msg);
        MaterialButton send = layout.findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msg.getText().toString().equals("")){
                    Toast.makeText(getContext(), getResources().getString(R.string.insert_message), Toast.LENGTH_SHORT).show();
                } else {
                    msg.getText().clear();
                    Toast.makeText(getContext(), getResources().getString(R.string.sent), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return layout;
    }
}

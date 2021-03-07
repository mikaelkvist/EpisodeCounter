package com.example.episode_counter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set theme to the default (change from splash screen)
        setTheme(R.style.Theme_SeriesTracker);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}
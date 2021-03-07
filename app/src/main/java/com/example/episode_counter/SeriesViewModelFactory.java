package com.example.episode_counter;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SeriesViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {

    private final Application mApplication;


    public SeriesViewModelFactory(@NonNull Application application) {
        super(application);
        mApplication = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SeriesViewModel(mApplication);
    }
}


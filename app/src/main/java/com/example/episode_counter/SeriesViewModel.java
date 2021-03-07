package com.example.episode_counter;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/*
SeriesViewModel is responsible for keeping the loaded series loaded when the fragments and
activities are detached and paused (this is done by extending AndroidViewModel).
It is also responsible for keeping track of selections and scroll values for RecyclerView.
 */
public class SeriesViewModel extends AndroidViewModel {

    private SeriesRepository mRepository;
    private LiveData<List<Series>> mSeries;

    // Values for recreating RecyclerViews
    private int mLayoutFirstVisibleItem;
    private int mLayoutOffset;

    // Selected series that might be deleted
    private List<Series> mDeleteSelected;
    // Using MutableLiveData so it will be easier to implement layout for tablets with different
    // fragments side by side
    private final MutableLiveData<Series> mSelected = new MutableLiveData<>();

    public SeriesViewModel(@NonNull Application application) {
        super(application);
        mRepository = new SeriesRepository(application);
        mSeries = mRepository.getSeries();
        mSelected.setValue(null);
        mDeleteSelected = new ArrayList<>();
        mLayoutFirstVisibleItem = -1;
        mLayoutOffset = -1;
    }

    public void storeLayoutPosition(int firstVisibleItem, int offset) {
        mLayoutFirstVisibleItem = firstVisibleItem;
        mLayoutOffset = offset;
    }

    public int getLayoutFirstVisibleItem() {return mLayoutFirstVisibleItem;}
    public int getLayoutOffset() { return mLayoutOffset;}

    public void addDeleteSelection(Series series) {mDeleteSelected.add(series);}
    public void removeDeleteSelection(Series series) {mDeleteSelected.remove(series);}
    public void deleteSelection() { mRepository.delete(mDeleteSelected);}
    public void clearDeleteSelection() {mDeleteSelected.clear();}

    public void exportData(Uri uri) {mRepository.exportData(uri);}
    public void importData(Uri uri) {mRepository.importData(uri);}

    public void select(Series series) {
        mSelected.setValue(series);
    }

    public LiveData<Series> getSelected() {
        return mSelected;
    }

    public LiveData<List<Series>> getSeries() {return mSeries;}

    public void insert(Series series, Bitmap image) {mRepository.insert(series, image);}

    public void update(Series series) {mRepository.update(series);}
    public void update(Series newSeries, Series oldSeries, boolean deleteOldImage) {mRepository.update(newSeries, oldSeries, deleteOldImage);}
    public void update(Series newSeries, Series oldSeries, Bitmap image) {mRepository.update(newSeries, oldSeries, image);}

    public void showViewIfImageExist(View view, String imageName) {mRepository.showViewIfImageExist(view, imageName);}
    public void loadImage(String seriesTitle, ImageView view) {mRepository.loadImage(seriesTitle, view);}

    public void deleteSeries(Series series) {mRepository.delete(series);}

    public void changeSortOrder(String order) {mRepository.changeSortOrder(order);}

    public String getSortOrder() {return mRepository.getSortOrder();}
}

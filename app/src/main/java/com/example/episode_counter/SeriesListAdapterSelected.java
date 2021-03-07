package com.example.episode_counter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SeriesListAdapterSelected extends ListAdapter<Series, SeriesListAdapterSelected.SeriesViewHolder> {

    private final Fragment mFragment;
    private SeriesViewModel mViewModel;
    private final HashMap<Integer, Boolean> mPositionSelected;

    public SeriesListAdapterSelected(Fragment fragment) {
        super(new SeriesDiffCallBack());
        mFragment = fragment;
        mPositionSelected = new HashMap<>();

    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SeriesViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesViewHolder holder, int position) {
        holder.bind(getItem(position), position, mFragment);

    }

    public void setViewModel(SeriesViewModel viewModel) {
        mViewModel = viewModel;
    }

    class SeriesViewHolder extends RecyclerView.ViewHolder {

        private View selectedOverlay;

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(Series series, int position, Fragment fragment) {
            // ----- If mPositionSelected doesn't have position as key ----
            if (!mPositionSelected.containsKey(position)) {

                // Initially the item that was longclicked will be selected
                if (mViewModel.getSelected().getValue() != null
                        && mViewModel.getSelected().getValue().getTitle().equals(series.getTitle())) {
                    mPositionSelected.put(position, Boolean.TRUE);
                    mViewModel.addDeleteSelection(series);

                    // Unselect the item, it is not needed anymore
                    mViewModel.select(null);
                } else {
                    mPositionSelected.put(position, Boolean.FALSE);
                }
            }

            // Set as selection represented in mPositionSelected
            selectedOverlay = itemView.findViewById(R.id.selected_overlay);
            int visibility = (mPositionSelected.get(position) == Boolean.TRUE) ? View.VISIBLE : View.INVISIBLE;
            selectedOverlay.setVisibility(visibility);

            itemView.setOnClickListener(view -> {
                // Select mode is on: clicking will select item
                if (selectedOverlay.getVisibility() == View.VISIBLE) {
                    selectedOverlay.setVisibility(View.INVISIBLE);
                    mViewModel.removeDeleteSelection(series);
                    mPositionSelected.put(position, Boolean.FALSE);
                } else {
                    selectedOverlay.setVisibility(View.VISIBLE);
                    mViewModel.addDeleteSelection(series);
                    mPositionSelected.put(position, Boolean.TRUE);
                }
            });

            // Change the information fields to the current series
            ((TextView)itemView.findViewById(R.id.title)).setText(series.getTitle());

            String countInfo = "";
            countInfo += (series.getSeason() > 0) ? "S" + series.getSeason() : "";
            if (!countInfo.equals("")) {
                countInfo += " ";
            }
            countInfo += (series.getEpisode() > 0) ? "E" + series.getEpisode() : "";

            ((TextView)itemView.findViewById(R.id.counter)).setText(countInfo);

            ((RatingBar)itemView.findViewById(R.id.ratingbar)).setRating(series.getRating());

            mViewModel.loadImage(series.getTitle(), itemView.findViewById(R.id.image));
        }
    }

    static class SeriesDiffCallBack extends DiffUtil.ItemCallback<Series> {

        @Override
        public boolean areItemsTheSame(@NonNull Series oldItem, @NonNull Series newItem) {
            return oldItem.getTitle().equals(newItem.getTitle());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Series oldItem, @NonNull Series newItem) {
            // how to check if images are the same?
            // maybe this is enough: because images will not have changed when you change sorting order
            // ths only time you change an image is inside an item and then when you exit the item
            // the image will be drawn, right?
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getRating() == newItem.getRating()
                    && oldItem.getEpisode() == newItem.getEpisode()
                    && oldItem.getSeason() == newItem.getSeason();
        }
    }

}

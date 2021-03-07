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
import java.util.List;

public class SeriesListAdapter extends ListAdapter<Series, SeriesListAdapter.SeriesViewHolder> {

    private final Fragment mFragment;
    private SeriesViewModel mViewModel;
    private List<View.OnLongClickListener> mLongClickListeners;
    private List<View.OnClickListener> mClickListeners;

    public SeriesListAdapter(Fragment fragment) {
        super(new SeriesDiffCallBack());
        mFragment = fragment;

    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SeriesViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesViewHolder holder, int position) {
        holder.bind(getItem(position), mFragment);

    }

    public void setViewModel(SeriesViewModel viewModel) {
        mViewModel = viewModel;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        if (mClickListeners == null) {
            mClickListeners = new ArrayList<>();
        }
        mClickListeners.add(listener);
    }

    public void setOnLongClickListener(View.OnLongClickListener listener) {
        if (mLongClickListeners == null) {
            mLongClickListeners = new ArrayList<>();
        }
        mLongClickListeners.add(listener);
    }

    class SeriesViewHolder extends RecyclerView.ViewHolder {

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind(Series series, Fragment fragment) {
            itemView.setOnClickListener(view -> {

                // Normal: clicking on item expands it in new fragment
                mViewModel.select(series);
                if (mClickListeners != null) {
                    for (View.OnClickListener l : mClickListeners) {
                        l.onClick(view);
                    }
                }
            });

            // First longclick will start "SelectionMode"
            itemView.setOnLongClickListener(view -> {
                mViewModel.select(series);
                if (mLongClickListeners != null) {
                    for (View.OnLongClickListener l : mLongClickListeners) {
                        l.onLongClick(view);
                    }
                }
                return true; //callback was consumed
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
            // ths only time you change an image is inside an item (FragmentExpanded) and then when
            // you exit the item the image will be redrawn
            return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getRating() == newItem.getRating()
                    && oldItem.getEpisode() == newItem.getEpisode()
                    && oldItem.getSeason() == newItem.getSeason();
        }
    }

}

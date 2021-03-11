package com.example.episode_counter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;

import io.reactivex.Completable;

public class FragmentEditSeries extends BottomSheetDialogFragment {

    private final int PICK_IMAGE_CODE = 1;
    private ImageView imageView;
    private boolean imageSet = false;
    private boolean removeImage = false;
    private SeriesViewModel mViewModel;
    private Button mRemoveImageButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_series, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SeriesViewModel
        mViewModel = new ViewModelProvider(requireActivity(),
                new SeriesViewModelFactory(requireActivity().getApplication()))
                .get(SeriesViewModel.class);

        // Information fields
        TextView titleView = view.findViewById(R.id.title_textview);
        EditText series_title = view.findViewById(R.id.edit_title);
        RatingBar series_rating = view.findViewById(R.id.ratingbar);
        imageView = view.findViewById(R.id.edit_imageview);
        mRemoveImageButton = view.findViewById(R.id.remove_image);
        Button cancel = view.findViewById(R.id.cancel);
        Button done = view.findViewById(R.id.done);

        // Set information according to the selected series in SeriesViewModel
        Series series = mViewModel.getSelected().getValue();
        if (series == null) {
            titleView.setText("New");
            mRemoveImageButton.setVisibility(View.GONE);
            done.setText("Create");
        } else {
            titleView.setText("Edit: " + series.getTitle());
            series_rating.setRating(series.getRating());
            mViewModel.loadImage(series.getTitle(), imageView);
            mViewModel.showViewIfImageExist(mRemoveImageButton, series.getTitle());
            done.setText("Save");
        }

        // ----- Set up the clickListeners ----

        // ImageView  (pick image)
        imageView.setOnClickListener(view1 -> startActivityForResult(
                new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), PICK_IMAGE_CODE));
        mRemoveImageButton.setOnClickListener(view14 -> {
            imageView.setImageResource(R.drawable.ic_image_24);
            imageSet = false;
            removeImage = true;
            mRemoveImageButton.setVisibility(View.GONE);
        });

        // Cancel button (exit without saving)
        cancel.setOnClickListener(view12 -> NavHostFragment.findNavController(FragmentEditSeries.this)
                .navigate(R.id.action_cancel));

        // Done button  (save and exit)
        done.setOnClickListener(view13 -> {

            // Need to specify title
            String newTitle = series_title.getText().toString();
            if (newTitle.equals("") && series == null) {
                Toast.makeText(getActivity(), "Please enter title.", Toast.LENGTH_SHORT).show();
            } else {
                if (newTitle.equals("") && series != null) {
                    // in case no title was entered, use the old one
                    newTitle = (newTitle.equals("")) ? series.getTitle() : newTitle;
                }

                Bitmap image = null;
                if (imageSet) {
                    image = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                }

                Series seriesToSave = new Series(newTitle, series_rating.getRating());
                if (series != null) {
                    // ----- We are editing an existing series -----
                    // transfer the counters
                    seriesToSave.setSeason(series.getSeason());
                    seriesToSave.setEpisode(series.getEpisode());

                    if (removeImage) {
                        mViewModel.update(seriesToSave, series, removeImage);
                    } else {
                        // Second argument is the old series we are updating
                        mViewModel.update(seriesToSave, series, image);
                    }

                    // We came from FragmentExpanded since we are updating and we need to select the
                    // new series since it contains all the new information we changed to
                    mViewModel.select(seriesToSave);

                } else {
                    // We are creating a new series
                    mViewModel.insert(seriesToSave, image);
                }

                // Close BottomSheetDialogFragment no matter the result
                NavHostFragment.findNavController(FragmentEditSeries.this)
                        .navigate(R.id.action_cancel);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_CODE) {
            try {

                Bitmap image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                imageView.setImageBitmap(image);
                imageSet = true;
                removeImage = false;
                mRemoveImageButton.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                Toast.makeText(getActivity(), "Failed to fetch image.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

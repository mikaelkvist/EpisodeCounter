package com.example.episode_counter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FragmentExpanded extends Fragment {

    private SeriesViewModel mViewModel;
    private Series selectedSeries;
    private TextView season_view;
    private TextView episode_view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // SeriesViewModel is shared between all fragment since we are using the Activity as the
        // owner (i.e. requireActivity())
        mViewModel = new ViewModelProvider(requireActivity(),
                new SeriesViewModelFactory(requireActivity().getApplication()))
                .get(SeriesViewModel.class);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.item_expanded, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ----- Get views -----
        season_view = view.findViewById(R.id.season_text);
        episode_view = view.findViewById(R.id.episode_text);

        // Set selected series initially
        setSelectedSeries(mViewModel.getSelected().getValue(), view);

        // Update 'selected' and all the views whenever selectedSeries changes
        mViewModel.getSelected().observe(requireActivity(), series -> setSelectedSeries(series, view));

        // ----- Setup the buttons -----
        // Up Button: exit this fragment
        view.findViewById(R.id.toolbar_up_button).setOnClickListener(view1 ->
                NavHostFragment.findNavController(FragmentExpanded.this)
                        .navigate(R.id.action_cancel));

        // ----- Counters ------
        // Season Left: decrease season count
        view.findViewById(R.id.season_decrease).setOnClickListener(view1 -> newSeasonCount(selectedSeries.getSeason()-1));
        // Season Right: increase season count
        view.findViewById(R.id.season_increase).setOnClickListener(view1 -> newSeasonCount(selectedSeries.getSeason()+1));
        // Season text: open dialog
        view.findViewById(R.id.season_text).setOnClickListener(view1 -> createNumberInputDialog("season"));


        // Episode Left: decrease episode count
        view.findViewById(R.id.episode_decrease).setOnClickListener(view1 -> newEpisodeCount(selectedSeries.getEpisode()-1));
        // Episode Right: increase episode count
        view.findViewById(R.id.episode_increase).setOnClickListener(view1 -> newEpisodeCount(selectedSeries.getEpisode()+1));
        // Episode text: open dialog
        view.findViewById(R.id.episode_text).setOnClickListener(view1 -> createNumberInputDialog("episode"));


        // ----- Toolbar buttons -----
        // Edit
        view.findViewById(R.id.edit).setOnClickListener(view1 ->
                NavHostFragment.findNavController(FragmentExpanded.this)
                        .navigate(R.id.action_fragmentExpanded_to_fragmentEditSeries));
        // Delete
        view.findViewById(R.id.delete).setOnClickListener(view1 -> createDeleteDialog());
    }

    private void createDeleteDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(R.string.delete + " " + selectedSeries.getTitle() + "?");
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {});
        builder.setPositiveButton(R.string.delete, (dialogInterface, i) -> {
            mViewModel.deleteSeries(selectedSeries);
            mViewModel.select(null);
            NavHostFragment.findNavController(FragmentExpanded.this).navigate(R.id.action_cancel);
                });
        builder.create();
        builder.show();
    }

    private void createNumberInputDialog(String type) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.number_input, null);
        EditText numberInput = view.findViewById(R.id.number_input);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setView(view);
        builder.setTitle(getString(R.string.enter) + " " + type + " " + getString(R.string.number) + ":");
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {});
        builder.setPositiveButton(R.string.OK, (dialogInterface, i) ->
                newCountTest(type, numberInput.getText().toString()));
        builder.create().show();

    }

    private void newCountTest(String type, String input) {
        Integer count = null;
        try {
            count = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), R.string.value_out_of_range, Toast.LENGTH_SHORT).show();
        }
        if (count != null) {
            if (type.equals("season")) {
                newSeasonCount(count);
            } else if (type.equals("episode")) {
                newEpisodeCount(count);
            }
        }
    }

    private void newSeasonCount(int newCount) {
        if (newCount == Integer.MIN_VALUE) {
            // happens if increasing when count is MAX_VALUE (loops around)
            // reset the count to MAX_VALUE
            newCount = Integer.MAX_VALUE;
        } else {
            newCount = Math.max(newCount, 0);
            newCount = Math.min(newCount, Integer.MAX_VALUE);
        }
        selectedSeries.setSeason(newCount);
        mViewModel.update(selectedSeries);
        setSeasonText();
    }

    private void newEpisodeCount(int newCount) {
        if (newCount == Integer.MIN_VALUE) {
            // happens if increasing when count is MAX_VALUE (loops around)
            // reset the count to MAX_VALUE
            newCount = Integer.MAX_VALUE;
        } else {
            newCount = Math.max(newCount, 0);
            newCount = Math.min(newCount, Integer.MAX_VALUE);
        }
        selectedSeries.setEpisode(newCount);
        mViewModel.update(selectedSeries);
        setEpisodeText();
    }

    private void setSeasonText() {
        String text = getString(R.string.season) + ": " + selectedSeries.getSeason();
        season_view.setText(text);
    }

    private void setEpisodeText() {
        String text = getString(R.string.episode) +  ": " + selectedSeries.getEpisode();
        episode_view.setText(text);
    }

    private void setSelectedSeries(Series selected, View view) {
        // Start value
        selectedSeries = selected;

        // Set values for selected series
        if (selectedSeries != null) {
            ((TextView) view.findViewById(R.id.toolbar_title)).setText(selectedSeries.getTitle());
            mViewModel.loadImage(selectedSeries.getTitle(),view.findViewById(R.id.image));

            setSeasonText();
            setEpisodeText();

        }
    }


}

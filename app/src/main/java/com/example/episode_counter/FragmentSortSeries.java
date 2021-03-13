package com.example.episode_counter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FragmentSortSeries extends BottomSheetDialogFragment {

    private SeriesViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sort_series, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SeriesViewModel
        mViewModel = new ViewModelProvider(requireActivity(),
                new SeriesViewModelFactory(requireActivity().getApplication()))
                .get(SeriesViewModel.class);

        // Information fields
        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        RadioButton title_asc   = view.findViewById(R.id.title_asc);
        RadioButton title_desc  = view.findViewById(R.id.title_desc);
        RadioButton rating_high = view.findViewById(R.id.rating_high);
        RadioButton rating_low  = view.findViewById(R.id.rating_low);
        RadioButton date_new    = view.findViewById(R.id.date_new);
        RadioButton date_old    = view.findViewById(R.id.date_old);
        Button cancel           = view.findViewById(R.id.cancel);

        // Select the current sorting order
        String sortOrder = mViewModel.getSortOrder();
        if (sortOrder.equals("title_ASC"))          title_asc.setChecked(true);
        else if (sortOrder.equals("title_DESC"))    title_desc.setChecked(true);
        else if (sortOrder.equals("rating_high"))   rating_high.setChecked(true);
        else if (sortOrder.equals("rating_low"))    rating_low.setChecked(true);
        else if (sortOrder.equals("date_new"))      date_new.setChecked(true);
        else if (sortOrder.equals("date_old"))      date_old.setChecked(true);

        // ----- Set up the clickListeners ----

        // Cancel button (exit without saving)
        cancel.setOnClickListener(view12 -> NavHostFragment.findNavController(FragmentSortSeries.this)
                .navigate(R.id.action_cancel));

        // Done button  (save and exit)
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.title_asc)        mViewModel.changeSortOrder("title_ASC");
            else if (checkedId == R.id.title_desc)  mViewModel.changeSortOrder("title_DESC");
            else if (checkedId == R.id.rating_high) mViewModel.changeSortOrder("rating_high");
            else if (checkedId == R.id.rating_low)  mViewModel.changeSortOrder("rating_low");
            else if (checkedId == R.id.date_new)    mViewModel.changeSortOrder("date_new");
            else if (checkedId == R.id.date_old)    mViewModel.changeSortOrder("date_old");

            // Close BottomSheetDialogFragment no matter the result
            NavHostFragment.findNavController(FragmentSortSeries.this)
                    .navigate(R.id.action_cancel);
        });

    }

}

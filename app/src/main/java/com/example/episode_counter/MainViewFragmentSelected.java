package com.example.episode_counter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainViewFragmentSelected extends Fragment {


    private SeriesViewModel mViewModel;
    private SeriesListAdapterSelected mAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_view_selected, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SeriesViewModel
        mViewModel = new ViewModelProvider(requireActivity(),
                new SeriesViewModelFactory(requireActivity().getApplication()))
                .get(SeriesViewModel.class);

        // SeriesListAdapter
        mAdapter = new SeriesListAdapterSelected(MainViewFragmentSelected.this);

        mViewModel.getSeries().observe(requireActivity(), series -> mAdapter.submitList(series));
        //mViewModel.select(null);
        mAdapter.setViewModel(mViewModel);

        // RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        // ScrollToPosition
        if (mViewModel.getLayoutFirstVisibleItem() != -1 && mViewModel.getLayoutOffset() != -1) {
            mLayoutManager.scrollToPositionWithOffset(mViewModel.getLayoutFirstVisibleItem(),
                    mViewModel.getLayoutOffset());
        }

        // NavigationBar
        BottomNavigationView navigationView = view.findViewById(R.id.navigation_bar);
        navigationView.setOnNavigationItemSelectedListener(item -> {

            // Second menu (selectionMode)
            if (item.getItemId() == R.id.delete_selected) {
                mViewModel.deleteSelection();
            }
            // exit button has no specific actions

            // Common actions for all items in the BottomNavigationView
            mViewModel.select(null);
            // Important to delete the selection so nothing is saved to next time
            mViewModel.clearDeleteSelection();

            // Save LayoutPosition in ViewModel so reyclerView positions in different fragment match
            int firstItem = mLayoutManager.findFirstVisibleItemPosition();
            int firstOffset = (mLayoutManager.findViewByPosition(firstItem)).getTop();
            mViewModel.storeLayoutPosition(firstItem, firstOffset);

            NavHostFragment.findNavController(MainViewFragmentSelected.this)
                    .navigate(R.id.action_cancel);
            return false;
        });

    }
}

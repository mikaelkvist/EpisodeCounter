package com.example.episode_counter;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Delete;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainViewFragment extends Fragment implements View.OnLongClickListener, View.OnClickListener {


    private SeriesViewModel mViewModel;
    private SeriesListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SeriesViewModel
        mViewModel = new ViewModelProvider(requireActivity(),
                new SeriesViewModelFactory(requireActivity().getApplication()))
                .get(SeriesViewModel.class);

        // SeriesListAdapter
        mAdapter = new SeriesListAdapter(MainViewFragment.this);
        mAdapter.setOnClickListener(this);
        mAdapter.setOnLongClickListener(this);

        mViewModel.getSeries().observe(requireActivity(), series -> mAdapter.submitList(series));
        mViewModel.select(null);
        mAdapter.setViewModel(mViewModel);

        // RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        // NavigationBar
        BottomNavigationView navigationView = view.findViewById(R.id.navigation_bar);
        navigationView.setOnNavigationItemSelectedListener(item -> {

            // First menu (default)
            if (item.getItemId() == R.id.create_new) {
                // set null so we know no series is going to be edited
                mViewModel.select(null);
                NavHostFragment.findNavController(MainViewFragment.this)
                        .navigate(R.id.action_MainViewFragment_to_fragmentEditSeries);
            } else if (item.getItemId() == R.id.sort) {
                NavHostFragment.findNavController(MainViewFragment.this)
                        .navigate(R.id.action_MainViewFragment_to_fragmentSortSeries);
            } else if (item.getItemId() == R.id.import_export) {
                NavHostFragment.findNavController(MainViewFragment.this)
                        .navigate(R.id.action_MainViewFragment_to_fragmentImportExport);
            }
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // When this fragment comes back, we want the Recyclerview to have the same position
        // as other Recyclerviews in other fragments (specifically MainViewFragmentSelected)

        // ScrollToPosition
        if (mViewModel.getLayoutFirstVisibleItem() != -1 && mViewModel.getLayoutOffset() != -1) {
            mLayoutManager.scrollToPositionWithOffset(mViewModel.getLayoutFirstVisibleItem(),
                    mViewModel.getLayoutOffset());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        // Item longclicked: go to selection fragment

        int firstItem = mLayoutManager.findFirstVisibleItemPosition();
        int firstOffset = (mLayoutManager.findViewByPosition(firstItem)).getTop();
        mViewModel.storeLayoutPosition(firstItem, firstOffset);

        // OnLongClick is for items in Recyclerview: when one is clicked -> change to selectMode
        NavHostFragment.findNavController(MainViewFragment.this)
                .navigate(R.id.action_MainViewFragment_to_mainViewFragmentSelected);
        return false;
    }

    @Override
    public void onClick(View v) {
        // Item clicked: go to item expanded
        NavHostFragment.findNavController(MainViewFragment.this).navigate(R.id.action_MainView_to_Series);
    }
}

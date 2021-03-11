package com.example.episode_counter;

import android.content.Intent;
import android.nfc.FormatException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FragmentImportExport extends BottomSheetDialogFragment {

    private final int PICK_IMPORT = 2;
    private final int PICK_EXPORT = 3;
    private SeriesViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.import_export, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SeriesViewModel
        mViewModel = new ViewModelProvider(requireActivity(),
                new SeriesViewModelFactory(requireActivity().getApplication()))
                .get(SeriesViewModel.class);

        // ----- Set up the clickListeners ----
        // Import button
        view.findViewById(R.id.import_button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_file_to_import)), PICK_IMPORT);
        });
        // Export button
        view.findViewById(R.id.export_button).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, "data.txt");
            startActivityForResult(Intent.createChooser(intent, getString(R.string.enter_filename_to_export_to)), PICK_EXPORT);


        });
        // Cancel button
        view.findViewById(R.id.cancel).setOnClickListener(v -> {
            NavHostFragment.findNavController(FragmentImportExport.this)
                    .navigate(R.id.action_cancel);
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (requestCode == PICK_EXPORT)         mViewModel.exportData(data.getData());
            else if (requestCode == PICK_IMPORT)    mViewModel.importData(data.getData());

            // Do this for both EXPORT AND IMPORT
            NavHostFragment.findNavController(FragmentImportExport.this)
                    .navigate(R.id.action_cancel);
        }

    }
}

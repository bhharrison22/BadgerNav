package com.example.badgernav.ui.buildinginfo;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.badgernav.R;

public class BuildingInfoFragment extends Fragment {

    private BuildingInfoViewModel mViewModel;

    public static BuildingInfoFragment newInstance() {
        return new BuildingInfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i("BuildingInfo","Created!");

        return inflater.inflate(R.layout.building_info_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            fillDropDown();
        }
        catch (Exception ex) {
            Log.e("BuildingInfo","fillDropDown Error! - " + ex.getMessage());
        }

        updateCapacity();

        mViewModel = new ViewModelProvider(this).get(BuildingInfoViewModel.class);
    }

    private void fillDropDown() {
        Spinner dropDown = getView().findViewById(R.id.buildingDropDownList);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.test_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropDown.setAdapter(adapter);
    }

    private void updateCapacity() {
        int capacity = 10;
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.capacityBar);
        TextView capacityText = getView().findViewById(R.id.capacityText);

        capacityText.setText(capacity + "%");
        progressBar.setProgress(capacity);
    }



}
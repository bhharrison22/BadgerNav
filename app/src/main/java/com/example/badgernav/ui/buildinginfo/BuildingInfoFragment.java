package com.example.badgernav.ui.buildinginfo;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.badgernav.R;

import java.util.ArrayList;

public class BuildingInfoFragment extends Fragment {

    private BuildingInfoViewModel mViewModel;
    SQLiteDatabase sqLiteDatabase;
    private ArrayList<BuildingInfo> buildingInfos;

    public static BuildingInfoFragment newInstance() {
        return new BuildingInfoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i("BuildingInfo","Created!");
        sqLiteDatabase = getContext().openOrCreateDatabase("BadgerNav", Context.MODE_PRIVATE,null);

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

        updateCapacity(0);

        mViewModel = new ViewModelProvider(this).get(BuildingInfoViewModel.class);
    }

    private void fillDropDown() {
        /*
        Spinner dropDown = getView().findViewById(R.id.buildingDropDownList);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.building_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropDown.setAdapter(adapter);
         */
        String[] locations = getResources().getStringArray(R.array.building_array);

        BuildingInfoDBHelper dbHelper = new BuildingInfoDBHelper(sqLiteDatabase);
        dbHelper.createTable();
        dbHelper.populateBuildingTable(locations);
        buildingInfos = dbHelper.getBuildingInfo();

        AutoCompleteTextView autoCompleteTextView = getView().findViewById(R.id.buildingDropDownList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, locations);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String buildingName = autoCompleteTextView.getText().toString();

                BuildingInfo selected = buildingInfos.stream().filter(b -> buildingName.equals(b.getName())).findFirst().orElse(null);
                if (selected != null) {
                    TextView buildingNameView = getView().findViewById(R.id.buildingName);
                    buildingNameView.setText(selected.getName());

                    updateCapacity(selected.getCapacity());

                    TextView hoursNameView = getView().findViewById(R.id.hoursText);
                    hoursNameView.setText(selected.getHours());

                    TextView addrNameView = getView().findViewById(R.id.addrText);
                    addrNameView.setText(selected.getAddress());
                }
            }
        });

    }

    private void updateCapacity(int capacity) {
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.capacityBar);
        TextView capacityText = getView().findViewById(R.id.capacityText);

        capacityText.setText(capacity + "%");
        progressBar.setProgress(capacity);
    }




}
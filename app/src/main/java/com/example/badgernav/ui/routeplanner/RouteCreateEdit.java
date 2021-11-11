package com.example.badgernav.ui.routeplanner;

import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.example.badgernav.R;

public class RouteCreateEdit extends Fragment {

    private RouteCreateEditViewModel mViewModel;

    public static RouteCreateEdit newInstance() {
        return new RouteCreateEdit();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.route_create_edit_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AutoCompleteTextView view = getView().findViewById(R.id.location_input);
        String[] locations = {"Bascom Hall", "Grainger Hall", "Ben's House"};
        // String[] locations = getResources().getStringArray(R.array.buildings_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, locations);
        view.setAdapter(adapter);

        mViewModel = new ViewModelProvider(this).get(RouteCreateEditViewModel.class);
    }

    public void back(View view){
        Intent intent = new Intent(getContext(), RoutePlannerFragment.class);
        startActivity(intent);
    }

    public void delete(View view){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setTitle("Are you sure you want to delete?");
        builder1.setMessage("Deletion will remove the stop from the route");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(getContext(), RoutePlannerFragment.class);
                        startActivity(intent);
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
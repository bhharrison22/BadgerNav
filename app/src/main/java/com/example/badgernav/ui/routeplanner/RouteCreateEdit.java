package com.example.badgernav.ui.routeplanner;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.badgernav.Event;
import com.example.badgernav.R;

public class RouteCreateEdit extends Fragment {

    private RouteCreateEditViewModel mViewModel;
    private ImageButton backButton;
    private Button saveButton;
    private Button deleteButton;
    private EditText nameInput;
    private EditText buildingingInput;

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
        String[] locations = getResources().getStringArray(R.array.building_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, locations);
        view.setAdapter(adapter);

        nameInput = getView().findViewById(R.id.name_input);
        buildingingInput = getView().findViewById(R.id.location_input);

        backButton = getView().findViewById(R.id.imageButton);
        backButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setReorderingAllowed(true);
                ft.replace(R.id.nav_host_fragment_content_nav_menu, RoutePlannerFragment.class, null);
                ft.commit();
            }
        });

        deleteButton = getView().findViewById(R.id.button);
        deleteButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                delete();
            }
        });

        saveButton = getView().findViewById(R.id.button2);
        saveButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                RoutePlannerFragment.createEvent(new Event(nameInput.getText().toString(), buildingingInput.getText().toString()));
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setReorderingAllowed(true);
                ft.replace(R.id.nav_host_fragment_content_nav_menu, RoutePlannerFragment.class, null);
                ft.commit();
            }
        });

        mViewModel = new ViewModelProvider(this).get(RouteCreateEditViewModel.class);
    }

    public void delete(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setTitle("Are you sure you want to delete?");
        builder1.setMessage("Deletion will remove the stop from the route");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.setReorderingAllowed(true);
                        ft.replace(R.id.nav_host_fragment_content_nav_menu, RoutePlannerFragment.class, null);
                        ft.commit();
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
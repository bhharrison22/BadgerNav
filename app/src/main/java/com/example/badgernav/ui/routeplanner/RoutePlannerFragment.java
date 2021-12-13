package com.example.badgernav.ui.routeplanner;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.badgernav.Event;
import com.example.badgernav.EventDBHelper;
import com.example.badgernav.MainActivity;
import com.example.badgernav.NavMenuActivity;
import com.example.badgernav.R;
import com.example.badgernav.ui.buildinginfo.BuildingInfo;
import com.example.badgernav.ui.buildinginfo.BuildingInfoDBHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class RoutePlannerFragment extends Fragment {
    private ImageButton button;

    SQLiteDatabase sqLiteDatabase;
    static EventDBHelper dbHelper;
    private static ArrayList<Event> eventList;
    private RecyclerView recyclerView;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 12;
    private final LatLng mDestinationLatLng = new LatLng(-33.8523341, 151.2106085);
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private RoutePlannerViewModel mViewModel;

    public static RoutePlannerFragment newInstance() {
        return new RoutePlannerFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        sqLiteDatabase = getContext().openOrCreateDatabase("BadgerNav", Context.MODE_PRIVATE,null);
        return inflater.inflate(R.layout.route_planner_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        setContentView(R.layout.activity_main);
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(googleMap -> {
//            mMap = googleMap;
//            googleMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destination"));
//            displayMyLocation();
//        });
        button = getView().findViewById(R.id.imageButton2);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //Intent intent = new Intent(getContext(), CreateEdit.class);
                //startActivity(intent);
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setReorderingAllowed(true);
                ft.replace(R.id.nav_host_fragment_content_nav_menu, RouteCreateEdit.class, null);
                ft.commit();
            }
        });

        dbHelper = new EventDBHelper(sqLiteDatabase);
        dbHelper.createTable();
        eventList = dbHelper.getEvents();
        recyclerView = getView().findViewById(R.id.recyclerView);
        setAdapter();
        
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        mViewModel = new ViewModelProvider(this).get(RoutePlannerViewModel.class);
        // TODO: Use the ViewModel
    }

    private void setAdapter() {
        RecyclerAdapter adapter = new RecyclerAdapter(eventList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    public static void createEvent(Event event) { dbHelper.addEvent(event);}
    public static void removeEvent(Event event){ dbHelper.removeEvent(event);}

    // TODO: decide what to do when a method is selected
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    private void displayMyLocation(){
        int permission = ActivityCompat.checkSelfPermission(getContext().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions((Activity) getContext(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }else{
            mFusedLocationProviderClient.getLastLocation().addOnCompleteListener((Activity) getContext(), task -> {
                Location mLastKnownLocation = task.getResult();
                if(task.isSuccessful() && mLastKnownLocation != null){
                    mMap.addPolyline(new PolylineOptions().add(new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), mDestinationLatLng));
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayMyLocation();
            }
        }
    }


}
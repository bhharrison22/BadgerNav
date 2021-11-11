package com.example.badgernav.ui.routeplanner;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.badgernav.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class RoutePlannerFragment extends Fragment {

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
        return inflater.inflate(R.layout.route_planner_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setContentView(R.layout.activity_main);
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(googleMap -> {
//            mMap = googleMap;
//            googleMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destination"));
//            displayMyLocation();
//        });
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        // Creates the spinner
        Spinner spinner = (Spinner) getView().findViewById(R.id.methodSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.methods_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mViewModel = new ViewModelProvider(this).get(RoutePlannerViewModel.class);
        // TODO: Use the ViewModel
    }

    // TODO: decide what to do when a method is selected
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onClick(View view){
        Intent intent = new Intent(getContext(), RouteCreateEdit.class);
        startActivity(intent);
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
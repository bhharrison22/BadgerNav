package com.example.badgernav.ui.map;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.badgernav.Event;
import com.example.badgernav.EventDBHelper;
import com.example.badgernav.R;
import com.example.badgernav.models.PolylineData;
import com.example.badgernav.models.UserPosition;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.firestore.GeoPoint;

import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        BottomNavigationView.OnNavigationItemSelectedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnPolylineClickListener,
        View.OnClickListener {

    private final String API_KEY = "AIzaSyACtAHIOxePNulTCT-WirMIcT8KW-kXLnw";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "MapFragment.java";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 12;
    private UserPosition mUserPosition;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private boolean mLocationPermissionGranted = false;
    enum Status{DIRECTIONS, FOOD, STUDY, FITNESS}
    private Status map_status = Status.DIRECTIONS;
    BottomNavigationView bottomNavigationView;
    private SQLiteDatabase sqLiteDatabase;
    private EventDBHelper dbHelper;
    private ArrayList<Event> eventList;
    private GeoApiContext mGeoApiContext = null;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationClient;
    private Marker mSelectedMarker = null;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
    private ArrayList<Marker> mMarkers = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        sqLiteDatabase = getContext().openOrCreateDatabase("BadgerNav", Context.MODE_PRIVATE, null);

        View view = inflater.inflate(R.layout.map_fragment, container, false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mMapView = (MapView) view.findViewById(R.id.mapView);
        initGoogleMap(savedInstanceState);
        getLastKnownLocation();

        bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        view.findViewById(R.id.btn_reset_map).setOnClickListener(this);
        return view;
    }

    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mGoogleMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mGoogleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    private void removeTripMarkers() {
        for (Marker marker : mTripMarkers) {
            marker.remove();
        }
    }

    private void resetSelectedMarker() {
        if (mSelectedMarker != null) {
            mSelectedMarker.setVisible(true);
            mSelectedMarker = null;
            removeTripMarkers();
        }
    }

    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if (mPolyLinesData.size() > 0) {
                    for (PolylineData polylineData : mPolyLinesData) {
                        polylineData.getPolyline().remove();
                    }
                    mPolyLinesData.clear();
                    mPolyLinesData = new ArrayList<>();
                }

                double duration = 999999;
                for (DirectionsRoute route : result.routes) {
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (com.google.maps.model.LatLng latLng : decodedPath) {

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.dark_grey));
                    polyline.setClickable(true);
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0]));

                    double tempDuration = route.legs[0].duration.inSeconds;
                    if (tempDuration < duration) {
                        duration = tempDuration;
                        onPolylineClick(polyline);
                        zoomRoute(polyline.getPoints());
                    }

                    mSelectedMarker.setVisible(false);
                }
            }
        });
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "onComplete: latitude: " + geoPoint.getLatitude());
                    Log.d(TAG, "onComplete: Longitude: " + geoPoint.getLongitude());

                    if (mUserPosition != null) {
                        mUserPosition.setGeoPoint(geoPoint);
                    } else {
                        mUserPosition = new UserPosition(geoPoint);
                    }
                }
            }
        });
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);
        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(API_KEY)
                    .build();
        }
    }

    private void calculateDirections(Marker marker) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mUserPosition.getGeoPoint().getLatitude(),
                        mUserPosition.getGeoPoint().getLongitude()
                )
        );
        directions.mode(TravelMode.WALKING);
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocationPermissionGranted) {
            getLastKnownLocation();
        } else {
            getLocationPermission();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        try {
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this.getContext(), R.raw.map_style));
            if (!success) {
                Log.e(TAG, "onMapReady: Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "onMapReady: Can't find style. Error: " + e);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mUserPosition == null) {
            getLastKnownLocation();
        }
        mGoogleMap = map;
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnPolylineClickListener(this);
        addMapMarkers();
    }

    private void setMapStartingView() {
        LatLng start = new LatLng(43.0712074, -89.4062688);
        CameraPosition.Builder camBuilder = CameraPosition.builder();
        camBuilder.target(start);
        camBuilder.zoom(15);
        CameraPosition cp = camBuilder.build();
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

    private void resetMap() {
        if (mGoogleMap != null) {
            mGoogleMap.clear();

            if (mMarkers.size() > 0) {
                mMarkers.clear();
                mMarkers = new ArrayList<>();
            }

            if (mPolyLinesData.size() > 0) {
                mPolyLinesData.clear();
                mPolyLinesData = new ArrayList<>();
            }
        }
    }

    private void addMapMarkers() {
        if (mGoogleMap != null) {
            resetMap();

            dbHelper = new EventDBHelper(sqLiteDatabase);
            dbHelper.createTable();
            eventList = dbHelper.getEvents();
            ArrayList<String> eBuildings = new ArrayList<>();
            for (Event e : eventList) {
                eBuildings.add(e.getBuilding());
                getPlaceInfo(e, mGoogleMap);
            }
            Log.i(TAG, String.format("addMapMarkers: Event Buildings %s", eBuildings));
            mGoogleMap.setOnInfoWindowClickListener(this);
            setMapStartingView();
        }
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.directions:
                Log.d(TAG, "onNavigationItemSelected: directions selected");
                map_status = Status.DIRECTIONS;
                addMapMarkers();
                return true;
            case R.id.food:
                Log.d(TAG, "onNavigationItemSelected: food selected");
                map_status = Status.FOOD;
                addMapMarkers();
                return true;
            case R.id.study:
                Log.d(TAG, "onNavigationItemSelected: study selected");
                map_status = Status.STUDY;
                addMapMarkers();
                return true;
            case R.id.fitness:
                Log.d(TAG, "onNavigationItemSelected: fitness selected");
                map_status = Status.FITNESS;
                addMapMarkers();
                return true;
            default:
                return false;
        }
    }

    private void getPlaceInfo(Event e, GoogleMap map) {

        String name = e.getBuilding();

        String encodedName = URLEncoder.encode(name) + "\n";
        if (name.equals("Ben's House")) {
            encodedName = URLEncoder.encode("107 N Randall Ave, Madison WI");
        }

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json\n" +
                "?input=" + encodedName +
                "&inputtype=textquery\n" +
                "&key=" + API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONObject candidate = (JSONObject) (json.getJSONArray("candidates").get(0));
                            String placeID = candidate.getString("place_id");
                            Log.i(TAG, "getPlaceInfo: Calling getGeocode for " + e.getBuilding());
                            getGeocode(placeID, e, map);
                        } catch (JSONException e) {
                            Log.e(TAG, "getPlaceInfo: Find place API fail: " + e.toString());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getPlaceInfo: Find place API fail: " + error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void getGeocode(String placeID, Event e, GoogleMap map) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://maps.googleapis.com/maps/api/geocode/json\n" +
                "?place_id=" + placeID + "\n" +
                "&key=" + API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            Double lat = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                            Double lng = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                            LatLng loc = new LatLng(lat, lng);
                            Log.i(TAG, "getGeocode: Lat:" + lat + " Lng:" + lng);
                            String name = e.getTitle();
                            String desc = e.getBuilding() + " at " + e.getTime();

                            Marker marker = map.addMarker(new MarkerOptions()
                                    .snippet(desc)
                                    .position(loc)
                                    .title(name));
                            mMarkers.add(marker);
                        } catch (JSONException e) {
                            Log.e(TAG, "getGeocode: Geocode API call fail: " + e.toString());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getGeocode: Geocode API call fail: " + error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        if (marker.getTitle().contains("Trip: #")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Open Google Maps?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (dialog, id) -> {
                        String latitude = String.valueOf(marker.getPosition().latitude);
                        String longitude = String.valueOf(marker.getPosition().longitude);
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");

                        try {
                            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivity(mapIntent);
                            }
                        } catch (NullPointerException e) {
                            Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage());
                            Toast.makeText(getActivity(), "Couldn't open map", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, id) -> {
                        dialog.cancel();
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Get directions to " + marker.getTitle() + "?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (dialog, id) -> {
                        resetSelectedMarker();
                        mSelectedMarker = marker;
                        calculateDirections(marker);
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, id) -> {
                        dialog.cancel();
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        int index = 0;
        for (PolylineData polylineData : mPolyLinesData) {
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if (polyline.getId().equals(polylineData.getPolyline().getId())) {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.blue));
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );

                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Trip: #" + index)
                        .snippet("Duration: " + polylineData.getLeg().duration)
                );

                marker.showInfoWindow();

                mTripMarkers.add(marker);
            } else {
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.dark_grey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset_map: {
                addMapMarkers();
                break;
            }
        }
    }
}
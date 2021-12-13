package com.example.badgernav.ui.routeplanner;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.badgernav.models.Event;
import com.example.badgernav.util.EventDBHelper;
import com.example.badgernav.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.GeoApiContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

@SuppressLint("LongLogTag")
public class RoutePlannerFragment extends Fragment implements OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "RoutePlannerFragment.java";
    private final String API_KEY = "AIzaSyACtAHIOxePNulTCT-WirMIcT8KW-kXLnw";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private ImageButton button;
    private SQLiteDatabase sqLiteDatabase;
    static EventDBHelper dbHelper;
    private static ArrayList<Event> eventList;
    private RecyclerView recyclerView;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private RoutePlannerViewModel mViewModel;

    private ArrayList<Marker> mMarkers = new ArrayList<>();
    private GeoApiContext mGeoApiContext = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        sqLiteDatabase = getContext().openOrCreateDatabase("BadgerNav", Context.MODE_PRIVATE, null);
        View view = inflater.inflate(R.layout.route_planner_fragment, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        initGoogleMap(savedInstanceState);
        return view;
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        button = getView().findViewById(R.id.imageButton2);
        button.setOnClickListener(v -> {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setReorderingAllowed(true);
            ft.replace(R.id.nav_host_fragment_content_nav_menu, RouteCreateEdit.class, null);
            ft.commit();
        });

        dbHelper = new EventDBHelper(sqLiteDatabase);
        dbHelper.createTable();
        eventList = dbHelper.getEvents();
        recyclerView = getView().findViewById(R.id.recyclerView);
        setAdapter();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        // Creates the spinner
        Spinner spinner = (Spinner) getView().findViewById(R.id.methodSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.methods_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mViewModel = new ViewModelProvider(this).get(RoutePlannerViewModel.class);

    }

    private void setAdapter() {
        RecyclerAdapter adapter = new RecyclerAdapter(eventList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    public static void createEvent(Event event) {
        dbHelper.addEvent(event);
    }

    public static void removeEvent(Event event) {
        dbHelper.removeEvent(event);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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
        mGoogleMap = map;
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
        }
    }

    private void addMapMarkers() {
        if (mGoogleMap != null) {
            resetMap();

            ArrayList<String> eBuildings = new ArrayList<>();
            EventDBHelper dbHelper = new EventDBHelper(sqLiteDatabase);
            dbHelper.createTable();
            ArrayList<Event> eventList = dbHelper.getEvents();
            for (Event e : eventList) {
                eBuildings.add(e.getBuilding());
                getPlaceInfo(e, mGoogleMap);
            }

            Log.i(TAG, String.format("addMapMarkers: Event Buildings %s", eBuildings));
            mGoogleMap.setOnInfoWindowClickListener(this);
            setMapStartingView();
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
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        JSONObject candidate = (JSONObject) (json.getJSONArray("candidates").get(0));
                        String placeID = candidate.getString("place_id");
                        Log.i(TAG, "getPlaceInfo: Calling getGeocode for " + e.getBuilding());
                        getGeocode(placeID, e, map);
                    } catch (JSONException e1) {
                        Log.e(TAG, "getPlaceInfo: Find place API fail: " + e1.toString());
                    }

                }, error -> Log.e(TAG, "getPlaceInfo: Find place API fail: " + error.toString()));

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
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        double lat = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        double lng = json.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                        LatLng loc = new LatLng(lat, lng);
                        Log.i(TAG, "getGeocode: Lat:" + lat + " Lng:" + lng);
                        String name = e.getTitle();
                        String desc = e.getBuilding() + " at " + e.getTime();

                        Marker marker = map.addMarker(new MarkerOptions()
                                .snippet(desc)
                                .position(loc)
                                .title(name));
                        marker.setTag(e);
                        mMarkers.add(marker);
                    } catch (JSONException e1) {
                        Log.e(TAG, "getGeocode: Geocode API call fail: " + e1.toString());
                    }

                }, error -> Log.e(TAG, "getGeocode: Geocode API call fail: " + error.toString()));

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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
    public void onInfoWindowClick(@NonNull Marker marker) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete " + marker.getTitle() + "?")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, id) -> {
                    removeEvent((Event)marker.getTag());
                    addMapMarkers();
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
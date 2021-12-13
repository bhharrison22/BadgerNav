package com.example.badgernav.ui.map;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.badgernav.Event;
import com.example.badgernav.EventDBHelper;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.WriteAbortedException;
import java.io.Writer;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback, BottomNavigationView.OnNavigationItemSelectedListener {

    private final String API_KEY = "AIzaSyACtAHIOxePNulTCT-WirMIcT8KW-kXLnw";
    private final String SEARCH_API_KEY = "AIzaSyACtAHIOxePNulTCT-WirMIcT8KW-kXLnw";

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "MapFragment.java";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 12;
    private MapView mMapView;
    private boolean mLocationPermissionGranted = false;
    private String map_status = "ALL";
    BottomNavigationView bottomNavigationView;
    private SQLiteDatabase sqLiteDatabase;
    private EventDBHelper dbHelper;
    private ArrayList<Event> eventList;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        sqLiteDatabase = getContext().openOrCreateDatabase("BadgerNav", Context.MODE_PRIVATE, null);

        View view = inflater.inflate(R.layout.map_fragment, container, false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        mMapView = (MapView) view.findViewById(R.id.mapView);
        initGoogleMap(savedInstanceState);

        bottomNavigationView = view.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        return view;
    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()) {
                    Location location = task.getResult();
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "onComplete: latitude: " + geoPoint.getLatitude());
                    Log.d(TAG, "onComplete: Longitude: " + geoPoint.getLongitude());
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
        if(mLocationPermissionGranted) {
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
//        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
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

        dbHelper = new EventDBHelper(sqLiteDatabase);
        dbHelper.createTable();
        eventList = dbHelper.getEvents();
        ArrayList<String> eBuildings = new ArrayList<>();
        for (Event e: eventList) {
            eBuildings.add(e.getBuilding());
            getPlaceInfo(e, map);
        }
        Log.i(TAG, String.format("onMapReady: Event Buildings %s", eBuildings));



        map.setMyLocationEnabled(true);
        LatLng start = new LatLng(43.0722515,-89.4035545);
        CameraPosition.Builder camBuilder = CameraPosition.builder();
        camBuilder.target(start);
        camBuilder.zoom(16);

        CameraPosition cp = camBuilder.build();

        map.moveCamera(CameraUpdateFactory.newCameraPosition(cp));

//        // Grab building info
//        InputStream is = getResources().openRawResource(R.raw.buildings);
//        Writer writer = new StringWriter();
//        char[] buffer = new char[1024];
//        try {
//            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//            int n;
//            while ((n = reader.read(buffer)) != -1) {
//                writer.write(buffer, 0, n);
//            }
//            is.close();
//        } catch (IOException e) {
//            Log.e(TAG, "onMapReady: Could not find building json");
//        }

//        String jsonString = writer.toString();
//        JSONArray arr = null;
//        try {
//            arr = new JSONArray(jsonString);
//            for (int i = 0; i < arr.length(); i++) {
//                JSONObject obj = arr.getJSONObject(i);
//                String name = obj.getString("name");
//                String filter = obj.getString("filter");
//                LatLng loc = new LatLng(obj.getDouble("lat"), obj.getDouble("lon"));
//                if (filter.equals(map_status) || map_status.equals("ALL")) {
//                    Marker marker = map.addMarker(new MarkerOptions()
//                            .position(loc)
//                            .title(name));
//                }
//            }
//        } catch (JSONException e) {
//            Log.e(TAG, "onMapReady: JSON parsing error");
//        }

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
                map_status = "ALL";
//                mMapView.getM
                return true;
            case R.id.food:
                Log.d(TAG, "onNavigationItemSelected: food selected");
                map_status = "FOOD";
                return true;
            case R.id.study:
                Log.d(TAG, "onNavigationItemSelected: study selected");
                map_status = "STUDY";
                return true;
            case R.id.fitness:
                Log.d(TAG, "onNavigationItemSelected: fitness selected");
                map_status = "FITNESS";
                return true;
            default:
                return false;
        }
    }

    private void getPlaceInfo(Event e, GoogleMap map) {

        String name = e.getBuilding();

        String encodedName = URLEncoder.encode(name) + "\n";
        if (name.equals("Ben's House")){
            encodedName = URLEncoder.encode("107 N Randall Ave, Madison WI");
        }

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url ="https://maps.googleapis.com/maps/api/place/findplacefromtext/json\n" +
                "?input=" + encodedName +
                "&inputtype=textquery\n" +
                "&key=" + API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONObject candidate = (JSONObject)(json.getJSONArray("candidates").get(0));
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

    private void getPlaceDetails(String placeID) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url ="https://maps.googleapis.com/maps/api/place/details/json\n" +
                "?placeid=" + placeID + "\n" +
                "&key=" + API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            String address = json.getJSONObject("result").getString("formatted_address"); // get address
                            TextView addrNameView = getView().findViewById(R.id.addrText);
                            addrNameView.setText(address);

                        } catch (JSONException e) {
                            Log.e(TAG, "getPlaceDetails: Place details API call fail: " + e.toString());
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "getPlaceDetails: Place details API call fail: " + error.toString());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void getGeocode(String placeID, Event e, GoogleMap map) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url ="https://maps.googleapis.com/maps/api/geocode/json\n" +
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


}
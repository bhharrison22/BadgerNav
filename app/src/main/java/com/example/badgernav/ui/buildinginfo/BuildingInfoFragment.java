package com.example.badgernav.ui.buildinginfo;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.badgernav.MainActivity;
import com.example.badgernav.R;
import com.example.badgernav.ui.calendar.ContactsActivity;
import com.example.badgernav.ui.calendar.MainCalendarActivity;
import com.example.badgernav.ui.calendar.MeetingActivity;
import com.google.gson.JsonArray;
import com.google.type.DateTime;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;

public class BuildingInfoFragment extends Fragment {

    private final String API_KEY = "AIzaSyACtAHIOxePNulTCT-WirMIcT8KW-kXLnw";
    private final String SEARCH_API_KEY = "AIzaSyACtAHIOxePNulTCT-WirMIcT8KW-kXLnw";
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

                    try {
                        getPlaceInfo(selected.getName());
                        imageSearch(selected.getName());
                    }
                    catch (Exception ex) {
                        System.out.println(ex);
                    }

                    updateCapacity(selected.getCapacity());

                    TextView hoursNameView = getView().findViewById(R.id.hoursText);
                    hoursNameView.setText(selected.getHours());

                    TextView addrNameView = getView().findViewById(R.id.addrText);
                    addrNameView.setText(selected.getAddress());
                }
            }
        });

    }

    // -1 indicates closed
    private void updateCapacity(int capacity) {
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.capacityBar);
        TextView capacityText = getView().findViewById(R.id.capacityText);

        if (capacity >= 0)
            capacityText.setText(capacity + "%");
        else
            capacityText.setText("CLOSED");
        progressBar.setProgress(capacity);


    }

    private void getPlaceInfo(String name) {

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
                            getPlaceDetails(placeID);

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error!");
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

                            try {
                                // get today's hours
                                JSONArray jsonHours = json.getJSONObject("result").getJSONObject("opening_hours").getJSONArray("weekday_text");

                                //Get Day of week and convert to JSONArray index
                                LocalDateTime now = LocalDateTime.now();
                                int dayOfWeek = now.getDayOfWeek().getValue() - 1 ;
                                if (dayOfWeek == -1)
                                    dayOfWeek = 6;
                                String hours = jsonHours.getString(dayOfWeek);

                                // Set as closed if its after hours
                                // This code is really fucking stupid and breaks if somewhere opens at noon or is closed all day
                                // but Java's string conversions to DateTime is super broken and I wasted too much time trying to do it properly
                                TextView hoursNameView = getView().findViewById(R.id.hoursText);
                                hoursNameView.setText(hours);

                                //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy KK:mm a");

                                int day = now.getDayOfMonth();
                                int month = now.getMonthValue();
                                int year = now.getYear();

                                String opening = hours.substring(hours.indexOf(": ") + 2, hours.indexOf(" – ") - 3);
                                String closing = hours.substring(hours.indexOf(" – ") + 3, hours.indexOf("PM")-1);

                                int openingHour = Character.getNumericValue(opening.charAt(0));
                                int openingMinute = Integer.parseInt(opening.substring(opening.indexOf(":") + 1));

                                int closingHour = Character.getNumericValue(closing.charAt(0)) + 12;
                                int closingMinute = Integer.parseInt(closing.substring(opening.indexOf(":") + 1));

                                LocalDateTime openingDate = LocalDateTime.of(year, month, day, openingHour, openingMinute);
                                LocalDateTime closingDate = LocalDateTime.of(year, month, day, closingHour, closingMinute);

                                if(isAfterHours(openingDate, closingDate, now)) {
                                    updateCapacity(-1);
                                }
                            }
                            catch (Exception e) {
                                StackTraceElement[] err = e.getStackTrace();
                                System.err.println("String Parsing went wrong");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean isAfterHours(LocalDateTime openingTime, LocalDateTime ClosingTime, LocalDateTime now) {
        return (now.compareTo(ClosingTime) > 0 || now.compareTo(openingTime) < 0);
    }

    private void imageSearch(String subject) {
        String encodedSubject = URLEncoder.encode(subject + " UW Madison") + "\n";
        if (subject.equals("Ben's House")) {
            encodedSubject = URLEncoder.encode("107 N Randall Ave");
        }
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url ="https://www.googleapis.com/customsearch/v1?key=" + SEARCH_API_KEY + "&cx=c37c32ed9f1ec39c1&q="+encodedSubject+"&searchType=image";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);

                            Random rand = new Random();
                            int index = rand.nextInt(5 + 1 - 1) + 1; // gets random image from top 5 results

                            JSONObject values = (JSONObject) json.getJSONArray("items").get(index);
                            String imgURL = values.getString("link");
                            setBuildingImage(imgURL);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void setBuildingImage(String url) {
        ImageView buildingImage = getView().findViewById(R.id.buildingMapImage);
        try {
            if (!url.contains("https"))
                Picasso.get().load(url.replace("http", "https")).resize(230,200).into(buildingImage);
            else
                Picasso.get().load(url).resize(230,200).into(buildingImage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }





}
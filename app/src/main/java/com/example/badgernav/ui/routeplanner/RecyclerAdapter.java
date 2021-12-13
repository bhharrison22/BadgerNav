package com.example.badgernav.ui.routeplanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.badgernav.Event;
import com.example.badgernav.R;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{

    private ArrayList<Event> eventsList;

    public RecyclerAdapter(ArrayList<Event> eventsList){
        this.eventsList = eventsList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView eventText;
        private TextView buildingText;
        private TextView timeText;

        public MyViewHolder(final View itemView) {
            super(itemView);
            eventText = itemView.findViewById(R.id.eventText);
            buildingText = itemView.findViewById(R.id.buildingText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }

    @NonNull
    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_items, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.MyViewHolder holder, int position) {
        String event = eventsList.get(position).getTitle();
        String building = eventsList.get(position).getBuilding();
        String time = eventsList.get(position).getTime();

        holder.eventText.setText(event);
        holder.buildingText.setText(building);
        holder.timeText.setText(time);
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }
}

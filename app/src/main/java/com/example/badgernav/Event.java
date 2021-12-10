package com.example.badgernav;

public class Event {
    private String title;
    private String building;
    private String time;

    public Event(String title, String building, String time){
        this.building = building;
        this.time = time;
        this.title = title;
    }
    public Event(String title, String building){
        this.building = building;
        this.time = "0:00";
        this.title = title;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

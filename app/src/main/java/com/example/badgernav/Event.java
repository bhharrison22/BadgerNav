package com.example.badgernav;

public class Event {
    private String title;
    private String building;
    private String time;
    private String method;

    public Event(String title, String building, String time, String method){
        this.building = building;
        this.time = time;
        this.title = title;
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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

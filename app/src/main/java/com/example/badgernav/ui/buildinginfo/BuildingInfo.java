package com.example.badgernav.ui.buildinginfo;

import java.util.Random;

public class BuildingInfo {
    private String name;
    private String address;
    private String hours;
    private String floormap;
    private int capacity;

    public BuildingInfo(String name, String address, String hours, String floormap, int capacity) {
        this.name = name;
        this.address = address;
        this.hours = hours;
        this.floormap = floormap;
        Random rand = new Random();
        this.capacity = rand.nextInt(45 + 1 - 5) + 5; // random number between 5 and 45
    }

    public String getName() {return name;}
    public String getAddress() {return address;}
    public String getHours() {return hours;}
    public String getFloormap() {return floormap;}
    public int getCapacity() {return capacity;}

}

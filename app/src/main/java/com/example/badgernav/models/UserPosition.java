package com.example.badgernav.models;

import com.google.firebase.firestore.GeoPoint;

public class UserPosition {
    private GeoPoint geoPoint;

    public UserPosition(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public UserPosition() {

    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "geoPoint=" + geoPoint +
                '}';
    }
}

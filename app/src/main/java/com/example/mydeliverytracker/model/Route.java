package com.example.mydeliverytracker.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Route {
    private List<LatLng> points;
    private String eta; // e.g., "15 mins"
    private double distance; // in kilometers

    public Route(List<LatLng> points, String eta, double distance) {
        this.points = points;
        this.eta = eta;
        this.distance = distance;
    }

    public List<LatLng> getPoints() { return points; }
    public String getEta() { return eta; }
    public double getDistance() { return distance; }
}

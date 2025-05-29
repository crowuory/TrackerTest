package com.example.mydeliverytracker.api;

import java.util.List;

public class DirectionsResponse {
    public List<Route> routes;

    public static class Route {
        public List<Leg> legs;
    }

    public static class Leg {
        public Distance distance;
        public Duration duration;
        public List<Step> steps;
    }

    public static class Distance {
        public String text;
        public double value; // in meters
    }

    public static class Duration {
        public String text;
        public long value; // in seconds
    }

    public static class Step {
        public Polyline polyline;
    }

    public static class Polyline {
        public String points;
    }
}

package com.googleinterns.smb.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POJO class to capture response from Directions API call.
 */
public class DirectionResponse {

    @SerializedName("routes")
    @Expose
    private List<Route> routes = null;

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public long getTotalDuration() {
        List<DirectionResponse.Leg> legs = getRoutes().get(0).getLegs();
        long durationInSecs = 0;
        for (DirectionResponse.Leg leg : legs) {
            durationInSecs += leg.getDuration().getValue();
        }
        return durationInSecs;
    }

    public long getTotalDistance() {
        List<DirectionResponse.Leg> legs = getRoutes().get(0).getLegs();
        long distanceInMeters = 0;
        for (DirectionResponse.Leg leg : legs) {
            distanceInMeters += leg.getDistance().getValue();
        }
        return distanceInMeters;
    }

    public String getEncodedPath() {
        return getRoutes().get(0).getOverviewPolyline().getPath();
    }

    public class Route {

        @SerializedName("legs")
        @Expose
        private List<Leg> legs = null;

        @SerializedName("overview_polyline")
        @Expose
        private OverviewPolyline overviewPolyline;

        public List<Leg> getLegs() {
            return legs;
        }

        public void setLegs(List<Leg> legs) {
            this.legs = legs;
        }

        public OverviewPolyline getOverviewPolyline() {
            return overviewPolyline;
        }

        public void setOverviewPolyline(OverviewPolyline overviewPolyline) {
            this.overviewPolyline = overviewPolyline;
        }
    }

    public class Leg {

        @SerializedName("distance")
        @Expose
        private Distance distance;

        @SerializedName("duration")
        @Expose
        private Duration duration;

        public Distance getDistance() {
            return distance;
        }

        public void setDistance(Distance distance) {
            this.distance = distance;
        }

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

    }

    public class Duration {

        @SerializedName("value")
        @Expose
        private Long value;

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }

    }

    public class Distance {

        @SerializedName("value")
        @Expose
        private Long value;

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }

    }

    public class OverviewPolyline {

        @SerializedName("points")
        @Expose
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }
}

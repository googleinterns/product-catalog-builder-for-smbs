package com.googleinterns.smb.common;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class MapViewHandler implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private LatLng source;
    private LatLng destination;
    private String encodedPath;
    private boolean isMapLoaded = false;

    // Maps view constants
    private static final int PATH_STROKE_WIDTH_PX = 16;
    private static final int PATH_COLOR = 0xff6199f5;

    public MapViewHandler(SupportMapFragment mapFragment) {
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (source != null) {
            loadMap();
        }
    }

    public void setMapAttributes(LatLng source, LatLng destination, String encodedPath) {
        this.source = source;
        this.destination = destination;
        this.encodedPath = encodedPath;
        if (googleMap != null) {
            loadMap();
        }
    }

    private synchronized void loadMap() {
        if (isMapLoaded)
            return;
        googleMap.addMarker(new MarkerOptions().position(destination).title("Customer"));
        googleMap.addMarker(new MarkerOptions().position(source).title("You"));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(source);
        builder.include(destination);
        List<LatLng> path = PolyUtil.decode(encodedPath);
        for (int i = 0; i < path.size(); i++) {
            builder.include(path.get(i));
        }
        int padding = 50;
        LatLngBounds bounds = builder.build();
        Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(path));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        polyline.setColor(PATH_COLOR);
        polyline.setWidth(PATH_STROKE_WIDTH_PX);
        polyline.setJointType(JointType.ROUND);
        isMapLoaded = true;
    }
}

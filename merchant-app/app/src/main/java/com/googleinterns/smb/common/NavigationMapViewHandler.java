package com.googleinterns.smb.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.googleinterns.smb.MainActivity;
import com.googleinterns.smb.R;

import java.util.List;

/**
 * Map handler to display navigation path from merchant to customer location.
 * See {@link com.googleinterns.smb.NewOrderDisplayActivity} and {@link com.googleinterns.smb.OngoingOrderDisplayActivity}
 */
public class NavigationMapViewHandler implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private LatLng mSource;
    private LatLng mDestination;
    private String mEncodedPath;
    private boolean mIsMapLoaded = false;

    // Maps view constants
    private static final int PATH_STROKE_WIDTH_PX = 16;
    private static final int PATH_COLOR = 0xff6199f5;
    private static final int MAP_PADDING = 50;

    public NavigationMapViewHandler(SupportMapFragment mapFragment) {
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        if (mSource != null) {
            loadMap();
        }
    }

    public void setMapAttributes(LatLng source, LatLng destination, String encodedPath) {
        this.mSource = source;
        this.mDestination = destination;
        this.mEncodedPath = encodedPath;
        if (mGoogleMap != null) {
            loadMap();
        }
    }

    private synchronized void loadMap() {
        if (mIsMapLoaded)
            return;
        // Add merchant and customer location markers
        mGoogleMap.addMarker(new MarkerOptions().position(mDestination).title("Customer"));
        mGoogleMap.addMarker(new MarkerOptions()
                .position(mSource)
                .icon(bitmapDescriptorFromVector(MainActivity.getContext()))
                .title("You")
                .flat(true));
        // Create bounding box for setting camera zoom
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mSource);
        builder.include(mDestination);
        List<LatLng> path = PolyUtil.decode(mEncodedPath);
        for (int i = 0; i < path.size(); i++) {
            builder.include(path.get(i));
        }
        LatLngBounds bounds = builder.build();
        // Draw route from merchant to customer
        Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(path));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, MAP_PADDING));
        polyline.setColor(PATH_COLOR);
        polyline.setWidth(PATH_STROKE_WIDTH_PX);
        polyline.setJointType(JointType.ROUND);
        mIsMapLoaded = true;
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_merchant_store);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}

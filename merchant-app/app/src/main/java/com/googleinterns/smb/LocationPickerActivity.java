package com.googleinterns.smb;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.googleinterns.smb.common.UIUtils;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class LocationPickerActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private static final String TAG = LocationPickerActivity.class.getName();
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    // Setting default location to New Delhi, India
    private static final LatLng mDefaultLocation = new LatLng(28.6139, 77.2090);


    private GoogleMap mGoogleMap;
    private boolean mIsGPSAvailable = false;
    private LatLng mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Pick location");
        setContentView(R.layout.activity_location_picker);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Button confirm = findViewById(R.id.button_confirm_location);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastLocation == null) {
                    return;
                }
                Intent data = new Intent();
                data.putExtra("latitude", mLastLocation.latitude);
                data.putExtra("longitude", mLastLocation.longitude);
                UIUtils.showToast(LocationPickerActivity.this,
                        LocationPickerActivity.this.getString(R.string.location_set));
                setResult(RESULT_OK, data);
                finish();
            }
        });
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Permission satisfied
            promptEnableGPS();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this,
                    getString(R.string.location_permission_rationale),
                    REQUEST_LOCATION_PERMISSION, perms);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 10);
        googleMap.moveCamera(cameraUpdate);
        if (mIsGPSAvailable) {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                mLastLocation = googleMap.getCameraPosition().target;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LocationRequest.PRIORITY_HIGH_ACCURACY) {
            switch (resultCode) {
                case RESULT_OK:
                    // All required changes were successfully made
                    mIsGPSAvailable = true;
                    if (mGoogleMap != null) {
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                    break;
                case RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.i(TAG, "onActivityResult: User rejected GPS request");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Prompt user to enable GPS
     */
    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    private void promptEnableGPS() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    mIsGPSAvailable = true;
                    if (mGoogleMap != null) {
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        LocationPickerActivity.this,
                                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });
    }
}

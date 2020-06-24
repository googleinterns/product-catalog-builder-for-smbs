package com.googleinterns.smb;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.googleinterns.smb.adapter.ConfirmedOrderAdapter;
import com.googleinterns.smb.common.APIHandler;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.DirectionResponse;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Order;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OngoingOrderDisplayActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private static final String TAG = OngoingOrderDisplayActivity.class.getName();

    private static final int PATH_STROKE_WIDTH_PX = 16;
    private static final int PATH_COLOR = 0xff6199f5;

    private GoogleMap googleMap;
    private List<LatLng> path;
    private Order order;
    private boolean isMapLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Order");
        setContentView(R.layout.activity_confirmed_order_display);
        Order order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            this.order = order;
            initView(order);
        } else {
            Log.e(TAG, "No order was provided");
        }
    }

    public void initView(final Order order) {
        TextView timeElapsed = findViewById(R.id.time_elapsed);
        timeElapsed.setText(order.getTimeElapsedString(System.currentTimeMillis()));

        TextView customerName = findViewById(R.id.customer_name);
        customerName.setText(String.format(Locale.getDefault(), "%s's order", order.getCustomerName()));

        TextView orderTotal = findViewById(R.id.total_price);
        orderTotal.setText(String.format(Locale.getDefault(), "%.2f", order.getOrderTotal()));

        TextView timeOfOrder = findViewById(R.id.time_of_order);
        timeOfOrder.setText(order.getTimeOfOrder());

        TextView address = findViewById(R.id.address);
        address.setText(order.getCustomerAddress());

        Button deliver = findViewById(R.id.deliver);
        deliver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.showToast(OngoingOrderDisplayActivity.this, "Delivery has started via partner");
                Intent intent = OngoingOrdersActivity.makeIntent(OngoingOrderDisplayActivity.this);
                startActivity(intent);
            }
        });

        // Initialize Map view
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LatLng source = Merchant.getInstance().getLatLng();
        LatLng destination = order.getCustomerLatLng();

        APIHandler.DirectionService directionService = APIHandler.getDirectionsAPIInterface();
        Call<DirectionResponse> route = directionService.getRoute(
                CommonUtils.getStringFromLatLng(source),
                CommonUtils.getStringFromLatLng(destination),
                getString(R.string.maps_api_key)
        );
        route.enqueue(new Callback<DirectionResponse>() {
            @Override
            public void onResponse(@NotNull Call<DirectionResponse> call, @NotNull Response<DirectionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    onFetchComplete(response.body());
                }
            }

            @Override
            public void onFailure(@NotNull Call<DirectionResponse> call, @NotNull Throwable t) {
                Log.e(TAG, "Error: fetching route information", t);
            }
        });

        ConfirmedOrderAdapter adapter = new ConfirmedOrderAdapter(order.getBillItems());
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (path != null) {
            loadMap();
        }
    }

    public void onFetchComplete(DirectionResponse response) {
        // Compute total duration and distance
        // Using the first Route from getRoutes()
        List<DirectionResponse.Leg> legs = response.getRoutes().get(0).getLegs();
        long durationInSecs = 0;
        long distanceInMeters = 0;
        for (DirectionResponse.Leg leg : legs) {
            durationInSecs += leg.getDuration().getValue();
            distanceInMeters += leg.getDistance().getValue();
        }
        // Get encoded path, geometry from API
        String encodedPath = response.getRoutes().get(0).getOverviewPolyline().getPath();
        // Decode into List<LatLng> to draw the path on the map
        path = PolyUtil.decode(encodedPath);
        if (googleMap != null) {
            loadMap();
        }
        TextView distance = findViewById(R.id.distance);
        distance.setText(getFormattedDistance(distanceInMeters));
        TextView expectedTime = findViewById(R.id.expected_time);
        expectedTime.setText(CommonUtils.getFormattedTime(durationInSecs));
    }

    private String getFormattedDistance(long distanceInMeters) {
        if (distanceInMeters < 1000) {
            return String.format(Locale.getDefault(), "%d m", (int) distanceInMeters);
        }
        double distanceInKms = (double) distanceInMeters / 1000;
        return String.format(Locale.getDefault(), "%.2f km", distanceInKms);
    }

    private void loadMap() {
        if (isMapLoaded) {
            return;
        }
        LatLng source = Merchant.getInstance().getLatLng();
        LatLng destination = order.getCustomerLatLng();
        googleMap.addMarker(new MarkerOptions().position(destination).title(order.getCustomerName()));
        googleMap.addMarker(new MarkerOptions().position(source).title("You"));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(source);
        builder.include(destination);
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

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.android.PolyUtil;
import com.googleinterns.smb.adapter.ConfirmedOrderAdapter;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.FetchRequestTask;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Order;

import java.util.List;
import java.util.Locale;

public class OngoingOrderDisplayActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        FetchRequestTask.OnFetchCompleteListener {

    private static final String TAG = OngoingOrderDisplayActivity.class.getName();

    // TODO move to colors.xml
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

        // Fetch data from directions API
        String uri = getDirectionsAPIUri(Merchant.getInstance().getLatLng(), order.getCustomerLatLng());
        new FetchRequestTask(this).execute(uri);

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

    private String getDirectionsAPIUri(LatLng sourceLatLng, LatLng destinationLatLng) {
        return String.format(
                Locale.getDefault(),
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s",
                sourceLatLng.latitude, sourceLatLng.longitude,
                destinationLatLng.latitude, destinationLatLng.longitude,
                getString(R.string.maps_api_key));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (path != null) {
            loadMap();
        }
    }

    @Override
    public void onFetchComplete(String response) {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(response);
        JsonObject data = jsonElement.getAsJsonObject();
        JsonArray routes = data.getAsJsonArray("routes");
        JsonObject route = routes.get(0).getAsJsonObject();
        JsonArray legs = route.getAsJsonArray("legs");

        // Compute total duration and distance
        long durationInSecs = 0;
        long distanceInMeters = 0;
        for (int i = 0; i < legs.size(); i++) {
            JsonObject leg = legs.get(i).getAsJsonObject();
            JsonObject duration = leg.getAsJsonObject("duration");
            durationInSecs += duration.get("value").getAsLong();
            JsonObject distance = leg.getAsJsonObject("distance");
            distanceInMeters += distance.get("value").getAsLong();
        }
        JsonObject overview = route.getAsJsonObject("overview_polyline");
        String encodedPath = overview.get("points").getAsString();
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

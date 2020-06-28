package com.googleinterns.smb;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.googleinterns.smb.adapter.OrderDisplayAdapter;
import com.googleinterns.smb.common.APIHandler;
import com.googleinterns.smb.common.APIHandler.ConsumerService;
import com.googleinterns.smb.common.APIHandler.DirectionService;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.DeliveryTimePicker;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.common.MapViewHandler;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.BillItem;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Order;
import com.googleinterns.smb.pojo.DirectionResponse;
import com.googleinterns.smb.pojo.SendBidRequest;

import org.jetbrains.annotations.NotNull;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewOrderDisplayActivity extends AppCompatActivity implements OrderDisplayAdapter.PriceChangeListener {

    private static final String TAG = NewOrderDisplayActivity.class.getName();
    private static final int REQUEST_MERCHANT_DETAILS = 1;

    private TextView mTotalPrice;
    private OrderDisplayAdapter orderDisplayAdapter;
    private Order order;
    private MapViewHandler mapViewHandler;
    private LatLng merchantLatLng;
    private LatLng customerLatLng;
    private DeliveryTimePicker deliveryTimePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_display);
        setTitle("Order");
        mTotalPrice = findViewById(R.id.total_price);
        TextView mTextViewCustomerName = findViewById(R.id.customer_name);
        order = (Order) getIntent().getSerializableExtra("order");

        // Initialise views with order information
        mTextViewCustomerName.setText(String.format(Locale.getDefault(), "%s's order", order.getCustomerName()));
        TextView timeElapsed = findViewById(R.id.time_elapsed);
        timeElapsed.setText(order.getTimeElapsedString(System.currentTimeMillis()));
        TextView timeOfOrder = findViewById(R.id.time_of_order);
        timeOfOrder.setText(order.getTimeOfOrder());
        Button accept = findViewById(R.id.accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrderAccept();
            }
        });
        Button decline = findViewById(R.id.decline);
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order.decline();
                exit();
            }
        });
        // Setup delivery time picker
        deliveryTimePicker = new DeliveryTimePicker(findViewById(R.id.delivery_time_chip_group));

        // Initialize Map view
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapViewHandler = new MapViewHandler(mapFragment);

        merchantLatLng = Merchant.getInstance().getLatLng();
        customerLatLng = order.getCustomerLocation();

        if (merchantLatLng != null) {
            fetchDirections();
        }
        initRecyclerView(order.getBillItems());
    }

    private void initRecyclerView(List<BillItem> billItems) {
        orderDisplayAdapter = new OrderDisplayAdapter(this, billItems, getSupportFragmentManager());
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(orderDisplayAdapter);
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
    public void onPriceChange(double newTotalPrice) {
        mTotalPrice.setText(String.format(Locale.getDefault(), "%.2f", newTotalPrice));
    }

    /**
     * Callback from DirectionService result
     *
     * @param distanceInMeters  Distance from merchant to customer location
     * @param durationInSeconds Travel time from merchant to customer
     * @param encodedPath       Route from merchant to customer encoded in string representation
     */
    public void onDirectionsResult(long distanceInMeters, long durationInSeconds, String encodedPath) {
        TextView distance = findViewById(R.id.distance);
        distance.setText(CommonUtils.getFormattedDistance(distanceInMeters));
        TextView expectedTime = findViewById(R.id.expected_time);
        expectedTime.setText(CommonUtils.getFormattedTime(durationInSeconds));
        mapViewHandler.setMapAttributes(merchantLatLng, customerLatLng, encodedPath);
        deliveryTimePicker.setDefaultDeliveryTime((int) durationInSeconds);
    }

    public void onOrderAccept() {
        if (merchantLatLng == null) {
            Intent intent = new Intent(this, SettingsActivity.class);
            UIUtils.showToast(this, "Details required to place order");
            startActivityForResult(intent, REQUEST_MERCHANT_DETAILS);
            return;
        }
        List<BillItem> availableItems = orderDisplayAdapter.getAvailableItems();
        if (availableItems.size() == 0) {
            UIUtils.showToast(this, "No items marked available");
            return;
        }
        // Update database with accepted order details
        FirebaseUtils.acceptOrder(order, availableItems);
        sendBidRequest();
    }

    private void exit() {
        Intent intent = OngoingOrdersActivity.makeIntent(NewOrderDisplayActivity.this);
        startActivity(intent);
    }


    private void sendBidRequest() {
        SendBidRequest request = SendBidRequest.createSendBidRequest(order, orderDisplayAdapter);
        request.setDeliveryTime((long) deliveryTimePicker.getDeliveryTimeInSeconds());
        ConsumerService service = APIHandler.getConsumerService();
        Call<Void> response = service.sendBid(request);
        response.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() && (response.code() == HttpURLConnection.HTTP_OK)) {
                    UIUtils.showToast(NewOrderDisplayActivity.this, "Sent! You will be notified once customer confirms the order");
                    exit();
                } else {
                    UIUtils.showToast(NewOrderDisplayActivity.this, "Failed!");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                UIUtils.showToast(NewOrderDisplayActivity.this, "Something went wrong. Please try again");
            }
        });
        UIUtils.showToast(NewOrderDisplayActivity.this, "Sending bid...");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MERCHANT_DETAILS) {
            if (resultCode == RESULT_OK) {
                merchantLatLng = Merchant.getInstance().getLatLng();
                fetchDirections();
            }
        }
    }

    private void fetchDirections() {
        DirectionService directionService = APIHandler.getDirectionService();
        Call<DirectionResponse> route = directionService.getRoute(
                CommonUtils.getStringFromLatLng(merchantLatLng),
                CommonUtils.getStringFromLatLng(customerLatLng),
                getString(R.string.directions_api_key)
        );
        route.enqueue(new Callback<DirectionResponse>() {
            @Override
            public void onResponse(@NotNull Call<DirectionResponse> call, @NotNull Response<DirectionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DirectionResponse direction = response.body();
                    onDirectionsResult(direction.getTotalDistance(), direction.getTotalDuration(), direction.getEncodedPath());
                }
            }

            @Override
            public void onFailure(@NotNull Call<DirectionResponse> call, @NotNull Throwable t) {
                Log.e(TAG, "Error: fetching route information", t);
            }
        });
    }
}

package com.googleinterns.smb;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.googleinterns.smb.adapter.ConfirmedItemAdapter;
import com.googleinterns.smb.common.APIHandler;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.NavigationMapViewHandler;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Order;
import com.googleinterns.smb.pojo.DirectionResponse;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity to display a particular ongoing card_new_order.
 */
public class OngoingOrderDisplayActivity extends AppCompatActivity {

    private static final String TAG = OngoingOrderDisplayActivity.class.getName();

    private Order order;
    private NavigationMapViewHandler mNavigationMapViewHandler;
    private LatLng mMerchantLatLng;
    private LatLng mCustomerLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Order");
        setContentView(R.layout.activity_ongoing_order_display);
        order = (Order) getIntent().getSerializableExtra("card_new_order");
        initViews();
    }

    public void initViews() {
        TextView timeElapsed = findViewById(R.id.text_view_time_elapsed);
        timeElapsed.setText(order.getTimeElapsedString(System.currentTimeMillis()));

        TextView customerName = findViewById(R.id.text_view_customer_name);
        customerName.setText(String.format(Locale.getDefault(), "%s's card_new_order", order.getCustomerName()));

        TextView orderTotal = findViewById(R.id.text_view_total_price);
        orderTotal.setText(String.format(Locale.getDefault(), "%.2f", order.getOrderTotal()));

        TextView timeOfOrder = findViewById(R.id.text_view_time_of_order);
        timeOfOrder.setText(order.getTimeOfOrder());

        TextView address = findViewById(R.id.text_view_address);
        address.setText(order.getCustomerAddress());

        Button deliver = findViewById(R.id.button_deliver);
        deliver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.showToast(OngoingOrderDisplayActivity.this, "Delivery has started via partner");
                Intent intent = OngoingOrdersActivity.makeIntent(OngoingOrderDisplayActivity.this);
                startActivity(intent);
            }
        });

        Button call = findViewById(R.id.button_call);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contact = order.getCustomerContact();
                if (contact == null) {
                    UIUtils.showToast(OngoingOrderDisplayActivity.this, "No contact provided");
                    return;
                }
                String uri = "tel:" + contact;
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });

        // Initialize locations
        mMerchantLatLng = Merchant.getInstance().getLatLng();
        mCustomerLatLng = order.getCustomerLocation();

        // Initialize Map view
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mNavigationMapViewHandler = new NavigationMapViewHandler(mapFragment);

        APIHandler.DirectionService directionService = APIHandler.getDirectionService();
        Call<DirectionResponse> route = directionService.getRoute(
                CommonUtils.getCommaFormattedLatLng(mMerchantLatLng),
                CommonUtils.getCommaFormattedLatLng(mCustomerLatLng),
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

        ConfirmedItemAdapter adapter = new ConfirmedItemAdapter(order.getBillItems());
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

    /**
     * Callback from {@link com.googleinterns.smb.common.APIHandler.DirectionService}
     *
     * @param distanceInMeters  Distance from merchant to customer location
     * @param durationInSeconds Travel time from merchant to customer
     * @param encodedPath       Route from merchant to customer encoded in string representation
     */
    public void onDirectionsResult(long distanceInMeters, long durationInSeconds, String encodedPath) {
        TextView distance = findViewById(R.id.text_view_distance);
        distance.setText(CommonUtils.getFormattedDistance(distanceInMeters));
        TextView expectedTime = findViewById(R.id.text_view_expected_time);
        expectedTime.setText(CommonUtils.getFormattedElapsedTime(durationInSeconds));
        mNavigationMapViewHandler.setMapAttributes(mMerchantLatLng, mCustomerLatLng, encodedPath);
    }
}

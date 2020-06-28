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
import com.googleinterns.smb.adapter.NewOrderItemAdapter;
import com.googleinterns.smb.common.APIHandler;
import com.googleinterns.smb.common.APIHandler.ConsumerService;
import com.googleinterns.smb.common.APIHandler.DirectionService;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.DeliveryTimePicker;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.common.NavigationMapViewHandler;
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

/**
 * Activity to display new card_new_order
 */
public class NewOrderDisplayActivity extends AppCompatActivity implements NewOrderItemAdapter.PriceChangeListener {

    private static final String TAG = NewOrderDisplayActivity.class.getName();
    private static final int REQUEST_MERCHANT_DETAILS = 1;

    private TextView mTotalPrice;
    private NewOrderItemAdapter mNewOrderItemAdapter;
    private Order mOrder;
    private NavigationMapViewHandler mNavigationMapViewHandler;
    private LatLng mMerchantLatLng;
    private LatLng mCustomerLatLng;
    private DeliveryTimePicker mDeliveryTimePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order_display);
        setTitle("Order");
        mTotalPrice = findViewById(R.id.text_view_total_price);
        TextView mTextViewCustomerName = findViewById(R.id.text_view_customer_name);
        mOrder = (Order) getIntent().getSerializableExtra("card_new_order");

        // Initialise views with card_new_order information
        mTextViewCustomerName.setText(String.format(Locale.getDefault(), "%s's card_new_order", mOrder.getCustomerName()));
        TextView timeElapsed = findViewById(R.id.text_view_time_elapsed);
        timeElapsed.setText(mOrder.getTimeElapsedString(System.currentTimeMillis()));
        TextView timeOfOrder = findViewById(R.id.text_view_time_of_order);
        timeOfOrder.setText(mOrder.getTimeOfOrder());
        Button accept = findViewById(R.id.button_accept);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrderAccept();
            }
        });
        Button decline = findViewById(R.id.button_decline);
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOrder.decline();
                exit();
            }
        });
        // Setup delivery time picker
        mDeliveryTimePicker = new DeliveryTimePicker(findViewById(R.id.chip_group_delivery_time));

        // Initialize Map view
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mNavigationMapViewHandler = new NavigationMapViewHandler(mapFragment);

        mMerchantLatLng = Merchant.getInstance().getLatLng();
        mCustomerLatLng = mOrder.getCustomerLocation();

        if (mMerchantLatLng != null) {
            fetchDirections();
        }
        initRecyclerView(mOrder.getBillItems());
    }

    private void initRecyclerView(List<BillItem> billItems) {
        mNewOrderItemAdapter = new NewOrderItemAdapter(this, billItems, getSupportFragmentManager());
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mNewOrderItemAdapter);
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
     * Callback from {@link com.googleinterns.smb.common.APIHandler.DirectionService} result
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
        mDeliveryTimePicker.setDefaultDeliveryTime((int) durationInSeconds);
    }

    public void onOrderAccept() {
        if (mMerchantLatLng == null) {
            Intent intent = new Intent(this, SettingsActivity.class);
            UIUtils.showToast(this, "Details required to place card_new_order");
            startActivityForResult(intent, REQUEST_MERCHANT_DETAILS);
            return;
        }
        List<BillItem> availableItems = mNewOrderItemAdapter.getAvailableItems();
        if (availableItems.size() == 0) {
            UIUtils.showToast(this, getString(R.string.no_items_marked_available));
            return;
        }
        // Update database with accepted card_new_order details
        FirebaseUtils.acceptOrder(mOrder, availableItems);
        sendBidRequest();
    }

    private void exit() {
        Intent intent = OngoingOrdersActivity.makeIntent(NewOrderDisplayActivity.this);
        startActivity(intent);
    }


    /**
     * Send bid request / card_new_order acceptance to consumer side API. See {@link com.googleinterns.smb.common.APIHandler.ConsumerService}
     */
    private void sendBidRequest() {
        SendBidRequest request = SendBidRequest.createSendBidRequest(mOrder, mNewOrderItemAdapter);
        request.setDeliveryTime((long) mDeliveryTimePicker.getDeliveryTimeInSeconds());
        ConsumerService service = APIHandler.getConsumerService();
        Call<Void> response = service.sendBid(request);
        response.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() && (response.code() == HttpURLConnection.HTTP_OK)) {
                    UIUtils.showToast(NewOrderDisplayActivity.this, getString(R.string.order_accept_success));
                    exit();
                } else {
                    UIUtils.showToast(NewOrderDisplayActivity.this, getString(R.string.failed));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                UIUtils.showToast(NewOrderDisplayActivity.this, getString(R.string.something_went_wrong));
            }
        });
        UIUtils.showToast(NewOrderDisplayActivity.this, getString(R.string.sending_bid));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MERCHANT_DETAILS) {
            if (resultCode == RESULT_OK) {
                mMerchantLatLng = Merchant.getInstance().getLatLng();
                fetchDirections();
            }
        }
    }

    /**
     * Get merchant to customer route information from {@link com.googleinterns.smb.common.APIHandler.DirectionService}
     */
    private void fetchDirections() {
        DirectionService directionService = APIHandler.getDirectionService();
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
    }
}

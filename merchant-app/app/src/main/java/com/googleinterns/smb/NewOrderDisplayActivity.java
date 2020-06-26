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

import com.googleinterns.smb.adapter.OrderDisplayAdapter;
import com.googleinterns.smb.common.APIHandler;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.BillItem;
import com.googleinterns.smb.model.Order;
import com.googleinterns.smb.pojo.SendBidRequest;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewOrderDisplayActivity extends AppCompatActivity implements OrderDisplayAdapter.PriceChangeListener {

    private static final String TAG = NewOrderDisplayActivity.class.getName();

    private TextView mTotalPrice;
    private OrderDisplayAdapter orderDisplayAdapter;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_display);
        setTitle("Order");
        mTotalPrice = findViewById(R.id.total_price);
        TextView mTextViewCustomerName = findViewById(R.id.customer_name);
        order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
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
                    SendBidRequest request = SendBidRequest.createSendBidRequest(order, orderDisplayAdapter);
                    APIHandler.ConsumerService service = APIHandler.getConsumerService();
                    Call<Void> response = service.sendBid(request);
                    response.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful() && (response.code() == HttpURLConnection.HTTP_OK)) {
                                UIUtils.showToast(NewOrderDisplayActivity.this, "Sent! You will be notified once customer confirms the order");
                                Intent intent = OngoingOrdersActivity.makeIntent(NewOrderDisplayActivity.this);
                                startActivity(intent);
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
            });
            initRecyclerView(order.getBillItems());
        } else {
            Log.e(TAG, "Error: No data received");
        }
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
}

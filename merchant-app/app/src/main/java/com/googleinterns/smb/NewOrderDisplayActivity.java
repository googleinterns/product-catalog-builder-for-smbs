package com.googleinterns.smb;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.OrderDisplayAdapter;
import com.googleinterns.smb.model.BillItem;
import com.googleinterns.smb.model.Order;

import java.util.List;

public class NewOrderDisplayActivity extends AppCompatActivity implements OrderDisplayAdapter.PriceChangeListener {

    private static final String TAG = NewOrderDisplayActivity.class.getName();

    private TextView mTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_display);
        setTitle("Order");
        mTotalPrice = findViewById(R.id.total_price);
        TextView mTextViewCustomerName = findViewById(R.id.customer_name);
        Order order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            // Initialise views with order information
            mTextViewCustomerName.setText(String.format("%s's order", order.getCustomerName()));
            TextView timeElapsed = findViewById(R.id.time_elapsed);
            timeElapsed.setText(order.getTimeElapsedString(System.currentTimeMillis()));
            TextView timeOfOrder = findViewById(R.id.time_of_order);
            timeOfOrder.setText(order.getTimeOfOrder());
            initRecyclerView(order.getBillItems());
        } else {
            Log.e(TAG, "Error: No data received");
        }
    }

    private void initRecyclerView(List<BillItem> billItems) {
        OrderDisplayAdapter orderDisplayAdapter = new OrderDisplayAdapter(this, billItems, getSupportFragmentManager());
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(orderDisplayAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onPriceChange(double newTotalPrice) {
        mTotalPrice.setText(String.format("%.2f", newTotalPrice));
    }
}

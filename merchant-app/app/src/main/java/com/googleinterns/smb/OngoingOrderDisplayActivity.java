package com.googleinterns.smb;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.ConfirmedOrderAdapter;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Order;

public class OngoingOrderDisplayActivity extends AppCompatActivity {

    private static final String TAG = OngoingOrderDisplayActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Order");
        setContentView(R.layout.activity_confirmed_order_display);
        Order order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            initView(order);
        } else {
            Log.e(TAG, "No order was provided");
        }
    }

    @SuppressLint("DefaultLocale")
    public void initView(Order order) {
        TextView timeElapsed = findViewById(R.id.time_elapsed);
        timeElapsed.setText(order.getTimeElapsedString(System.currentTimeMillis()));
        TextView customerName = findViewById(R.id.customer_name);
        customerName.setText(String.format("%s's order", order.getCustomerName()));
        TextView orderTotal = findViewById(R.id.total_price);
        orderTotal.setText(String.format("%.2f", order.getOrderTotal()));
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
}

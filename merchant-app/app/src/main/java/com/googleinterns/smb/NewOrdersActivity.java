package com.googleinterns.smb;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.OrderAdapter;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.model.Order;

import java.io.Serializable;
import java.util.List;

public class NewOrdersActivity extends AppCompatActivity implements
        FirebaseUtils.OnOrderReceivedListener,
        OrderAdapter.OrderSelectListener {

    private static final String TAG = NewOrdersActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_orders);
        setTitle("Orders");
        FirebaseUtils.getNewOrders(this);
    }

    /**
     * Callback from FirebaseUtils.getNewOrders() after fetching available orders
     */
    @Override
    public void onOrderReceived(List<Order> orders) {
        initRecyclerView(orders);
    }

    private void initRecyclerView(List<Order> orders) {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        Log.d(TAG, orders.toString());
        recyclerView.setAdapter(new OrderAdapter(this, orders));
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    @Override
    public void onOrderSelect(Order order) {
        Intent intent = new Intent(this, OrderDisplayActivity.class);
        intent.putExtra("order", (Serializable) order);
        startActivity(intent);
    }
}

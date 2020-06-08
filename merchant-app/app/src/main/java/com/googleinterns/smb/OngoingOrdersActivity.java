package com.googleinterns.smb;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.OrderAdapter;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.model.Order;

import java.util.List;

public class OngoingOrdersActivity extends AppCompatActivity implements
        FirebaseUtils.OnOrderReceivedListener,
        OrderAdapter.OrderSelectListener {

    private static final String TAG = OngoingOrdersActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_orders);
        setTitle("Ongoing orders");
        FirebaseUtils.getOngoingOrders(this);
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
    public void onOrderReceived(List<Order> orders) {
        View progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        initRecyclerView(orders);
    }

    @Override
    public void onOrderSelect(Order order) {
        Intent intent = new Intent(this, OngoingOrderDisplayActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }
}

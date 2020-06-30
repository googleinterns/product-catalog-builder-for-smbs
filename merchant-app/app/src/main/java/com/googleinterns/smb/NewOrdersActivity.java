package com.googleinterns.smb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.OrderAdapter;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.model.Order;

import java.io.Serializable;
import java.util.List;

/**
 * Activity to display all new orders
 */
public class NewOrdersActivity extends MainActivity implements
        FirebaseUtils.OnOrderReceivedListener,
        OrderAdapter.OrderSelectListener {

    private static final String TAG = NewOrdersActivity.class.getName();
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("New Orders");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = inflater.inflate(R.layout.activity_new_orders, null, false);
        mContainer.addView(mContentView, 0);
        FirebaseUtils.getNewOrders(this);
    }

    /**
     * Callback from {@link FirebaseUtils#getNewOrders(Context)} after fetching available orders
     */
    @Override
    public void onOrderReceived(List<Order> orders) {
        View progressBar = mContentView.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        initRecyclerView(orders);
    }

    private void initRecyclerView(List<Order> orders) {
        if (orders.isEmpty()) {
            TextView emptyOrderMsg = mContentView.findViewById(R.id.empty_orders_msg);
            emptyOrderMsg.setText(R.string.empty_new_orders_msg);
            View emptyMsgLayout = mContentView.findViewById(R.id.empty_msg);
            emptyMsgLayout.setVisibility(View.VISIBLE);
        }
        RecyclerView recyclerView = mContentView.findViewById(R.id.recycler_view);
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
        Intent intent = new Intent(this, NewOrderDisplayActivity.class);
        intent.putExtra("card_new_order", (Serializable) order);
        startActivity(intent);
    }
}

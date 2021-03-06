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

import com.googleinterns.smb.adapter.OngoingOrderAdapter;
import com.googleinterns.smb.adapter.OrderAdapter;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.model.Order;

import java.util.List;

/**
 * Activity to display all ongoing orders
 */
public class OngoingOrdersActivity extends MainActivity implements
        FirebaseUtils.OnOrderReceivedListener,
        OrderAdapter.OrderSelectListener {

    private static final String TAG = OngoingOrdersActivity.class.getName();
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.ongoing_orders));
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = inflater.inflate(R.layout.activity_new_orders, null, false);
        mContainer.addView(mContentView, 0);
        FirebaseUtils.getOngoingOrders(this);
    }

    private void initRecyclerView(List<Order> orders) {
        if (orders.isEmpty()) {
            TextView emptyOrderMsg = mContentView.findViewById(R.id.empty_orders_msg);
            emptyOrderMsg.setText(R.string.empty_ongoing_orders_msg);
            View emptyMsgLayout = mContentView.findViewById(R.id.empty_msg);
            emptyMsgLayout.setVisibility(View.VISIBLE);
        }
        RecyclerView recyclerView = mContentView.findViewById(R.id.recycler_view);
        Log.d(TAG, orders.toString());
        recyclerView.setAdapter(new OngoingOrderAdapter(this, orders));
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    @Override
    public void onOrderReceived(List<Order> orders) {
        View progressBar = mContentView.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        initRecyclerView(orders);
    }

    @Override
    public void onOrderSelect(Order order) {
        Intent intent = new Intent(this, OngoingOrderDisplayActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, OngoingOrdersActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }
}

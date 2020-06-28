package com.googleinterns.smb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.googleinterns.smb.R;
import com.googleinterns.smb.model.Order;

import java.util.List;

/**
 * Recycler view adapter to display ongoing orders. see {@link com.googleinterns.smb.OngoingOrdersActivity}
 */
public class OngoingOrderAdapter extends OrderAdapter {
    public OngoingOrderAdapter(OrderSelectListener listener, List<Order> orders) {
        super(listener, orders);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_ongoing_order, parent, false);
        return createViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final ViewHolder viewHolder = (ViewHolder) holder;
        final Order order = mOrders.get(position);
        // Initialise card_new_order status, and disable all previous card_new_order states
        switch (order.getStatus()) {
            case Order.ONGOING:
                viewHolder.mOrderStatusToggle.check(R.id.button_in_progress);
                break;
            case Order.DISPATCHED:
                viewHolder.mInProgress.setEnabled(false);
                viewHolder.mDispatched.setClickable(false);
                viewHolder.mOrderStatusToggle.check(R.id.button_dispatched);
                break;
            case Order.DELIVERED:
                viewHolder.mInProgress.setEnabled(false);
                viewHolder.mDispatched.setEnabled(false);
                viewHolder.mDelivered.setClickable(false);
                viewHolder.mOrderStatusToggle.check(R.id.button_delivered);
        }
        // Setup card_new_order status toggles
        viewHolder.mOrderStatusToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (!isChecked) {
                    return;
                }
                for (int id : group.getCheckedButtonIds()) {
                    if (id == checkedId) {
                        continue;
                    }
                    group.uncheck(id);
                }
                // On card_new_order status update disable previous states and notify updated status
                switch (checkedId) {
                    case R.id.button_dispatched:
                        viewHolder.mInProgress.setEnabled(false);
                        viewHolder.mDispatched.setClickable(false);
                        order.notifyOrderDispatch();
                        break;
                    case R.id.button_delivered:
                        viewHolder.mInProgress.setEnabled(false);
                        viewHolder.mDispatched.setEnabled(false);
                        viewHolder.mDelivered.setClickable(false);
                        order.notifyOrderDelivered();
                }
            }
        });
    }

    @Override
    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    static class ViewHolder extends OrderAdapter.ViewHolder {

        private MaterialButtonToggleGroup mOrderStatusToggle;
        private Button mInProgress;
        private Button mDispatched;
        private Button mDelivered;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mOrderStatusToggle = itemView.findViewById(R.id.toggle_order_progress);
            mInProgress = itemView.findViewById(R.id.button_in_progress);
            mDispatched = itemView.findViewById(R.id.button_dispatched);
            mDelivered = itemView.findViewById(R.id.button_delivered);
        }
    }

}

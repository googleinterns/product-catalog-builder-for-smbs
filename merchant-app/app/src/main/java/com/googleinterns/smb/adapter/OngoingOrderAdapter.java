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

public class OngoingOrderAdapter extends OrderAdapter {
    public OngoingOrderAdapter(OrderSelectListener listener, List<Order> orders) {
        super(listener, orders);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ongoing_order, parent, false);
        return createViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final ViewHolder viewHolder = (ViewHolder) holder;
        final Order order = orders.get(position);
        switch (order.getStatus()) {
            case Order.ONGOING:
                viewHolder.toggleGroup.check(R.id.in_progress);
                break;
            case Order.DISPATCHED:
                viewHolder.toggleGroup.check(R.id.dispatched);
                break;
            case Order.DELIVERED:
                viewHolder.toggleGroup.check(R.id.delivered);
        }
        viewHolder.toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
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
                switch (checkedId) {
                    case R.id.dispatched:
                        viewHolder.inProgress.setEnabled(false);
                        viewHolder.dispatched.setClickable(false);
                        order.notifyOrderDispatch();
                        break;
                    case R.id.delivered:
                        viewHolder.inProgress.setEnabled(false);
                        viewHolder.dispatched.setEnabled(false);
                        viewHolder.delivered.setClickable(false);
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

        private MaterialButtonToggleGroup toggleGroup;
        private Button inProgress;
        private Button dispatched;
        private Button delivered;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            toggleGroup = itemView.findViewById(R.id.order_progress);
            inProgress = itemView.findViewById(R.id.in_progress);
            dispatched = itemView.findViewById(R.id.dispatched);
            delivered = itemView.findViewById(R.id.delivered);
        }
    }

}

package com.googleinterns.smb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import com.google.android.material.chip.Chip;
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
        ((ViewHolder) holder).markAsDeliveredAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If isChecked update status in database
                ((ViewHolder) holder).markAsDeliveredAction.setText(R.string.delivered);
            }
        });
    }

    @Override
    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    static class ViewHolder extends OrderAdapter.ViewHolder {

        private Chip markAsDeliveredAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            markAsDeliveredAction = itemView.findViewById(R.id.action_mark_as_delivered);
        }
    }

}

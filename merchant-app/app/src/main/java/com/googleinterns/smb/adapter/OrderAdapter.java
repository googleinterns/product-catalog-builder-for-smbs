package com.googleinterns.smb.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.R;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Order;

import java.util.List;

/**
 * Recycler view adapter for displaying order items in order display activity
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OrderSelectListener {
        void onOrderSelect(Order order);
    }

    private List<Order> orders;
    private OrderSelectListener mListener;

    public OrderAdapter(OrderSelectListener listener, List<Order> orders) {
        this.orders = orders;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // initialise order view
        Order order = orders.get(position);
        holder.customerName.setText(order.getCustomerName());
        holder.customerAddress.setText(order.getCustomerAddress());
        holder.orderTotal.setText(String.format("%s %.2f", UIUtils.RUPEE, order.getOrderTotal()));
        holder.timeElapsed.setText(order.getTimeElapsedString(System.currentTimeMillis()));
        holder.orderCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onOrderSelect(orders.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView customerName;
        private TextView customerAddress;
        private TextView orderTotal;
        private TextView timeElapsed;
        private View orderCardLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            customerName = itemView.findViewById(R.id.customer_name);
            customerAddress = itemView.findViewById(R.id.customer_address);
            orderTotal = itemView.findViewById(R.id.order_total);
            orderCardLayout = itemView.findViewById(R.id.order_card_layout);
            timeElapsed = itemView.findViewById(R.id.time_elapsed);
        }
    }
}

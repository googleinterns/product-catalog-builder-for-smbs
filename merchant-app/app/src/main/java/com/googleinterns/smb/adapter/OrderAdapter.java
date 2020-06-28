package com.googleinterns.smb.adapter;

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
import java.util.Locale;

/**
 * Recycler view adapter for displaying card_new_order items in {@link com.googleinterns.smb.NewOrdersActivity}
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OrderSelectListener {
        void onOrderSelect(Order order);
    }

    protected List<Order> mOrders;
    private OrderSelectListener mListener;

    public OrderAdapter(OrderSelectListener listener, List<Order> orders) {
        mOrders = orders;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_new_order, parent, false);
        return createViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // Initialise card_new_order view
        Order order = mOrders.get(position);
        holder.mCustomerName.setText(order.getCustomerName());
        holder.mCustomerAddress.setText(order.getCustomerAddress());
        holder.mOrderTotal.setText(String.format(Locale.getDefault(), "%s %.2f", UIUtils.RUPEE, order.getOrderTotal()));
        holder.mTimeElapsed.setText(order.getTimeElapsedString(System.currentTimeMillis()));
        String items = "items";
        int numItems = order.getItemCount();
        if (numItems == 1) {
            items = "item";
        }
        holder.mItemCount.setText(String.format(Locale.getDefault(), "(%d %s)", numItems, items));
        holder.mOrderCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onOrderSelect(mOrders.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return mOrders.size();
    }

    /**
     * Use of factory method for allowing inheritance and overriding of ViewHolder class
     */
    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mCustomerName;
        private TextView mCustomerAddress;
        private TextView mOrderTotal;
        private TextView mTimeElapsed;
        private View mOrderCardLayout;
        private TextView mItemCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCustomerName = itemView.findViewById(R.id.text_view_customer_name);
            mCustomerAddress = itemView.findViewById(R.id.text_view_customer_address);
            mOrderTotal = itemView.findViewById(R.id.text_view_order_total);
            mOrderCardLayout = itemView.findViewById(R.id.layout_order_card);
            mTimeElapsed = itemView.findViewById(R.id.text_view_time_elapsed);
            mItemCount = itemView.findViewById(R.id.text_view_item_count);
        }
    }
}

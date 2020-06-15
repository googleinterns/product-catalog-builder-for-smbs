package com.googleinterns.smb.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.googleinterns.smb.R;
import com.googleinterns.smb.model.BillItem;

import java.util.List;

/**
 * Recycler view adapter for displaying order items in order display activity
 */
public class ConfirmedOrderAdapter extends RecyclerView.Adapter<ConfirmedOrderAdapter.ViewHolder> {

    private List<BillItem> billItems;

    public ConfirmedOrderAdapter(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.confirmed_order_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Initialise bill item view
        BillItem billItem = billItems.get(position);
        holder.mProductName.setText(billItem.getProductName());
        holder.mPrice.setText(billItem.getDiscountedPriceString());
        holder.mQty.setText(billItem.getQtyString());
        holder.mTotalPrice.setText(billItem.getTotalPriceString());
        Glide.with(holder.mProductImage.getContext())
                .load(billItem.getImageURL())
                .fitCenter()
                .into(holder.mProductImage);
    }

    @Override
    public int getItemCount() {
        return billItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ConfirmedOrderAdapter mAdapter;
        private TextView mProductName;
        private TextView mPrice;
        private TextView mTotalPrice;
        private ImageView mProductImage;
        private TextView mQty;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.product_name);
            mPrice = itemView.findViewById(R.id.merchant_price);
            mProductImage = itemView.findViewById(R.id.product_image);
            mQty = itemView.findViewById(R.id.qty_val);
            mTotalPrice = itemView.findViewById(R.id.total);
        }
    }
}

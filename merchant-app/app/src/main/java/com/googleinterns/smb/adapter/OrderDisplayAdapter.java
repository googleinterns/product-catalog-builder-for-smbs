package com.googleinterns.smb.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.googleinterns.smb.R;
import com.googleinterns.smb.fragment.EditPriceDialogFragment;
import com.googleinterns.smb.model.BillItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recycler view adapter for displaying order items in order display activity
 */
public class OrderDisplayAdapter extends RecyclerView.Adapter<OrderDisplayAdapter.ViewHolder> {

    public interface PriceChangeListener {
        void onPriceChange(double newTotalPrice);
    }

    // Fragment manager required for displaying dialogs
    private FragmentManager mFragmentManager;
    private List<BillItem> billItems;
    private List<Boolean> availableItems;
    private PriceChangeListener mListener;

    public OrderDisplayAdapter(PriceChangeListener listener, List<BillItem> billItems, FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        this.billItems = billItems;
        availableItems = new ArrayList<>(Collections.nCopies(billItems.size(), true));
        mListener = listener;
        // Notify listener with initial total price
        mListener.onPriceChange(getTotalPrice());
    }

    /**
     * Compute total price by summing price for each item
     */
    private Double getTotalPrice() {
        Double totalPrice = 0.0;
        for (int i = 0; i < billItems.size(); i++) {
            if (availableItems.get(i)) {
                totalPrice += billItems.get(i).getTotalPrice();
            }
        }
        return totalPrice;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new ViewHolder(view, this, mFragmentManager);
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

    private void updateAvailability(int position, boolean isAvailable) {
        availableItems.set(position, isAvailable);
        mListener.onPriceChange(getTotalPrice());
    }

    /**
     * Called by ViewHolder class on price edit confirm
     *
     * @param discountPrice updated price by user
     * @param position      position of card in recycler view
     */
    private void onConfirm(Double discountPrice, int position) {
        BillItem billItem = billItems.get(position);
        billItem.setDiscountedPrice(discountPrice);
        billItems.set(position, billItem);
        notifyItemChanged(position);
        mListener.onPriceChange(getTotalPrice());
    }

    private Double getMRP(int position) {
        return billItems.get(position).getMRP();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements EditPriceDialogFragment.EditPriceDialogInterface {
        private OrderDisplayAdapter mAdapter;
        private TextView mProductName;
        private TextView mPrice;
        private TextView mTotalPrice;
        private ImageView mProductImage;
        private TextView mQty;

        ViewHolder(@NonNull View itemView, OrderDisplayAdapter adapter, final FragmentManager fragmentManager) {
            super(itemView);
            mAdapter = adapter;
            mProductName = itemView.findViewById(R.id.product_name);
            mPrice = itemView.findViewById(R.id.merchant_price);
            mProductImage = itemView.findViewById(R.id.product_image);
            mQty = itemView.findViewById(R.id.qty_val);
            mTotalPrice = itemView.findViewById(R.id.total);
            ImageButton mEditQty = itemView.findViewById(R.id.edit_price);
            // Setup edit dialog
            mEditQty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditPriceDialogFragment editQtyDialogFragment = new EditPriceDialogFragment(ViewHolder.this);
                    editQtyDialogFragment.show(fragmentManager, "Edit qty dialog");
                }
            });
            CheckBox mCheckBoxAvailable = itemView.findViewById(R.id.checkbox_not_available);
            mCheckBoxAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mAdapter.updateAvailability(ViewHolder.this.getAdapterPosition(), isChecked);
                }
            });
        }

        /**
         * Callback from Edit price dialog
         */
        @Override
        public void onConfirm(Double newPrice) {
            mAdapter.onConfirm(newPrice, getAdapterPosition());
        }

        @Override
        public Double getMRP() {
            return mAdapter.getMRP(getAdapterPosition());
        }
    }
}

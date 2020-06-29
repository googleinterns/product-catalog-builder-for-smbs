package com.googleinterns.smb.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.fragment.EditPriceDialogFragment;
import com.googleinterns.smb.model.BillItem;

import java.util.ArrayList;
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
    private List<Boolean> itemAvailabilities;
    private PriceChangeListener mListener;
    private Context context;

    public OrderDisplayAdapter(Context context, List<BillItem> billItems, FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        this.context = context;
        this.billItems = billItems;
        itemAvailabilities = new ArrayList<>();
        for (BillItem billItem : billItems) {
            if (billItem.getMRP() > 0.0) {
                itemAvailabilities.add(true);
            } else {
                itemAvailabilities.add(false);
            }
        }
        try {
            mListener = (PriceChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement" + PriceChangeListener.class.getName());
        }
        // Notify listener with initial total price
        mListener.onPriceChange(getTotalPrice());
    }

    /**
     * Compute total price by summing price for each item
     */
    public Double getTotalPrice() {
        Double totalPrice = 0.0;
        for (int i = 0; i < billItems.size(); i++) {
            if (itemAvailabilities.get(i)) {
                totalPrice += billItems.get(i).getTotalPrice();
            }
        }
        return totalPrice;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new ViewHolder(view, this);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // Initialise bill item view
        final BillItem billItem = billItems.get(position);
        holder.mProductName.setText(billItem.getProductName());
        holder.mPrice.setText(billItem.getDiscountedPriceString());
        holder.mQty.setText(billItem.getQtyString());
        holder.mTotalPrice.setText(billItem.getTotalPriceString());
        Glide.with(holder.mProductImage.getContext())
                .load(billItem.getImageURL())
                .fitCenter()
                .into(holder.mProductImage);
        // Setup edit dialog
        holder.mEditQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPriceDialogFragment editQtyDialogFragment = new EditPriceDialogFragment(
                        new EditPriceDialogFragment.EditPriceDialogInterface() {
                            @Override
                            public void onConfirm(Double discountPrice) {
                                OrderDisplayAdapter.this.onConfirm(discountPrice, position);
                            }

                            @Override
                            public Double getMRP() {
                                return OrderDisplayAdapter.this.getMRP(position);
                            }
                        });
                editQtyDialogFragment.show(mFragmentManager, "Edit qty dialog");
            }
        });
        holder.mCheckBoxAvailable.setChecked(itemAvailabilities.get(position));
        holder.mCheckBoxAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && billItems.get(position).getDiscountedPrice() <= 0.0) {
                    UIUtils.showToast(context, "Set a price to mark available");
                    buttonView.setChecked(false);
                    return;
                }
                itemAvailabilities.set(position, isChecked);
                mListener.onPriceChange(getTotalPrice());
            }
        });
    }

    @Override
    public int getItemCount() {
        return billItems.size();
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

    public List<Boolean> getItemAvailabilities() {
        return itemAvailabilities;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public List<BillItem> getAvailableItems() {
        List<BillItem> availableItems = new ArrayList<>();
        for (int i = 0; i < billItems.size(); i++) {
            if (itemAvailabilities.get(i)) {
                availableItems.add(billItems.get(i));
            }
        }
        return availableItems;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private OrderDisplayAdapter mAdapter;
        private TextView mProductName;
        private TextView mPrice;
        private TextView mTotalPrice;
        private ImageView mProductImage;
        private TextView mQty;
        private ImageButton mEditQty;
        private CheckBox mCheckBoxAvailable;

        ViewHolder(@NonNull View itemView, OrderDisplayAdapter adapter) {
            super(itemView);
            mAdapter = adapter;
            mProductName = itemView.findViewById(R.id.product_name);
            mPrice = itemView.findViewById(R.id.merchant_price);
            mProductImage = itemView.findViewById(R.id.product_image);
            mQty = itemView.findViewById(R.id.qty_val);
            mTotalPrice = itemView.findViewById(R.id.total);
            mEditQty = itemView.findViewById(R.id.edit_price);
            mCheckBoxAvailable = itemView.findViewById(R.id.checkbox_not_available);
        }
    }
}

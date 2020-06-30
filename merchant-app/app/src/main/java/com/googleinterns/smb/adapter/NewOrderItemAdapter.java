package com.googleinterns.smb.adapter;

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
 * Recycler view adapter for displaying card_new_order items in card_new_order display activity
 */
public class NewOrderItemAdapter extends RecyclerView.Adapter<NewOrderItemAdapter.ViewHolder> {

    public interface PriceChangeListener {
        void onPriceChange(double newTotalPrice);
    }

    // Fragment manager required for displaying dialogs
    private FragmentManager mFragmentManager;
    private List<BillItem> mBillItems;
    private List<Boolean> mItemAvailabilities;
    private PriceChangeListener mListener;
    private Context mContext;

    public NewOrderItemAdapter(Context context, List<BillItem> billItems, FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        mContext = context;
        mBillItems = billItems;
        mItemAvailabilities = new ArrayList<>();
        for (BillItem billItem : billItems) {
            if (billItem.getMRP() > 0.0) {
                mItemAvailabilities.add(true);
            } else {
                mItemAvailabilities.add(false);
            }
        }
        try {
            mListener = (PriceChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement " + PriceChangeListener.class.getName());
        }
        // Notify listener with initial total price
        mListener.onPriceChange(getTotalPrice());
    }

    /**
     * Compute total price by summing price for each item
     */
    public Double getTotalPrice() {
        Double totalPrice = 0.0;
        for (int i = 0; i < mBillItems.size(); i++) {
            if (mItemAvailabilities.get(i)) {
                totalPrice += mBillItems.get(i).getTotalPrice();
            }
        }
        return totalPrice;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_new_order_item, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // Initialise bill item view
        final BillItem billItem = mBillItems.get(position);
        holder.mProductName.setText(billItem.getProductName());
        holder.mPrice.setText(billItem.getDiscountedPriceString());
        holder.mQty.setText(billItem.getQtyString());
        holder.mTotalPrice.setText(billItem.getTotalPriceString());
        Glide.with(holder.mProductImage.getContext())
                .load(billItem.getImageURL())
                .fitCenter()
                .into(holder.mProductImage);
        // Setup edit price dialog
        holder.mEditQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPriceDialogFragment editQtyDialogFragment = new EditPriceDialogFragment(
                        new EditPriceDialogFragment.PriceConfirmationListener() {
                            @Override
                            public void onPriceConfirm(Double discountPrice) {
                                NewOrderItemAdapter.this.onConfirm(discountPrice, position);
                            }
                        }, billItem.getMRP());
                editQtyDialogFragment.show(mFragmentManager, EditPriceDialogFragment.class.getName());
            }
        });
        holder.mCheckBoxAvailable.setChecked(mItemAvailabilities.get(position));
        holder.mCheckBoxAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Display error message if product is marked available without setting price first
                if (isChecked && mBillItems.get(position).getDiscountedPrice() <= 0.0) {
                    UIUtils.showToast(mContext, mContext.getString(R.string.set_price_msg));
                    buttonView.setChecked(false);
                    return;
                }
                mItemAvailabilities.set(position, isChecked);
                mListener.onPriceChange(getTotalPrice());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBillItems.size();
    }

    /**
     * Called by {@link EditPriceDialogFragment.PriceConfirmationListener} on price edit confirm
     *
     * @param discountPrice updated price by user
     * @param position      position of card in recycler view
     */
    private void onConfirm(Double discountPrice, int position) {
        BillItem billItem = mBillItems.get(position);
        billItem.setDiscountedPrice(discountPrice);
        mBillItems.set(position, billItem);
        notifyItemChanged(position);
        mListener.onPriceChange(getTotalPrice());
    }

    public List<Boolean> getItemAvailabilities() {
        return mItemAvailabilities;
    }

    public List<BillItem> getBillItems() {
        return mBillItems;
    }

    public List<BillItem> getAvailableItems() {
        List<BillItem> availableItems = new ArrayList<>();
        for (int i = 0; i < mBillItems.size(); i++) {
            if (mItemAvailabilities.get(i)) {
                availableItems.add(mBillItems.get(i));
            }
        }
        return availableItems;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mProductName;
        private TextView mPrice;
        private TextView mTotalPrice;
        private ImageView mProductImage;
        private TextView mQty;
        private ImageButton mEditQty;
        private CheckBox mCheckBoxAvailable;

        ViewHolder(@NonNull View itemView, NewOrderItemAdapter adapter) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.text_view_product_name);
            mPrice = itemView.findViewById(R.id.merchant_price);
            mProductImage = itemView.findViewById(R.id.image_view_product);
            mQty = itemView.findViewById(R.id.text_view_qty);
            mTotalPrice = itemView.findViewById(R.id.text_view_total);
            mEditQty = itemView.findViewById(R.id.edit_price);
            mCheckBoxAvailable = itemView.findViewById(R.id.checkbox_not_available);
        }
    }
}

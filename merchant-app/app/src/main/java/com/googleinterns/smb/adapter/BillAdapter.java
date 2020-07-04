package com.googleinterns.smb.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.googleinterns.smb.R;
import com.googleinterns.smb.fragment.EditQtyDialogFragment;
import com.googleinterns.smb.model.BillItem;
import com.googleinterns.smb.model.Product;

import java.util.List;

/**
 * Recycler view adapter for displaying bill items in billing activity
 */
public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    public interface TotalPriceChangeListener {
        void onNewTotalPrice(double newPrice);
    }

    // Fragment manager required for displaying dialogs
    private FragmentManager mFragmentManager;
    private List<BillItem> mBillItems;
    private TotalPriceChangeListener mListener;

    public BillAdapter(TotalPriceChangeListener listener, List<Product> products, FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        mBillItems = BillItem.getBillItems(products);
        mListener = listener;
        // Notify listener with initial total price
        mListener.onNewTotalPrice(getTotalPrice());
    }

    /**
     * Compute total price by summing price for each {@link BillItem}
     */
    private Double getTotalPrice() {
        Double totalPrice = 0.0;
        for (BillItem billItem : mBillItems) {
            totalPrice += billItem.getTotalPrice();
        }
        return totalPrice;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_bill_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Initialise bill item view
        BillItem billItem = mBillItems.get(position);
        holder.mProductName.setText(billItem.getProductName());
        holder.mPrice.setText(billItem.getDiscountedPriceString());
        holder.mQty.setText(billItem.getQtyString());
        holder.mTotalPrice.setText(billItem.getTotalPriceString());
        Glide.with(holder.mProductImage.getContext())
                .load(billItem.getImageURL())
                .fitCenter()
                .into(holder.mProductImage);
        holder.mIncQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currQty = billItem.getQty();
                onQtyChange(currQty + 1, position);
            }
        });
        holder.mDecQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currQty = billItem.getQty();
                if (currQty == 0) {
                    return;
                }
                onQtyChange(currQty - 1, position);
            }
        });
        holder.mRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRemove(position);
            }
        });
        holder.mQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditQtyDialogFragment editQtyDialogFragment = new EditQtyDialogFragment(new EditQtyDialogFragment.QtyConfirmationListener() {
                    @Override
                    public void onQtyConfirm(int qty) {
                        onQtyChange(qty, position);
                    }
                });
                editQtyDialogFragment.show(mFragmentManager, EditQtyDialogFragment.class.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBillItems.size();
    }

    private void onQtyChange(int qty, int position) {
        mBillItems.get(position).setQty(qty);
        // Update listener with new total price
        mListener.onNewTotalPrice(getTotalPrice());
        notifyItemChanged(position);
    }

    /**
     * Callback on item removed from {@link ViewHolder#mRemoveItem}
     */
    private void onRemove(int position) {
        mBillItems.remove(position);
        mListener.onNewTotalPrice(getTotalPrice());
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mProductName;
        private TextView mPrice;
        private TextView mTotalPrice;
        private ImageView mProductImage;
        private TextView mQty;
        private View mIncQty;
        private View mDecQty;
        private ImageButton mRemoveItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.text_view_product_name);
            mPrice = itemView.findViewById(R.id.merchant_price);
            mProductImage = itemView.findViewById(R.id.image_view_product);
            mQty = itemView.findViewById(R.id.text_view_qty);
            mTotalPrice = itemView.findViewById(R.id.text_view_total);
            mRemoveItem = itemView.findViewById(R.id.button_remove);
            mIncQty = itemView.findViewById(R.id.view_increment_qty);
            mDecQty = itemView.findViewById(R.id.view_decrement_qty);
        }
    }
}

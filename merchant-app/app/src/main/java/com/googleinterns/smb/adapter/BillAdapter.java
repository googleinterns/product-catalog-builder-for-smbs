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

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    public interface QtyChangeListener {
        void onQtyChange(Double newPrice);
    }

    private FragmentManager mFragmentManager;
    private List<BillItem> billItems;
    private QtyChangeListener mListener;

    public BillAdapter(QtyChangeListener listener, List<Product> products, FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        this.billItems = BillItem.getBillItems(products);
        mListener = listener;
        // notify listener with initial total price
        mListener.onQtyChange(getTotalPrice());
    }

    private Double getTotalPrice() {
        Double totalPrice = 0.0;
        for (BillItem billItem : billItems) {
            totalPrice += billItem.getTotalPrice();
        }
        return totalPrice;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_item_card, parent, false);
        return new ViewHolder(view, this, mFragmentManager);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

    private void onConfirm(int qty, int position) {
        billItems.get(position).setQty(qty);
        // update listener with new total price
        mListener.onQtyChange(getTotalPrice());
        notifyItemChanged(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements EditQtyDialogFragment.OptionSelectListener {

        private BillAdapter mAdapter;
        private TextView mProductName;
        private TextView mPrice;
        private TextView mTotalPrice;
        private ImageView mProductImage;
        private TextView mQty;

        ViewHolder(@NonNull View itemView, BillAdapter adapter, final FragmentManager fragmentManager) {
            super(itemView);
            mAdapter = adapter;
            mProductName = itemView.findViewById(R.id.product_name);
            mPrice = itemView.findViewById(R.id.merchant_price);
            mProductImage = itemView.findViewById(R.id.product_image);
            mQty = itemView.findViewById(R.id.qty_val);
            mTotalPrice = itemView.findViewById(R.id.total);
            ImageButton mEditQty = itemView.findViewById(R.id.edit_qty);
            mEditQty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditQtyDialogFragment editQtyDialogFragment = new EditQtyDialogFragment(ViewHolder.this);
                    editQtyDialogFragment.show(fragmentManager, "Edit qty dialog");
                }
            });
        }

        @Override
        public void onConfirm(int qty) {
            mAdapter.onConfirm(qty, getAdapterPosition());
        }
    }
}

package com.googleinterns.smb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.googleinterns.smb.R;
import com.googleinterns.smb.model.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Recycler view adapter for displaying products in scan text bottom sheet
 */
public class BottomSheetItemAdapter extends RecyclerView.Adapter<BottomSheetItemAdapter.ViewHolder> {

    /**
     * Callback interface for updating product status to host listener
     */
    public interface ProductStatusListener {
        void onProductDiscard(Product product);

        void onProductAdd(Product product);
    }

    private List<Product> mProducts;
    private ProductStatusListener mListener;

    public BottomSheetItemAdapter(List<Product> products, ProductStatusListener listener) {
        mProducts = products;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_bottom_sheet_product, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = mProducts.get(position);
        holder.mProductName.setText(product.getProductName());
        holder.mMRP.setText(product.getMRPString());
        Glide.with(holder.mProductImage.getContext())
                .load(product.getImageURL())
                .fitCenter()
                .into(holder.mProductImage);
        holder.mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onProductAdded(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    /**
     * Callback on product added from {@link ViewHolder#mAdd}.
     * Move to confirmed products and remove from current list display
     */
    private void onProductAdded(int position) {
        mListener.onProductAdd(mProducts.get(position));
        mProducts.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Add new product to list
     */
    public void addProduct(Product product) {
        int pos = mProducts.size();
        mProducts.add(product);
        notifyItemInserted(pos);
    }

    /**
     * Clears all items in recycler view
     */
    public void clear() {
        mProducts = new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mProductName;
        private TextView mMRP;
        private ImageView mProductImage;
        private Button mAdd;

        ViewHolder(@NonNull View itemView, final BottomSheetItemAdapter bottomSheetItemAdapter) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.text_view_product_name);
            mMRP = itemView.findViewById(R.id.text_view_mrp);
            mProductImage = itemView.findViewById(R.id.image_view_product);
            mAdd = itemView.findViewById(R.id.button_add_item);
        }
    }
}

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

public class ProductBottomSheetAdapter extends RecyclerView.Adapter<ProductBottomSheetAdapter.ViewHolder> {

    public interface ProductStatusListener {
        void onProductDiscard(Product product);

        void onProductAdd(Product product);
    }

    private List<Product> products;
    private ProductStatusListener listener;

    public ProductBottomSheetAdapter(List<Product> products, ProductStatusListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_product_card, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.mProductName.setText(product.getProductName());
        holder.mMRP.setText(product.getMRPString());
        Glide.with(holder.mProductImage.getContext())
                .load(product.getImageURL())
                .fitCenter()
                .into(holder.mProductImage);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private void onProductAdded(int position) {
        listener.onProductAdd(products.get(position));
        products.remove(position);
        notifyItemRemoved(position);
    }

    private void onProductDiscarded(int position) {
        listener.onProductDiscard(products.get(position));
        products.remove(position);
        notifyItemRemoved(position);
    }

    public void addProduct(Product product) {
        int pos = products.size();
        products.add(product);
        notifyItemInserted(pos);
    }

    /**
     * Clears all items in recycler view
     */
    public void clear() {
        products = new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mProductName;
        private TextView mMRP;
        private ImageView mProductImage;
        private ProductBottomSheetAdapter productBottomSheetAdapter;

        ViewHolder(@NonNull View itemView, final ProductBottomSheetAdapter productBottomSheetAdapter) {
            super(itemView);
            this.productBottomSheetAdapter = productBottomSheetAdapter;
            mProductName = itemView.findViewById(R.id.product_name);
            mMRP = itemView.findViewById(R.id.mrp);
            mProductImage = itemView.findViewById(R.id.product_image);
            Button mAddButton = itemView.findViewById(R.id.add);
            Button mDiscardButton = itemView.findViewById(R.id.discard);
            mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    productBottomSheetAdapter.onProductAdded(getAdapterPosition());
                }
            });
            mDiscardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    productBottomSheetAdapter.onProductDiscarded(getAdapterPosition());
                }
            });
        }
    }
}

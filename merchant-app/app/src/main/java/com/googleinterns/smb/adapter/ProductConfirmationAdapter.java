package com.googleinterns.smb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.googleinterns.smb.R;
import com.googleinterns.smb.fragment.EditPriceDialogFragment;
import com.googleinterns.smb.model.Product;

import java.util.List;


public class ProductConfirmationAdapter extends RecyclerView.Adapter<ProductConfirmationAdapter.ViewHolder> {

    private static final String TAG = ProductConfirmationAdapter.class.getName();
    private FragmentManager mFragmentManager;
    private List<Product> products;

    public ProductConfirmationAdapter(List<Product> products, FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        this.products = products;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view, this, mFragmentManager);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Product product = products.get(position);
        holder.mProductName.setText(product.getProductName());
        holder.mMRP.setText(product.getMRPString());
        holder.mDiscountedPrice.setText(product.getDiscountedPriceString());
        Glide.with(holder.mProductImage.getContext())
                .load(product.getImageURL())
                .fitCenter()
                .into(holder.mProductImage);
        holder.bind(product);
    }

    private void onDocumentRemoved(int position) {
        products.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * called by ViewHolder class on discount price edit confirm
     *
     * @param discountPrice updated discount price by user
     * @param position      position of card in recycler view
     */
    private void onConfirm(Double discountPrice, int position) {
        Product product = products.get(position);
        product.setDiscountedPrice(discountPrice);
        products.set(position, product);
        notifyItemChanged(position);
    }

    public List<Product> getProducts() {
        return products;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, EditPriceDialogFragment.EditPriceDialogInterface {

        private TextView mProductName;
        private TextView mMRP;
        private TextView mDiscountedPrice;
        private ImageView mProductImage;
        private ProductConfirmationAdapter mAdapter;
        private Product product;

        ViewHolder(@NonNull View itemView, ProductConfirmationAdapter adapter, final FragmentManager fragmentManager) {
            super(itemView);
            mAdapter = adapter;
            mProductName = itemView.findViewById(R.id.product_name);
            mMRP = itemView.findViewById(R.id.mrp);
            mDiscountedPrice = itemView.findViewById(R.id.discounted_price);
            mProductImage = itemView.findViewById(R.id.product_image);
            Button mEditProduct = itemView.findViewById(R.id.price_edit);
            Button mDeleteProduct = itemView.findViewById(R.id.delete_product);
            mDeleteProduct.setOnClickListener(this);
            mEditProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditPriceDialogFragment editPriceDialogFragment = new EditPriceDialogFragment(ViewHolder.this);
                    editPriceDialogFragment.show(fragmentManager, "Edit dialog");
                }
            });
        }

        void bind(Product product) {
            this.product = product;
        }

        @Override
        public void onClick(View v) {
            mAdapter.onDocumentRemoved(getAdapterPosition());
        }

        @Override
        public void onConfirm(Double discountPrice) {
            mAdapter.onConfirm(discountPrice, getAdapterPosition());
        }

        @Override
        public Double getMRP() {
            return product.getMRP();
        }
    }
}

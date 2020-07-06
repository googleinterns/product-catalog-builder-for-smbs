package com.googleinterns.smb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.googleinterns.smb.R;
import com.googleinterns.smb.fragment.EditPriceDialogFragment;
import com.googleinterns.smb.model.Product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Recycler view adapter for displaying products in {@link com.googleinterns.smb.ConfirmationActivity}
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> implements Filterable {

    public interface ProductActionListener {
        void onProductDeleted(Product product);

        void onPriceChanged(Product updatedProduct);
    }

    // Fragment manager required for displaying dialogs
    private FragmentManager mFragmentManager;
    private List<Product> mProducts;
    private List<Product> mProductsAll;
    private ProductActionListener mListener;
    private Filter filter;

    public ProductAdapter(List<Product> products, ProductActionListener listener, FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        mProducts = products;
        mProductsAll = new ArrayList<>(mProducts);
        mListener = listener;
        filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Product> filteredList = new ArrayList<>();
                if (constraint.toString().trim().isEmpty()) {
                    filteredList.addAll(mProductsAll);
                } else {
                    for (Product product : mProductsAll) {
                        if (product.getProductName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredList.add(product);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mProducts.clear();
                mProducts.addAll((Collection<? extends Product>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Product product = mProducts.get(position);
        holder.mProductName.setText(product.getProductName());
        holder.mMRP.setText(product.getMRPString());
        holder.mDiscountedPrice.setText(product.getDiscountedPriceString());
        Glide.with(holder.mProductImage.getContext())
                .load(product.getImageURL())
                .fitCenter()
                .into(holder.mProductImage);
        holder.mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditPriceDialogFragment editPriceDialogFragment = new EditPriceDialogFragment(new EditPriceDialogFragment.PriceConfirmationListener() {
                    @Override
                    public void onPriceConfirm(Double discountPrice) {
                        onConfirm(discountPrice, holder.getAdapterPosition());
                    }
                }, product.getMRP());
                editPriceDialogFragment.show(mFragmentManager, EditPriceDialogFragment.class.getName());
            }
        });
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDocumentRemoved(holder.getAdapterPosition());
            }
        });
    }

    /**
     * Callback from {@link ViewHolder#mDelete} on product remove action
     */
    private void onDocumentRemoved(int position) {
        Product product = mProducts.get(position);
        mProducts.remove(position);
        notifyItemRemoved(position);
        mListener.onProductDeleted(product);
    }

    /**
     * Called by {@link EditPriceDialogFragment.PriceConfirmationListener} on discount price edit confirm
     *
     * @param discountPrice updated discount price by user
     * @param position      position of card in recycler view
     */
    private void onConfirm(Double discountPrice, int position) {
        Product product = mProducts.get(position);
        product.setDiscountedPrice(discountPrice);
        mListener.onPriceChanged(product);
        mProducts.set(position, product);
        notifyItemChanged(position);
    }

    public List<Product> getProducts() {
        return mProducts;
    }


    @Override
    public Filter getFilter() {
        return filter;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mProductName;
        private TextView mMRP;
        private TextView mDiscountedPrice;
        private ImageView mProductImage;
        private Button mEdit;
        private Button mDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.text_view_product_name);
            mMRP = itemView.findViewById(R.id.text_view_mrp);
            mDiscountedPrice = itemView.findViewById(R.id.text_view_discounted_price);
            mProductImage = itemView.findViewById(R.id.image_view_product);
            mEdit = itemView.findViewById(R.id.button_edit_price);
            mDelete = itemView.findViewById(R.id.button_delete_product);
        }
    }
}

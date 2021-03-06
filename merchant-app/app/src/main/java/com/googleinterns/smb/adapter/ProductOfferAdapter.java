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
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.OfferActionListener;
import com.googleinterns.smb.fragment.ViewOfferDialogFragment;
import com.googleinterns.smb.model.Product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProductOfferAdapter extends RecyclerView.Adapter<ProductOfferAdapter.ViewHolder> implements Filterable {

    private OfferActionListener mListener;
    private List<Product> mProducts;
    private List<Product> mProductsAll;
    private FragmentManager mFragmentManager;
    private ViewOfferDialogFragment mViewOfferDialogFragment;
    private Filter mFilter;

    public ProductOfferAdapter(OfferActionListener listener, List<Product> products, FragmentManager fragmentManager) {
        mListener = listener;
        mProducts = products;
        mProductsAll = new ArrayList<>(products);
        mFragmentManager = fragmentManager;
        mFilter = new Filter() {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_product_offer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Product product = mProducts.get(position);
        holder.mProductName.setText(product.getProductName());
        holder.mMRP.setText(product.getMRPString());
        holder.mDiscountedPrice.setText(product.getDiscountedPriceString());
        Glide.with(holder.mProductImage.getContext())
                .load(product.getImageURL())
                .fitCenter()
                .into(holder.mProductImage);
        holder.mOfferCount.setText(CommonUtils.getOfferCountString(product.getNumOffers()));
        holder.mViewOffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewOfferDialogFragment = new ViewOfferDialogFragment(
                        product.getOffers(),
                        new ViewOfferDialogFragment.OffersDialogInterface() {
                            @Override
                            public void onAddOfferSelect() {
                                mViewOfferDialogFragment.dismiss();
                                mListener.onAddOfferSelect(OfferActionListener.PRODUCT_OFFER, holder.getAdapterPosition());
                            }

                            @Override
                            public void onEditOfferSelect(int offerIdx) {
                                mViewOfferDialogFragment.dismiss();
                                mListener.onEditOfferSelect(OfferActionListener.PRODUCT_OFFER, holder.getAdapterPosition(), offerIdx);
                            }

                            @Override
                            public void onDeleteOfferSelect(int offerIdx) {
                                mListener.onDeleteOfferSelect(OfferActionListener.PRODUCT_OFFER, holder.getAdapterPosition(), offerIdx);
                            }
                        });
                mViewOfferDialogFragment.show(mFragmentManager, ViewOfferDialogFragment.class.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProducts.size();
    }

    public List<Product> getProducts() {
        return mProducts;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mProductName;
        private TextView mMRP;
        private TextView mDiscountedPrice;
        private ImageView mProductImage;
        private TextView mOfferCount;
        private Button mViewOffers;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.text_view_product_name);
            mMRP = itemView.findViewById(R.id.text_view_mrp);
            mDiscountedPrice = itemView.findViewById(R.id.text_view_discounted_price);
            mProductImage = itemView.findViewById(R.id.image_view_product);
            mOfferCount = itemView.findViewById(R.id.text_view_offer_count);
            mViewOffers = itemView.findViewById(R.id.button_view_offers);
        }
    }
}

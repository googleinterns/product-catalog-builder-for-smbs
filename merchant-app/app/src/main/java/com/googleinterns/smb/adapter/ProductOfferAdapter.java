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
import com.googleinterns.smb.fragment.ViewOfferDialogFragment;
import com.googleinterns.smb.model.Product;

import java.util.List;

public class ProductOfferAdapter extends RecyclerView.Adapter<ProductOfferAdapter.ViewHolder> {
    public interface OfferActionListener {
        void onAddOfferSelect(int productIdx);

        void onEditOfferSelect(int productIdx, int offerIdx);

        void onDeleteOfferSelect(int productIdx, int offerIdx);
    }

    private OfferActionListener listener;
    private List<Product> products;
    private FragmentManager fragmentManager;
    private ViewOfferDialogFragment viewOfferDialogFragment;

    public ProductOfferAdapter(OfferActionListener listener, List<Product> products, FragmentManager fragmentManager) {
        this.listener = listener;
        this.products = products;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_product_offer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Product product = products.get(position);
        holder.mProductName.setText(product.getProductName());
        holder.mMRP.setText(product.getMRPString());
        holder.mDiscountedPrice.setText(product.getDiscountedPriceString());
        Glide.with(holder.mProductImage.getContext())
                .load(product.getImageURL())
                .fitCenter()
                .into(holder.mProductImage);
        holder.offersCount.setText(product.getOfferCountString());
        holder.viewOffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewOfferDialogFragment = new ViewOfferDialogFragment(
                        product.getOffers(),
                        new ViewOfferDialogFragment.OffersDialogInterface() {
                            @Override
                            public void onAddOfferSelect() {
                                viewOfferDialogFragment.dismiss();
                                listener.onAddOfferSelect(position);
                            }

                            @Override
                            public void onEditOfferSelect(int offerIdx) {
                                viewOfferDialogFragment.dismiss();
                                listener.onEditOfferSelect(position, offerIdx);
                            }

                            @Override
                            public void onDeleteOfferSelect(int offerIdx) {
                                listener.onDeleteOfferSelect(position, offerIdx);
                            }
                        });
                viewOfferDialogFragment.show(fragmentManager, ViewOfferDialogFragment.class.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public List<Product> getProducts() {
        return products;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mProductName;
        private TextView mMRP;
        private TextView mDiscountedPrice;
        private ImageView mProductImage;
        private TextView offersCount;
        private Button viewOffers;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.product_name);
            mMRP = itemView.findViewById(R.id.mrp);
            mDiscountedPrice = itemView.findViewById(R.id.discounted_price);
            mProductImage = itemView.findViewById(R.id.product_image);
            offersCount = itemView.findViewById(R.id.offers_count);
            viewOffers = itemView.findViewById(R.id.view_offers);
        }
    }
}

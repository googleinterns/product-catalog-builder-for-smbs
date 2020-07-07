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
import com.googleinterns.smb.model.Brand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BrandOfferAdapter extends RecyclerView.Adapter<BrandOfferAdapter.ViewHolder> implements Filterable {

    private OfferActionListener mListener;
    private List<Brand> mBrands;
    private List<Brand> mBrandsAll;
    private FragmentManager mFragmentManager;
    private ViewOfferDialogFragment mViewOfferDialogFragment;
    private Filter mFilter;

    public BrandOfferAdapter(OfferActionListener listener, List<Brand> brands, FragmentManager fragmentManager) {
        mListener = listener;
        mBrands = brands;
        mBrandsAll = new ArrayList<>(brands);
        mFragmentManager = fragmentManager;
        mFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Brand> filteredList = new ArrayList<>();
                if (constraint.toString().trim().isEmpty()) {
                    filteredList.addAll(mBrandsAll);
                } else {
                    for (Brand product : mBrandsAll) {
                        if (product.getBrandName().toLowerCase().contains(constraint.toString().toLowerCase())) {
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
                mBrands.clear();
                mBrands.addAll((Collection<? extends Brand>) results.values);
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_brand_offer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Brand brand = mBrands.get(position);
        holder.mBrandName.setText(brand.getBrandName());
        Glide.with(holder.mBrandImage.getContext())
                .load(brand.getBrandImageUrl())
                .fitCenter()
                .into(holder.mBrandImage);
        holder.mOfferCount.setText(CommonUtils.getOfferCountString(brand.getNumOffers()));
        holder.mViewOffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewOfferDialogFragment = new ViewOfferDialogFragment(
                        brand.getOffers(),
                        new ViewOfferDialogFragment.OffersDialogInterface() {
                            @Override
                            public void onAddOfferSelect() {
                                mViewOfferDialogFragment.dismiss();
                                mListener.onAddOfferSelect(OfferActionListener.BRAND_OFFER, holder.getAdapterPosition());
                            }

                            @Override
                            public void onEditOfferSelect(int offerIdx) {
                                mViewOfferDialogFragment.dismiss();
                                mListener.onEditOfferSelect(OfferActionListener.BRAND_OFFER, holder.getAdapterPosition(), offerIdx);
                            }

                            @Override
                            public void onDeleteOfferSelect(int offerIdx) {
                                mListener.onDeleteOfferSelect(OfferActionListener.BRAND_OFFER, holder.getAdapterPosition(), offerIdx);
                            }
                        });
                mViewOfferDialogFragment.show(mFragmentManager, ViewOfferDialogFragment.class.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBrands.size();
    }

    public List<Brand> getBrands() {
        return mBrands;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mBrandName;
        private ImageView mBrandImage;
        private TextView mOfferCount;
        private Button mViewOffers;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mBrandName = itemView.findViewById(R.id.text_view_brand_name);
            mBrandImage = itemView.findViewById(R.id.image_view_brand);
            mOfferCount = itemView.findViewById(R.id.text_view_offer_count);
            mViewOffers = itemView.findViewById(R.id.button_view_offers);
        }
    }
}

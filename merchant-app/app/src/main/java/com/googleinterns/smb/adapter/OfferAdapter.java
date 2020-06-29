package com.googleinterns.smb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.R;
import com.googleinterns.smb.model.Offer;

import java.util.List;

/**
 * Recycler view adapter to display offers in {@link com.googleinterns.smb.fragment.ViewOfferDialogFragment}
 */
public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {

    public interface OfferActionListener {
        void onEditOfferSelect(int position);

        void onDeleteOfferSelect(int position);
    }

    private OfferActionListener mListener;
    private List<Offer> mOffers;

    public OfferAdapter(OfferActionListener listener, List<Offer> offers) {
        mListener = listener;
        mOffers = offers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_offer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Offer offer = mOffers.get(position);
        holder.mDiscountValue.setText(offer.getOfferAmountString());
        holder.mValidity.setText(offer.getValidity());
        holder.mStatus.setText(offer.getStatus());
        holder.mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onEditOfferSelect(position);
            }
        });
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDeleteOfferSelect(position);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mOffers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mDiscountValue;
        private TextView mValidity;
        private View mDelete;
        private View mEdit;
        private TextView mStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mDiscountValue = itemView.findViewById(R.id.text_view_discount_value);
            mValidity = itemView.findViewById(R.id.text_view_validity);
            mDelete = itemView.findViewById(R.id.image_view_delete);
            mEdit = itemView.findViewById(R.id.image_view_edit);
            mStatus = itemView.findViewById(R.id.text_view_status);
        }
    }
}
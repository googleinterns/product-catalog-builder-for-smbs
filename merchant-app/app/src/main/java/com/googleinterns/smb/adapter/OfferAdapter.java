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

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {

    public interface OfferActionListener {
        void onEditOfferSelect(int position);

        void onDeleteOfferSelect(int position);
    }

    private OfferActionListener listener;
    private List<Offer> offers;

    public OfferAdapter(OfferActionListener listener, List<Offer> offers) {
        this.listener = listener;
        this.offers = offers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Offer offer = offers.get(position);
        holder.discountValue.setText(offer.getOfferAmountString());
        holder.validity.setText(offer.getValidity());
        holder.status.setText(offer.getStatus());
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEditOfferSelect(position);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteOfferSelect(position);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView discountValue;
        private TextView validity;
        private View delete;
        private View edit;
        private TextView status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            discountValue = itemView.findViewById(R.id.discount_value);
            validity = itemView.findViewById(R.id.validity);
            delete = itemView.findViewById(R.id.delete);
            edit = itemView.findViewById(R.id.edit);
            status = itemView.findViewById(R.id.status);
        }
    }
}

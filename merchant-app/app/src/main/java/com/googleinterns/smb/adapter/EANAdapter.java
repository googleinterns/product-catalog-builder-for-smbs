package com.googleinterns.smb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.googleinterns.smb.R;


public class EANAdapter extends FirestoreAdapter<EANAdapter.EANViewHolder> {


    public EANAdapter(Query query) {
        super(query);
    }

    @NonNull
    @Override
    public EANViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new EANViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull EANViewHolder holder, int position) {
        holder.bind(getSnapshot(position));
    }

    static class EANViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mProductName;
        private TextView mMRP;
        private TextView mDiscountedPrice;
        private ImageView mProductImage;

        EANAdapter mAdapter;

        EANViewHolder(@NonNull View itemView, EANAdapter adapter) {
            super(itemView);
            mProductName = itemView.findViewById(R.id.product_name);
            mMRP = itemView.findViewById(R.id.mrp);
            mDiscountedPrice = itemView.findViewById(R.id.discounted_price);
            mProductImage = itemView.findViewById(R.id.product_image);
            mAdapter = adapter;
        }

        void bind(final DocumentSnapshot documentSnapshot) {
            String productName = documentSnapshot.getString("product_name");
            mProductName.setText(productName);
            String mrp = documentSnapshot.getDouble("MRP").toString();
            mMRP.setText(mrp);
            mDiscountedPrice.setText(mrp);
            String imageURL = documentSnapshot.getString("image_url");
            Glide.with(mProductImage.getContext())
                    .load(imageURL)
                    .fitCenter()
                    .into(mProductImage);
        }

        @Override
        public void onClick(View v) {
            mAdapter.onDocumentRemoved(getAdapterPosition());
        }

    }
}
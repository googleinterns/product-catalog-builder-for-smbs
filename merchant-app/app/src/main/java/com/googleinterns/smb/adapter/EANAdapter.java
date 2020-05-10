package com.googleinterns.smb.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.R;

import java.util.List;


public class EANAdapter extends RecyclerView.Adapter<EANAdapter.EANViewHolder> {

    List<String> mBarcodes;

    public EANAdapter(List<String> barcodes) {
        mBarcodes = barcodes;
    }

    @NonNull
    @Override
    public EANViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new EANViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull EANViewHolder holder, int position) {
        holder.bind(mBarcodes.get(position));
    }

    @Override
    public int getItemCount() {
        return mBarcodes.size();
    }

    private void onDelete(int position) {
        Log.d("EANAdapter", position + "");
        mBarcodes.remove(position);
        notifyItemRemoved(position);
    }

    static class EANViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mtextEAN;
        EANAdapter mAdapter;

        EANViewHolder(@NonNull View itemView, EANAdapter adapter) {
            super(itemView);
            mtextEAN = itemView.findViewById(R.id.ean_field);
            mAdapter = adapter;
            itemView.findViewById(R.id.delete_btn).setOnClickListener(this);
        }

        void bind(String barcode) {
            mtextEAN.setText(barcode);
        }

        @Override
        public void onClick(View v) {
            mAdapter.onDelete(getAdapterPosition());
        }

    }
}

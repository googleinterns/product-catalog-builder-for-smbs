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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.googleinterns.smb.R;
import com.googleinterns.smb.fragment.EditPriceDialogFragment;
import com.googleinterns.smb.model.Product;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class EANAdapter extends FirestoreAdapter<EANAdapter.EANViewHolder> {


    private static final String TAG = "EAN Adapter";
    private FragmentManager mFragmentManager;
    private List<Product> products = new ArrayList<>();

    public EANAdapter(Query query, FragmentManager fragmentManager) {
        super(query);
        mFragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public EANViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new EANViewHolder(view, this, mFragmentManager);
    }

    @Override
    public void onBindViewHolder(@NonNull EANViewHolder holder, final int position) {
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

    @Override
    protected void onDocumentAdded(@NotNull DocumentChange change, int newIdx) {
        DocumentSnapshot documentSnapshot = change.getDocument();
        Product product = new Product(documentSnapshot);
        products.add(newIdx, product);
        super.onDocumentAdded(change, newIdx);
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

    static class EANViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, EditPriceDialogFragment.OptionSelectListener {

        private TextView mProductName;
        private TextView mMRP;
        private TextView mDiscountedPrice;
        private ImageView mProductImage;
        private EANAdapter mAdapter;
        private Product product;

        EANViewHolder(@NonNull View itemView, EANAdapter adapter, final FragmentManager fragmentManager) {
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
                    EditPriceDialogFragment editPriceDialogFragment = new EditPriceDialogFragment(EANViewHolder.this, product.getMRP());
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
    }
}
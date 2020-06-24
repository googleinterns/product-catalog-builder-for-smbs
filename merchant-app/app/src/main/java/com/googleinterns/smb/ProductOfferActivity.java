package com.googleinterns.smb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.googleinterns.smb.adapter.ProductOfferAdapter;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Offer;
import com.googleinterns.smb.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductOfferActivity extends MainActivity implements ProductOfferAdapter.OfferActionListener {

    private View contentView;
    private ProductOfferAdapter productOfferAdapter;
    private List<Product> products;
    // Product index related to last offer change
    private int productIdx;
    // Offer index in a product related to last offer change
    private int offerIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Offers");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.activity_product_offer, null, false);
        container.addView(contentView, 0);

        TextInputEditText searchText = contentView.findViewById(R.id.search_edit_text);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
        Merchant merchant = Merchant.getInstance();
        merchant.fetchProducts(new Merchant.OnProductFetchedListener() {
            @Override
            public void onProductFetched(List<Product> products) {
                ProductOfferActivity.this.products = products;
                initRecyclerView();
            }
        });
    }

    private void filter(String searchText) {
        if (products == null) {
            return;
        }
        if (searchText.trim().equals("")) {
            productOfferAdapter.setProducts(products);
            return;
        }
        List<Product> filteredList = new ArrayList<>();
        for (Product product : products) {
            if (product.getProductName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productOfferAdapter.setProducts(filteredList);
    }

    private void initRecyclerView() {
        productOfferAdapter = new ProductOfferAdapter(this, products, getSupportFragmentManager());
        RecyclerView recyclerView = contentView.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(productOfferAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }


    @Override
    public void onAddOfferSelect(int productIdx) {
        this.productIdx = productIdx;
        Intent intent = new Intent(this, OfferDetailsActivity.class);
        intent.putExtra("requestCode", OfferDetailsActivity.RC_ADD_OFFER);
        startActivityForResult(intent, OfferDetailsActivity.RC_ADD_OFFER);
    }

    @Override
    public void onEditOfferSelect(int productIdx, int offerIdx) {
        this.productIdx = productIdx;
        this.offerIdx = offerIdx;
        Intent intent = new Intent(this, OfferDetailsActivity.class);
        intent.putExtra("requestCode", OfferDetailsActivity.RC_EDIT_OFFER);
        Offer offer = productOfferAdapter.getProducts().get(productIdx).getOffers().get(offerIdx);
        intent.putExtra("offer", offer);
        startActivityForResult(intent, OfferDetailsActivity.RC_EDIT_OFFER);
    }

    @Override
    public void onDeleteOfferSelect(int productIdx, int offerIdx) {
        Product product = productOfferAdapter.getProducts().get(productIdx);
        product.getOffers().remove(offerIdx);
        FirebaseUtils.updateProductOffers(product);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == OfferDetailsActivity.RC_ADD_OFFER) {
                Offer newOffer = (Offer) data.getSerializableExtra("offer");
                Product product = productOfferAdapter.getProducts().get(productIdx);
                product.getOffers().add(newOffer);
                FirebaseUtils.updateProductOffers(product);
            } else if (requestCode == OfferDetailsActivity.RC_EDIT_OFFER) {
                Offer updatedOffer = (Offer) data.getSerializableExtra("offer");
                Product product = productOfferAdapter.getProducts().get(productIdx);
                product.getOffers().set(offerIdx, updatedOffer);
                FirebaseUtils.updateProductOffers(product);
            }
        }
    }
}

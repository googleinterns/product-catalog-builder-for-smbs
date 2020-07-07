package com.googleinterns.smb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.googleinterns.smb.adapter.BrandOfferAdapter;
import com.googleinterns.smb.adapter.ProductOfferAdapter;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.common.OfferActionListener;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Brand;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Offer;
import com.googleinterns.smb.model.Product;

import java.util.List;

/**
 * Activity to add, remove and view offers on individual items.
 */
public class ProductOfferActivity extends MainActivity implements OfferActionListener {

    private View mContentView;
    private ProductOfferAdapter mProductOfferAdapter;
    private BrandOfferAdapter mBrandOfferAdapter;
    private List<Product> mProducts;
    // Item index related to last offer change
    private int mItemIdx;
    // Offer index in an item related to last offer change
    private int mOfferIdx;
    // Last offer query type [PRODUCT_OFFER or BRAND_OFFER]
    private int mType;
    private RecyclerView mRecyclerViewProductOffers;
    private RecyclerView mRecyclerViewBrandOffers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Offers");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = inflater.inflate(R.layout.activity_product_offer, null, false);
        mContainer.addView(mContentView, 0);

        TextInputEditText searchText = mContentView.findViewById(R.id.edit_text_search);
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
        MaterialButtonToggleGroup toggleGroupOfferType = findViewById(R.id.toggle_offer_type_filter);
        toggleGroupOfferType.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.button_product_offer) {
                    if (isChecked) {
                        mRecyclerViewProductOffers.setVisibility(View.VISIBLE);
                    } else {
                        mRecyclerViewProductOffers.setVisibility(View.GONE);
                    }
                } else if (checkedId == R.id.button_brand_offer) {
                    if (isChecked) {
                        mRecyclerViewBrandOffers.setVisibility(View.VISIBLE);
                    } else {
                        mRecyclerViewBrandOffers.setVisibility(View.GONE);
                    }
                }
            }
        });

        Merchant merchant = Merchant.getInstance();
        merchant.fetchProducts(new Merchant.OnProductFetchedListener() {
            @Override
            public void onProductFetched(List<Product> products) {
                initProductOffersRecyclerView(products);
            }
        });
        merchant.fetchBrands(new Merchant.OnBrandFetchedListener() {
            @Override
            public void onBrandFetched(List<Brand> brands) {
                initBrandOffersRecyclerView(brands);
            }
        });
    }

    private void filter(String searchText) {
        mProductOfferAdapter.getFilter().filter(searchText);
        mBrandOfferAdapter.getFilter().filter(searchText);
    }

    private void initProductOffersRecyclerView(List<Product> products) {
        mProductOfferAdapter = new ProductOfferAdapter(this, products, getSupportFragmentManager());
        mRecyclerViewProductOffers = mContentView.findViewById(R.id.recycler_view_product_offers);
        mRecyclerViewProductOffers.setAdapter(mProductOfferAdapter);
        mRecyclerViewProductOffers.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    private void initBrandOffersRecyclerView(List<Brand> brands) {
        mBrandOfferAdapter = new BrandOfferAdapter(this, brands, getSupportFragmentManager());
        mRecyclerViewBrandOffers = mContentView.findViewById(R.id.recycler_view_brand_offers);
        mRecyclerViewBrandOffers.setAdapter(mBrandOfferAdapter);
        mRecyclerViewBrandOffers.setLayoutManager(new GridLayoutManager(this, 2) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    @Override
    public void onAddOfferSelect(int type, int itemIdx) {
        mItemIdx = itemIdx;
        mType = type;
        Intent intent = new Intent(this, OfferDetailsActivity.class);
        intent.putExtra("requestCode", OfferDetailsActivity.RC_ADD_OFFER);
        startActivityForResult(intent, OfferDetailsActivity.RC_ADD_OFFER);
    }

    @Override
    public void onEditOfferSelect(int type, int itemIdx, int offerIdx) {
        mItemIdx = itemIdx;
        mOfferIdx = offerIdx;
        mType = type;
        Intent intent = new Intent(this, OfferDetailsActivity.class);
        intent.putExtra("requestCode", OfferDetailsActivity.RC_EDIT_OFFER);
        Offer offer;
        if (type == OfferActionListener.PRODUCT_OFFER) {
            offer = mProductOfferAdapter.getProducts().get(itemIdx).getOffers().get(offerIdx);
        } else {
            offer = mBrandOfferAdapter.getBrands().get(itemIdx).getOffers().get(offerIdx);
        }
        intent.putExtra("offer", offer);
        startActivityForResult(intent, OfferDetailsActivity.RC_EDIT_OFFER);
    }

    @Override
    public void onDeleteOfferSelect(int type, int itemIdx, int offerIdx) {
        if (type == OfferActionListener.PRODUCT_OFFER) {
            Product product = mProductOfferAdapter.getProducts().get(itemIdx);
            product.getOffers().remove(offerIdx);
            FirebaseUtils.updateProductOffers(product);
        } else {
            Brand brand = mBrandOfferAdapter.getBrands().get(itemIdx);
            brand.getOffers().remove(offerIdx);
            FirebaseUtils.updateBrandOffers(brand);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == OfferDetailsActivity.RC_ADD_OFFER) {
                Offer newOffer = (Offer) data.getSerializableExtra("offer");
                if (mType == OfferActionListener.PRODUCT_OFFER) {
                    Product product = mProductOfferAdapter.getProducts().get(mItemIdx);
                    product.getOffers().add(newOffer);
                    FirebaseUtils.updateProductOffers(product);
                } else {
                    Brand brand = mBrandOfferAdapter.getBrands().get(mItemIdx);
                    brand.getOffers().add(newOffer);
                    FirebaseUtils.updateBrandOffers(brand);
                }
                UIUtils.showToast(this, "Offer added");
            } else if (requestCode == OfferDetailsActivity.RC_EDIT_OFFER) {
                Offer updatedOffer = (Offer) data.getSerializableExtra("offer");
                if (mType == OfferActionListener.PRODUCT_OFFER) {
                    Product product = mProductOfferAdapter.getProducts().get(mItemIdx);
                    product.getOffers().set(mOfferIdx, updatedOffer);
                    FirebaseUtils.updateProductOffers(product);
                } else {
                    Brand brand = mBrandOfferAdapter.getBrands().get(mItemIdx);
                    brand.getOffers().set(mOfferIdx, updatedOffer);
                    FirebaseUtils.updateBrandOffers(brand);
                }
                UIUtils.showToast(this, "Offer updated");
            }
        }
    }
}

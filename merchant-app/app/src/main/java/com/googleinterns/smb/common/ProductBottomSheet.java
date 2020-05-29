package com.googleinterns.smb.common;

import com.googleinterns.smb.adapter.ProductAdapter;
import com.googleinterns.smb.model.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class acts as a view model for product display in ScanTextActivity bottom sheet
 */
public class ProductBottomSheet implements ProductAdapter.ProductStatusListener {

    // set of products already confirmed by user to add
    private Set<Product> mAdded = new HashSet<>();

    // set of products denied by user
    private Set<Product> mDiscarded = new HashSet<>();

    // set of products now present in recycler view
    private Set<Product> mPresent = new HashSet<>();

    private ProductAdapter productAdapter;

    public ProductBottomSheet(ProductAdapter productAdapter) {
        this.productAdapter = productAdapter;
    }

    /**
     * Add new products to recycler view
     *
     * @param products list of product detected by scanner
     */
    public void addProducts(List<Product> products) {
        for (Product product : products) {
            // ignore if already has been considered by user
            if (mAdded.contains(product) || mDiscarded.contains(product) || mPresent.contains(product)) {
                continue;
            }
            // add product to bottom sheet recycler view
            productAdapter.addProduct(product);
            mPresent.add(product);
        }
    }

    public List<Product> getSelectedProducts() {
        return new ArrayList<>(mAdded);
    }

    @Override
    public void onProductDiscard(Product product) {
        mDiscarded.add(product);
    }

    @Override
    public void onProductAdd(Product product) {
        mAdded.add(product);
    }

    /**
     * Clears all products in the recycler view
     */
    public void clear() {
        mPresent.clear();
        productAdapter.clear();
    }
}

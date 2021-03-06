package com.googleinterns.smb;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.googleinterns.smb.adapter.ProductAdapter;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Product;

import java.io.Serializable;
import java.util.List;

/**
 * Confirmation Activity to display all scanned products.
 * Merchant can set his own price for products, remove products and add all to inventory.
 */
public class ConfirmationActivity extends AppCompatActivity implements
        Merchant.OnDataUpdatedListener,
        FirebaseUtils.BarcodeProductQueryListener,
        ProductAdapter.ProductActionListener {

    // Recycler view adapter for displaying products
    private ProductAdapter mProductAdapter;
    private Merchant merchant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        setTitle(getString(R.string.products));
        // Get current merchant
        merchant = Merchant.getInstance();

        // Check the type of data in intent
        if (getIntent().hasExtra(CommonUtils.DETECTED_BARCODES)) {
            // Query for products from barcodes
            FirebaseUtils.queryProducts(this, CommonUtils.getBarcodes(getIntent()));
        } else if (getIntent().hasExtra(CommonUtils.DETECTED_PRODUCTS)) {
            initRecyclerView(CommonUtils.getProducts(getIntent()));
        } else {
            throw new AssertionError("Invalid data received in confirmation activity");
        }

        // FAB for adding all products in list to inventory
        FloatingActionButton mFABDone = findViewById(R.id.fab_done);
        mFABDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProducts();
            }
        });
    }

    /**
     * Add all confirmed products to merchant's inventory
     */
    private void addProducts() {
        List<Product> products = mProductAdapter.getProducts();
        if (products.isEmpty()) {
            UIUtils.showToast(this, "No products to add");
            startActivity(InventoryActivity.makeIntent(this));
            return;
        }
        // Add products to database
        merchant.addProducts(this, products);
    }

    /**
     * Fetch data and initialise recycler view
     */
    private void initRecyclerView(List<Product> products) {
        // Hide progress bar
        View view = findViewById(R.id.progress_bar);
        view.setVisibility(View.GONE);
        if (products.isEmpty()) {
            displayMessageOnEmpty();
            return;
        }
        mProductAdapter = new ProductAdapter(products, this, getSupportFragmentManager());
        // Initialize recycler view
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mProductAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    private void displayMessageOnEmpty() {
        View emptyInventory = findViewById(R.id.empty_scan);
        emptyInventory.setVisibility(View.VISIBLE);
    }

    /**
     * Make intent for confirmation activity.
     * To display scanned products and confirmation to add
     *
     * @param context  initiater activity
     * @param barcodes list of detected product barcodes
     * @return intent to start Confirmation activity
     */
    public static Intent makeIntentFromBarcodes(Context context, List<String> barcodes) {
        return new Intent(context, ConfirmationActivity.class)
                .putExtra(CommonUtils.DETECTED_BARCODES, (Serializable) barcodes);
    }

    /**
     * This method creates intent for confirmation using list of products to display
     */
    public static Intent makeIntentFromProducts(Context context, List<Product> products) {
        return new Intent(context, ConfirmationActivity.class)
                .putExtra(CommonUtils.DETECTED_PRODUCTS, (Serializable) products);
    }

    /**
     * Callback from {@link Merchant#addProducts(Merchant.OnDataUpdatedListener, List)}, on successful addition of products
     */
    @Override
    public void onDataUpdateSuccess() {
        UIUtils.showToast(this, "Products added to inventory");
        startActivity(InventoryActivity.makeIntent(this));
    }

    /**
     * Callback from {@link Merchant#addProducts(Merchant.OnDataUpdatedListener, List)}, on database update failure
     */
    @Override
    public void onDataUpdateFailure() {
        UIUtils.showToast(this, "Error: update failed");
    }

    /**
     * Callback from {@link FirebaseUtils#queryProducts(Context, List)}
     *
     * @param products corresponding to scanned barcode EANs
     */
    @Override
    public void onQueryComplete(List<Product> products) {
        initRecyclerView(products);
    }

    @Override
    public void onProductDeleted(Product product) {
        if (mProductAdapter.getItemCount() == 0) {
            displayMessageOnEmpty();
        }
    }

    @Override
    public void onPriceChanged(Product updatedProduct) {
    }
}


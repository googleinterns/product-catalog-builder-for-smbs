package com.googleinterns.smb;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.googleinterns.smb.adapter.EANAdapter;
import com.googleinterns.smb.adapter.ProductConfirmationAdapter;
import com.googleinterns.smb.model.Product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfirmationActivity extends AppCompatActivity {

    private static final String TAG = ConfirmationActivity.class.getName();
    public static final String DETECTED_BARCODES = "DETECTED_BARCODES";
    public static final String DETECTED_PRODUCTS = "DETECTED_PRODUCTS";

    private EANAdapter mEANAdapter;
    private ProductConfirmationAdapter mProductAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        setTitle("Products");
        initRecyclerView();
    }

    /**
     * Fetch data and initialise recycler view
     */
    private void initRecyclerView() {

        RecyclerView.Adapter adapter;
        if (getIntent().hasExtra(DETECTED_BARCODES)) {
            List<String> barcodes;
            try {
                barcodes = getBarcodes();
            } catch (AssertionError ae) {
                barcodes = new ArrayList<>();
            }
            adapter = getAdapterFromBarcodes(barcodes);
        } else if (getIntent().hasExtra(DETECTED_PRODUCTS)) {
            // load ProductConfirmationAdapter (render products directly)
            List<Product> products = getProducts();
            adapter = getAdapterFromProducts(products);
            Log.d(TAG, "Product list");
            for (int i = 0; i < products.size(); i++) {
                Log.d(TAG, "Product: " + products.get(i).getProductName());
            }
        } else {
            return;
        }
        // initialize recycler view
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    /**
     * Create recycler view adapter directly using barcodes. EANAdapter inherits from FirestoreAdapter and displays realtime data.
     *
     * @param barcodes list of retrieved barcodes
     * @return Recycler view adapter
     * @throws AssertionError
     */
    private RecyclerView.Adapter getAdapterFromBarcodes(List<String> barcodes) throws AssertionError {
        if ((barcodes.size() == 0)) throw new AssertionError("Barcode list must be non-empty");
        Query query = FirebaseFirestore.getInstance().collection("products");
        // filter products containing these barcodes
        query = query.whereIn("EAN", barcodes);
        mEANAdapter = new EANAdapter(query, getSupportFragmentManager());
        return mEANAdapter;
    }

    /**
     * Create recycler view adapter from products
     *
     * @param products list of retrieved products
     * @return recycler view adapter
     */
    private RecyclerView.Adapter getAdapterFromProducts(List<Product> products) {
        mProductAdapter = new ProductConfirmationAdapter(products, getSupportFragmentManager());
        return mProductAdapter;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mEANAdapter != null) {
            mEANAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mEANAdapter != null) {
            mEANAdapter.stopListening();
        }
    }

    /**
     * utility to get barcodes from serialised data in intent.
     */
    private List<String> getBarcodes() {
        List<String> barcodes;
        try {
            barcodes = (List<String>) getIntent().getSerializableExtra(DETECTED_BARCODES);
            if (barcodes == null) {
                barcodes = new ArrayList<>();
            }
        } catch (Exception e) {
            barcodes = new ArrayList<>();
            Log.e(TAG, "Error loading barcodes from intent");
        }
        return barcodes;
    }

    /**
     * utility to get products from serialised data in intent.
     */
    private List<Product> getProducts() {
        List<Product> products;
        try {
            products = (List<Product>) getIntent().getSerializableExtra(DETECTED_PRODUCTS);
            if (products == null) {
                products = new ArrayList<>();
            }
        } catch (Exception e) {
            products = new ArrayList<>();
            Log.e(TAG, "Error loading products from intent");
        }
        return products;
    }

    /**
     * Make intent for confirmation activity.
     * To display scanned products and confirmation to add
     *
     * @param context  initiater activity
     * @param barcodes list of detected barcodes to display
     * @return intent to start Confirmation activity
     */
    public static Intent makeIntentFromBarcodes(Context context, List<String> barcodes) {
        return new Intent(context, ConfirmationActivity.class)
                .putExtra(DETECTED_BARCODES, (Serializable) barcodes);
    }

    public static Intent makeIntentFromProducts(Context context, List<Product> products) {
        return new Intent(context, ConfirmationActivity.class)
                .putExtra(DETECTED_PRODUCTS, (Serializable) products);
    }
}

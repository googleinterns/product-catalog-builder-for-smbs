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
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.model.Product;

import java.io.Serializable;
import java.util.List;

public class ConfirmationActivity extends AppCompatActivity {

    private static final String TAG = ConfirmationActivity.class.getName();

    private EANAdapter mEANAdapter;

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
        if (getIntent().hasExtra(CommonUtils.DETECTED_BARCODES)) {
            List<String> barcodes = CommonUtils.getBarcodes(getIntent());
            if (barcodes.size() == 0)
                return;
            adapter = getAdapterFromBarcodes(barcodes);
        } else if (getIntent().hasExtra(CommonUtils.DETECTED_PRODUCTS)) {
            // load ProductConfirmationAdapter (render products directly)
            List<Product> products = CommonUtils.getProducts(getIntent());
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
        return new ProductConfirmationAdapter(products, getSupportFragmentManager());
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
     * Make intent for confirmation activity.
     * To display scanned products and confirmation to add
     *
     * @param context  initiater activity
     * @param barcodes list of detected barcodes to display
     * @return intent to start Confirmation activity
     */
    public static Intent makeIntentFromBarcodes(Context context, List<String> barcodes) {
        return new Intent(context, ConfirmationActivity.class)
                .putExtra(CommonUtils.DETECTED_BARCODES, (Serializable) barcodes);
    }

    public static Intent makeIntentFromProducts(Context context, List<Product> products) {
        return new Intent(context, ConfirmationActivity.class)
                .putExtra(CommonUtils.DETECTED_PRODUCTS, (Serializable) products);
    }
}


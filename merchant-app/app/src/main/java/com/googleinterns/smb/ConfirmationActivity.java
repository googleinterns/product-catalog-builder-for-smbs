package com.googleinterns.smb;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.googleinterns.smb.adapter.EANAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfirmationActivity extends AppCompatActivity {

    private static final String TAG = ConfirmationActivity.class.getName();
    public static final String DETECTED_BARCODES = "DETECTED_BARCODES";

    private EANAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        setTitle("Products");
        initRecyclerView();
    }

    /**
     * Fetch data from firebase and initialise recycler view
     */
    private void initRecyclerView() {
        // get barcodes from launcher activity
        List<String> barcodes = getBarcodes();

        Query query = FirebaseFirestore.getInstance().collection("products");
        // filter products containing these barcodes
        query = query.whereIn("EAN", barcodes);


        // initialize recycler view
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new EANAdapter(query);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
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
        }
        return barcodes;
    }

    /**
     * Make intent for confirmation activity.
     *
     * @param context  initiater activity
     * @param barcodes list of detected barcodes to display
     * @return intent to start Confirmation activity
     */
    public static Intent makeIntent(Context context, List<String> barcodes) {
        return new Intent(context, ConfirmationActivity.class)
                .putExtra(DETECTED_BARCODES, (Serializable) barcodes);
    }
}

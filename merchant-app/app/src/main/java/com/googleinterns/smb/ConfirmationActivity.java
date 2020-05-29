package com.googleinterns.smb;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.EANAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfirmationActivity extends AppCompatActivity {

    private static final String TAG = ConfirmationActivity.class.getName();
    public static final String DETECTED_BARCODES = "DETECTED_BARCODES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        setTitle("Products");

        // get barcodes from launcher activity
        List<String> barcodes = getBarcodes();
        // initialize recycler view
        RecyclerView recyclerView = findViewById(R.id.list_item);
        EANAdapter adapter = new EANAdapter(barcodes);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    /**
     * utility to get barcodes from serialised data in intent.
     */
    private List<String> getBarcodes() {
        List<String> barcodes = (List<String>) getIntent().getSerializableExtra(DETECTED_BARCODES);
        if (barcodes == null) {
            barcodes = new ArrayList<>();
        }
        return barcodes;
    }

    /**
     * Make intent for confirmation activity.
     * To display scanned products and confirm to add
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

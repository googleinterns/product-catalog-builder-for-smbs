package com.googleinterns.smb;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.EANAdapter;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationActivity extends AppCompatActivity {

    private static final String TAG = ConfirmationActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        setTitle("Products");

        // get barcodes from launcher activity
        List<String> barcodes = getBarcodes();
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

    private List<String> getBarcodes() {
        List<String> barcodes = (List<String>) getIntent().getSerializableExtra(ScanActivity.DETECTED_BARCODES);
        if (barcodes == null) {
            barcodes = new ArrayList<>();
        }
        return barcodes;
    }
}

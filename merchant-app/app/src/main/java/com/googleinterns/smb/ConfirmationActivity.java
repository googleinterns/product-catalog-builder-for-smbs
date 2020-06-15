package com.googleinterns.smb;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ConfirmationActivity extends AppCompatActivity {

    private static final String TAG = ConfirmationActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        List<String> barcodes = (List<String>) getIntent().getSerializableExtra(ScanActivity.DETECTED_BARCODES);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, barcodes);

        ListView listView = findViewById(R.id.list_item);
        listView.setAdapter(adapter);
    }
}

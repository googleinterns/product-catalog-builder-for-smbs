package com.googleinterns.smb;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        setTitle("Products");
        List<String> barcodes = (List<String>) getIntent().getSerializableExtra(ScanActivity.DETECTED_BARCODES);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.card_item, R.id.ean_field, barcodes);

        ListView listView = findViewById(R.id.list_item);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                LayoutInflater layoutInflater = getLayoutInflater();
                View item = layoutInflater.inflate(R.layout.card_item, (ViewGroup) view);
                TextView textView = item.findViewById(R.id.ean_field);
                String barcode = textView.getText().toString();
                Log.d(TAG, barcode);
            }
        });
    }
}
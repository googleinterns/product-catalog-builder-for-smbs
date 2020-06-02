package com.googleinterns.smb;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.OrderAdapter;
import com.googleinterns.smb.model.BillItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrderDisplayActivity extends AppCompatActivity implements OrderAdapter.PriceChangeListener {

    private static final String TAG = OrderDisplayActivity.class.getName();

    private TextView mTotalPrice;
    private TextView mTextViewCustomerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_display);
        mTotalPrice = findViewById(R.id.total_price);
        mTextViewCustomerName = findViewById(R.id.text_customer_name);
        String jsonDataString = getIntent().getStringExtra("data");
        String items = getIntent().getStringExtra("items");
        if (items != null) {
            try {
                JSONArray jsonArray = new JSONArray(items);
                List<BillItem> billitems = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    BillItem billItem = new BillItem(jsonArray.getJSONObject(i));
                    billitems.add(billItem);
                }
                initRecyclerView(billitems);
            } catch (JSONException e) {
                Log.e(TAG, "Unable to parse JSON", e);
            }
        } else {
            Log.e(TAG, "No data received");
        }
        if (jsonDataString != null) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(jsonDataString);
                String customerName = jsonObject.getString("customer_name");
                mTextViewCustomerName.setText(String.format("%s's order", customerName));
            } catch (JSONException e) {
                Log.e(TAG, "Unable to parse json", e);
            }
        } else {
            Log.e(TAG, "No data received");
        }
    }

    private void initRecyclerView(List<BillItem> billItems) {
        OrderAdapter orderAdapter = new OrderAdapter(this, billItems, getSupportFragmentManager());
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(orderAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onPriceChange(double newTotalPrice) {
        mTotalPrice.setText(String.format("%.2f", newTotalPrice));
    }
}

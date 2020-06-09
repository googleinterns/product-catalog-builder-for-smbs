package com.googleinterns.smb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.googleinterns.smb.adapter.ProductAdapter;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.model.Product;

import java.util.List;

public class InventoryActivity extends MainActivity implements FirebaseUtils.OnProductReceivedListener {

    private static final String TAG = InventoryActivity.class.getName();

    private View contentView;
    private ProductAdapter mProductAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Inventory");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.activity_inventory, null, false);
        container.addView(contentView, 0);
        FloatingActionButton fabAddProduct = contentView.findViewById(R.id.add_product);
        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InventoryActivity.this.addProduct(v);
            }
        });
        FirebaseUtils.getInventory(this);
    }

    @Override
    public void onProductReceived(List<Product> products) {
        initRecyclerView(products);
    }

    /**
     * Fetch data and initialise recycler view
     */
    private void initRecyclerView(List<Product> products) {
        // hide progress bar
        View view = findViewById(R.id.progressBar);
        view.setVisibility(View.GONE);
        if (products.isEmpty()) {
            View emptyInventory = contentView.findViewById(R.id.empty_inventory);
            emptyInventory.setVisibility(View.VISIBLE);
            return;
        }
        mProductAdapter = new ProductAdapter(products, getSupportFragmentManager());
        // initialize recycler view
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mProductAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, InventoryActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }
}

package com.googleinterns.smb;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.ProductBottomSheetAdapter;
import com.googleinterns.smb.common.ProductBottomSheet;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Product;
import com.googleinterns.smb.textrecognition.TextRecognitionProcessor;

import java.util.ArrayList;
import java.util.List;

public class ScanTextActivity extends ScanActivity implements ProductBottomSheetAdapter.ProductStatusListener, TextRecognitionProcessor.OnProductFoundListener {

    private TextRecognitionProcessor mDetector;
    private ProductBottomSheet productBottomSheet;

    @Override
    protected void initViews() {
        setTitle("Scan Text");
        setContentView(R.layout.activity_scan_text);
        TextView helpText = findViewById(R.id.help_text);
        helpText.setText(R.string.point_at_product_text_to_scan);
        initRecyclerView();
    }

    private void initRecyclerView() {
        List<Product> products = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.product_recycler_view);
        ProductBottomSheetAdapter productBottomSheetAdapter = new ProductBottomSheetAdapter(products, this);
        productBottomSheet = new ProductBottomSheet(productBottomSheetAdapter);
        recyclerView.setAdapter(productBottomSheetAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
        Button clearButton = findViewById(R.id.clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productBottomSheet.clear();
            }
        });
    }

    @Override
    protected void setDetector() {
        // attach text detector to camera source for live preview
        mDetector = new TextRecognitionProcessor(this);
        cameraSource.setMachineLearningFrameProcessor(mDetector);
    }

    @Override
    protected void createIntent() {
        List<Product> selectedProducts = productBottomSheet.getSelectedProducts();
        Log.d(TAG, "Product list");
        for (int i = 0; i < selectedProducts.size(); i++) {
            Log.d(TAG, "Product: " + selectedProducts.get(i).getProductName());
        }
        Intent intent = ConfirmationActivity.makeIntentFromProducts(this, selectedProducts);
        startActivity(intent);
    }

    @Override
    public void onProductDiscard(Product product) {
        UIUtils.showToast(this, "Product discarded");
        productBottomSheet.onProductDiscard(product);
    }

    @Override
    public void onProductAdd(Product product) {
        UIUtils.showToast(this, "Product added");
        productBottomSheet.onProductAdd(product);
    }

    @Override
    public void onProductFound(List<Product> products) {
        productBottomSheet.addProducts(products);
    }

}

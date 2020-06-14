package com.googleinterns.smb;

import android.annotation.SuppressLint;
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
    private TextView numSuggestedProducts;
    private View numProductsBadge;

    @Override
    protected void initViews() {
        setTitle("Scan Text");
        setContentView(R.layout.activity_scan_text);
        TextView helpText = findViewById(R.id.help_text);
        helpText.setText(R.string.point_at_product_text_to_scan);

        // Current number of suggested products in bottom sheet
        numSuggestedProducts = findViewById(R.id.bottom_sheet_product_count);
        numProductsBadge = findViewById(R.id.num_products_badge);
        initRecyclerView();
    }

    /**
     * Initialise recycler view in bottom sheet
     */
    private void initRecyclerView() {
        List<Product> products = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.product_recycler_view);
        // Recycler view adapter
        ProductBottomSheetAdapter productBottomSheetAdapter = new ProductBottomSheetAdapter(products, this);

        // Bottom sheet handler object
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
                updateBadgeCount(productBottomSheet.getNumberofProducts());
            }
        });
    }

    @Override
    protected void setDetector() {
        // Attach text detector to camera source for live preview
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

    /**
     * Callback when user discards a product in bottom sheet
     */
    @Override
    public void onProductDiscard(Product product) {
        UIUtils.showToast(this, "Product discarded");
        productBottomSheet.onProductDiscard(product);
        updateBadgeCount(productBottomSheet.getNumberofProducts());
    }

    /**
     * Callback when user adds a product
     */
    @Override
    public void onProductAdd(Product product) {
        UIUtils.showToast(this, "Product added");
        productBottomSheet.onProductAdd(product);
        updateBadgeCount(productBottomSheet.getNumberofProducts());
    }

    /**
     * Callback from TextRecognitionProcessor when new products are identified
     *
     * @param products new detected products
     */
    @Override
    public void onProductFound(List<Product> products) {
        productBottomSheet.addProducts(products);
        updateBadgeCount(productBottomSheet.getNumberofProducts());
    }

    @SuppressLint("DefaultLocale")
    private void updateBadgeCount(int newCount) {
        if (newCount == 0) {
            numProductsBadge.setVisibility(View.INVISIBLE);
        } else {
            numProductsBadge.setVisibility(View.VISIBLE);
            numSuggestedProducts.setText(String.format("%d", newCount));
        }
    }

}

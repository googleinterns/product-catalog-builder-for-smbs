package com.googleinterns.smb.scan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.ConfirmationActivity;
import com.googleinterns.smb.R;
import com.googleinterns.smb.adapter.BottomSheetItemAdapter;
import com.googleinterns.smb.common.ProductBottomSheet;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Product;
import com.googleinterns.smb.textrecognition.TextRecognitionProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Specialisation of {@link ScanActivity} to implement Text scan functionality
 */
public class ScanTextActivity extends ScanActivity implements
        BottomSheetItemAdapter.ProductStatusListener,
        TextRecognitionProcessor.OnProductFoundListener {

    private ProductBottomSheet productBottomSheet;
    private TextView numSuggestedProducts;
    private View numProductsBadge;
    private BottomSheetItemAdapter bottomSheetItemAdapter;

    @Override
    protected void initViews() {
        setTitle("Scan Text");
        setContentView(R.layout.activity_scan_text);
        TextView helpText = findViewById(R.id.text_view_help);
        helpText.setText(R.string.point_at_product_text_to_scan);

        // Current number of suggested products in bottom sheet
        numSuggestedProducts = findViewById(R.id.text_view_product_count);
        numProductsBadge = findViewById(R.id.card_view_num_products_badge);
        initRecyclerView();
    }

    /**
     * Initialise recycler view in bottom sheet
     */
    private void initRecyclerView() {
        List<Product> products = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        // Recycler view adapter
        bottomSheetItemAdapter = new BottomSheetItemAdapter(products, this);

        // Bottom sheet handler object
        productBottomSheet = new ProductBottomSheet(bottomSheetItemAdapter);
        recyclerView.setAdapter(bottomSheetItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });

        Button clearButton = findViewById(R.id.button_clear_all);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productBottomSheet.clear();
                updateBadgeCount(bottomSheetItemAdapter.getItemCount());
            }
        });
    }

    @Override
    protected void setDetector() {
        // Attach text detector to camera source for live preview
        TextRecognitionProcessor mDetector = new TextRecognitionProcessor(this);
        mCameraSource.setMachineLearningFrameProcessor(mDetector);
    }

    @Override
    protected boolean isProductDetected() {
        return !productBottomSheet.getSelectedProducts().isEmpty();
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
        updateBadgeCount(bottomSheetItemAdapter.getItemCount());
    }

    /**
     * Callback when user adds a product
     */
    @Override
    public void onProductAdd(Product product) {
        UIUtils.showToast(this, "Product added");
        productBottomSheet.onProductAdd(product);
        updateBadgeCount(bottomSheetItemAdapter.getItemCount());
    }

    /**
     * Callback from TextRecognitionProcessor when new products are identified
     *
     * @param products new detected products
     */
    @Override
    public void onProductFound(List<Product> products) {
        productBottomSheet.addProducts(products);
        updateBadgeCount(bottomSheetItemAdapter.getItemCount());
    }

    @SuppressLint("DefaultLocale")
    private void updateBadgeCount(int newCount) {
        if (newCount == 0) {
            numProductsBadge.setVisibility(View.INVISIBLE);
        } else {
            numProductsBadge.setVisibility(View.VISIBLE);
            numSuggestedProducts.setText(String.format(Locale.getDefault(), "%d", newCount));
        }
    }

}

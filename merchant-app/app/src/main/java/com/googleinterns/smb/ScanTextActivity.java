package com.googleinterns.smb;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.adapter.ProductAdapter;
import com.googleinterns.smb.common.ProductBottomSheet;
import com.googleinterns.smb.model.Product;
import com.googleinterns.smb.textrecognition.TextRecognitionProcessor;

import java.util.ArrayList;
import java.util.List;

public class ScanTextActivity extends ScanActivity implements ProductAdapter.ProductStatusListener, TextRecognitionProcessor.OnProductFoundListener {

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
        ProductAdapter productAdapter = new ProductAdapter(products, this);
        productBottomSheet = new ProductBottomSheet(productAdapter);
        recyclerView.setAdapter(productAdapter);
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
        // TODO create intent to confirmation activity
    }

    @Override
    public void onProductDiscard(Product product) {
        showToast("Product discarded");
        productBottomSheet.onProductDiscard(product);
    }

    @Override
    public void onProductAdd(Product product) {
        showToast("Product added");
        productBottomSheet.onProductAdd(product);
    }

    @Override
    public void onProductFound(List<Product> products) {
        productBottomSheet.addProducts(products);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}

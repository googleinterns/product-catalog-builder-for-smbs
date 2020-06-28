package com.googleinterns.smb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.googleinterns.smb.adapter.ProductAdapter;
import com.googleinterns.smb.barcodescanning.BarcodeScanningProcessor;
import com.googleinterns.smb.barcodescanning.BarcodeStatusListener;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.common.VideoToBarcodeTask;
import com.googleinterns.smb.fragment.AddProductDialogFragment;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.model.Product;
import com.googleinterns.smb.scan.ScanBarcodeActivity;
import com.googleinterns.smb.scan.ScanTextActivity;

import java.io.IOException;
import java.util.List;

public class InventoryActivity extends MainActivity implements
        Merchant.OnProductFetchedListener,
        AddProductDialogFragment.OptionSelectListener,
        BarcodeStatusListener,
        Merchant.OnDataUpdatedListener,
        ProductAdapter.ProductActionListener {

    private static final String TAG = InventoryActivity.class.getName();
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;

    // Video to barcode converter task
    private AsyncTask<?, ?, ?> mTask;
    // Dialog to display navigation options
    private DialogFragment mDialogFragment;
    private View mContentView;
    private ProductAdapter mProductAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Inventory");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = inflater.inflate(R.layout.activity_inventory, null, false);
        mContainer.addView(mContentView, 0);
        FloatingActionButton fabAddProduct = mContentView.findViewById(R.id.fab_add_product);
        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InventoryActivity.this.addProduct(v);
            }
        });
        Merchant.getInstance().fetchProducts(this);
    }

    /**
     * Called by the add FAB, initiate adding a product. Show dialog for option selection.
     * 1. Scan barcode - onScanSelect()
     * 2. Image upload from gallery - onImageUploadSelect()
     * 3. Video upload - onVideoUploadSelect()
     * 4. Scan text - onScanTextSelect()
     *
     * @param view FAB
     */
    public void addProduct(View view) {
        mDialogFragment = new AddProductDialogFragment();
        mDialogFragment.show(getSupportFragmentManager(), "Add dialog");
    }

    @Override
    public void onScanSelect() {
        Intent intent = new Intent(this, ScanBarcodeActivity.class);
        startActivity(intent);
        mDialogFragment.dismiss();
    }

    @Override
    public void onScanTextSelect() {
        Intent intent = new Intent(this, ScanTextActivity.class);
        startActivity(intent);
        mDialogFragment.dismiss();
    }

    @Override
    public void onImageUploadSelect() {
        // Start choose image from gallery intent
        Intent intent = new Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE);
        mDialogFragment.dismiss();
    }

    @Override
    public void onVideoUploadSelect() {
        // Start choose video intent
        Intent intent = new Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_video)), PICK_VIDEO);
        mDialogFragment.dismiss();
    }

    /**
     * Fetch data and initialise recycler view
     */
    private void initRecyclerView(List<Product> products) {
        // Hide progress bar
        View view = mContentView.findViewById(R.id.progress_bar);
        view.setVisibility(View.GONE);
        if (products.isEmpty()) {
            displayMessageOnEmpty();
            return;
        }
        mProductAdapter = new ProductAdapter(products, this, getSupportFragmentManager());
        // Initialize recycler view
        RecyclerView recyclerView = mContentView.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mProductAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });
    }

    private void displayMessageOnEmpty() {
        View emptyInventory = mContentView.findViewById(R.id.empty_inventory);
        emptyInventory.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            Bitmap imageBitmap;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                BarcodeScanningProcessor imageProcessor = new BarcodeScanningProcessor();
                imageProcessor.getFromBitmap(imageBitmap, this);
            } catch (IOException e) {
                Log.d(TAG, "Error retrieving image: ", e);
                UIUtils.showToast(this, getString(R.string.error_retrieve_image));
            }
        } else if (requestCode == PICK_VIDEO && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            try {
                ProgressDialog mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMessage(getString(R.string.processing));
                mTask = new VideoToBarcodeTask(this, this, videoUri, mProgressDialog);
                mTask.execute();
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
            } catch (Exception e) {
                UIUtils.showToast(this, getString(R.string.error_processing_video));
                Log.d(TAG, "Error while loading file: ", e);
            }
        }
    }

    /**
     * This status callback can be received directly from {@link BarcodeScanningProcessor} or
     * from {@link VideoToBarcodeTask} task on completion.
     *
     * @param barcodes array of EAN strings
     */
    @Override
    public void onSuccess(List<String> barcodes) {
        if (barcodes.size() > 0) {
            // Pass the detected barcodes to confirmation activity (to remove if any unwanted detected)
            startActivity(ConfirmationActivity.makeIntentFromBarcodes(this, barcodes));
        } else {
            UIUtils.showToast(this, getString(R.string.no_barcode_found));
        }
    }

    @Override
    public void onFailure(Exception e) {
        Log.d(TAG, "Error: ", e);
        UIUtils.showToast(this, getString(R.string.error_processing_image));
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, InventoryActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    @Override
    public void onDataUpdateSuccess() {
    }

    @Override
    public void onDataUpdateFailure() {
        // Delete failed
        UIUtils.showNoConnectionMessage(this, mContainer);
    }

    @Override
    public void onProductDeleted(Product product) {
        Merchant.getInstance().deleteProduct(this, product);
        if (mProductAdapter.getItemCount() == 0) {
            displayMessageOnEmpty();
        }
    }

    @Override
    public void onPriceChanged(Product updatedProduct) {
        Merchant.getInstance().updateProduct(this, updatedProduct);
    }

    @Override
    public void onProductFetched(List<Product> products) {
        initRecyclerView(products);
    }
}

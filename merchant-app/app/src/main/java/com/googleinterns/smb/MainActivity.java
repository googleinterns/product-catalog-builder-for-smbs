package com.googleinterns.smb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.googleinterns.smb.barcodescanning.BarcodeScanningProcessor;
import com.googleinterns.smb.fragment.AddProductDialogFragment;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AddProductDialogFragment.OptionSelectListener, BarcodeScanningProcessor.StatusListener {

    private static final String TAG = MainActivity.class.getName();
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called by the add FAB, initiate adding a product. Show dialog for option selection.
     * 1. Scan barcode
     * 2. Image upload from gallery
     *
     * @param view FAB
     */
    public void addProduct(View view) {
        DialogFragment dialogFragment = new AddProductDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void onUploadSelect() {
        Intent intent = new Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
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
                showToast("Error retrieving image. Please try again");
            }
        }
    }

    @Override
    public void onScanSelect() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess(List<String> barcodes) {
        if (barcodes.size() > 0) {
            // pass the detected barcodes to confirmation activity (to remove if any unwanted detected)
            startActivity(ConfirmationActivity.makeIntent(this, barcodes));
        } else {
            showToast("No barcodes found!");
        }
    }

    @Override
    public void onFailure(Exception e) {
        Log.d(TAG, "Error: ", e);
        showToast("Detected error when processing image");
    }
}

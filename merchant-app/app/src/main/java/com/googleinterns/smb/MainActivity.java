package com.googleinterns.smb;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.googleinterns.smb.barcodescanning.BarcodeScanningProcessor;
import com.googleinterns.smb.barcodescanning.BarcodeStatusListener;
import com.googleinterns.smb.common.VideoToBarcode;
import com.googleinterns.smb.fragment.AddProductDialogFragment;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AddProductDialogFragment.OptionSelectListener, BarcodeStatusListener {

    private static final String TAG = MainActivity.class.getName();
    private static final int PICK_IMAGE = 1;

    private static final int PICK_VIDEO = 2;
    private AsyncTask<?, ?, ?> task;
    private DialogFragment mDialogFragment;

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
        mDialogFragment = new AddProductDialogFragment();
        mDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void onImageUploadSelect() {
        Intent intent = new Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE);
    }

    @Override
    public void onScanSelect() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    @Override
    public void onVideoUploadSelect() {
        Intent intent = new Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_video)), PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mDialogFragment != null) {
            mDialogFragment.dismiss();
        }
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            Bitmap imageBitmap;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                BarcodeScanningProcessor imageProcessor = new BarcodeScanningProcessor();
                imageProcessor.getFromBitmap(imageBitmap, this);
            } catch (IOException e) {
                Log.d(TAG, "Error retrieving image: ", e);
                showToast(getString(R.string.error_retrieve_image));
            }
        } else if (requestCode == PICK_VIDEO && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            try {
                ProgressDialog mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMessage(getString(R.string.processing));
                task = new VideoToBarcode(this, this, videoUri, mProgressDialog);
                task.execute();
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
            } catch (Exception e) {
                showToast(getString(R.string.error_processing_video));
                Log.d(TAG, "Error while loading file: ", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (task != null) {
            task.cancel(true);
        }
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
            showToast(getString(R.string.no_barcode_found));
        }
    }

    @Override
    public void onFailure(Exception e) {
        Log.d(TAG, "Error: ", e);
        showToast(getString(R.string.error_processing_image));
    }
}

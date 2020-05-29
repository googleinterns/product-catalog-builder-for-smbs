package com.googleinterns.smb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.googleinterns.smb.barcodescanning.BarcodeScanningProcessor;
import com.googleinterns.smb.barcodescanning.BarcodeStatusListener;
import com.googleinterns.smb.common.VideoToBarcode;
import com.googleinterns.smb.fragment.AddProductDialogFragment;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AddProductDialogFragment.OptionSelectListener, BarcodeStatusListener {

    private static final String TAG = MainActivity.class.getName();
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final int START_SIGN_IN = 3;

    // video to barcode converter task
    private AsyncTask<?, ?, ?> task;
    private DialogFragment mDialogFragment;
    private boolean isSigningIn = false;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this);
            startSignIn();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start sign in if necessary
        if (isSignInRequired()) {
            startSignIn();
        }
    }

    private boolean isSignInRequired() {
        return (!isSigningIn && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    /**
     * Initiates google auth sign in with firebase UI
     */
    private void startSignIn() {
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build();
        isSigningIn = true;
        startActivityForResult(intent, START_SIGN_IN);
    }

    /**
     * Called by the add FAB, initiate adding a product. Show dialog for option selection.
     * 1. Scan barcode - onScanSelect()
     * 2. Image upload from gallery - onImageUploadSelect()
     * 3. Video upload - onVideoUploadSelect()
     *
     * @param view FAB
     */
    public void addProduct(View view) {
        mDialogFragment = new AddProductDialogFragment();
        mDialogFragment.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void onScanSelect() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
        mDialogFragment.dismiss();
    }

    @Override
    public void onImageUploadSelect() {
        // start choose image from gallery intent
        Intent intent = new Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE);
        mDialogFragment.dismiss();
    }

    @Override
    public void onVideoUploadSelect() {
        // start choose video intent
        Intent intent = new Intent()
                .setType("video/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_video)), PICK_VIDEO);
        mDialogFragment.dismiss();
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
        } else if (requestCode == START_SIGN_IN) {
            isSigningIn = false;
            if (resultCode != RESULT_OK && isSignInRequired()) {
                startSignIn();
            } else if (resultCode == RESULT_OK) {
                showToast("Sign In successful!");
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

    /**
     * This status callback can be received directly from BarcodeScanningProcessor or from VideoToBarcode task on completion.
     *
     * @param barcodes array of EAN strings
     */
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

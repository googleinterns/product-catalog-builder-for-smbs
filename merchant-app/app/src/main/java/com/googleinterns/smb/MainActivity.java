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
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.googleinterns.smb.barcodescanning.BarcodeScanningProcessor;
import com.googleinterns.smb.barcodescanning.BarcodeStatusListener;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.common.VideoToBarcode;
import com.googleinterns.smb.fragment.AddProductDialogFragment;
import com.googleinterns.smb.model.Merchant;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        AddProductDialogFragment.OptionSelectListener,
        BarcodeStatusListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getName();
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final int START_SIGN_IN = 3;

    private static Context applicationContext;

    // Video to barcode converter task
    private AsyncTask<?, ?, ?> task;
    // Dialog to display navigation options
    private DialogFragment mDialogFragment;
    private boolean isSigningIn = false;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    protected FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        container = findViewById(R.id.container);
        initNavigationDrawer();

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        if (isSignInRequired()) {
            startSignIn();
        } else {
            initMerchant();
        }
    }

    private void initNavigationDrawer() {
        // Set custom toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Setup navigation drawer options
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_inventory:
                onInventorySelect();
                break;
            case R.id.menu_new_orders:
                onNewOrderSelect();
                break;
            case R.id.menu_ongoing_orders:
                onOngoingOrderSelect();
                break;
            case R.id.menu_create_bill:
                onBillSelect();
                break;
            case R.id.menu_debug_tools:
                Intent intent = new Intent(this, DebugActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_sign_out:
                Merchant.removeInstance();
                AuthUI.getInstance().signOut(this);
                startSignIn();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isSignInRequired() {
        return (!isSigningIn && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    /**
     * Initiates google auth sign in with firebase UI
     */
    private void startSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
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

    public void onBillSelect() {
        Intent intent = new Intent(this, ScanBarcodeActivity.class);
        // Start barcode scanner for creating bill
        intent.putExtra(ScanBarcodeActivity.CREATE_BILL, true);
        startActivity(intent);
    }

    public void onNewOrderSelect() {
        Intent intent = new Intent(this, NewOrdersActivity.class);
        startActivity(intent);
        finish();
    }

    public void onOngoingOrderSelect() {
        Intent intent = new Intent(this, OngoingOrdersActivity.class);
        startActivity(intent);
        finish();
    }

    public void onInventorySelect() {
        Intent intent = new Intent(this, InventoryActivity.class);
        startActivity(intent);
        finish();
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
                task = new VideoToBarcode(this, this, videoUri, mProgressDialog);
                task.execute();
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
            } catch (Exception e) {
                UIUtils.showToast(this, getString(R.string.error_processing_video));
                Log.d(TAG, "Error while loading file: ", e);
            }
        } else if (requestCode == START_SIGN_IN) {
            isSigningIn = false;
            if (resultCode != RESULT_OK && isSignInRequired()) {
                startSignIn();
            } else if (resultCode == RESULT_OK) {
                UIUtils.showToast(this, "Sign In successful!");
                initMerchant();
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

    /**
     * This status callback can be received directly from BarcodeScanningProcessor or from VideoToBarcode task on completion.
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
        return new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    private void initMerchant() {
        // Check internet connection
        if (CommonUtils.isConnectedToInternet(this)) {
            // Initialize merchant
            Merchant merchant = Merchant.getInstance();
            View headerLayout = navigationView.getHeaderView(0);
            TextView username = headerLayout.findViewById(R.id.username);
            TextView email = headerLayout.findViewById(R.id.email);
            ImageView profileImage = headerLayout.findViewById(R.id.profile_image);
            username.setText(merchant.getName());
            email.setText(merchant.getEmail());
            Glide.with(profileImage.getContext())
                    .load(merchant.getPhotoUri())
                    .circleCrop()
                    .into(profileImage);
        } else {
            UIUtils.showNoConnectionMessage(this, findViewById(R.id.container));
        }
    }

    /**
     * Close drawer on back press if open
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public static Context getContext() {
        return applicationContext;
    }
}

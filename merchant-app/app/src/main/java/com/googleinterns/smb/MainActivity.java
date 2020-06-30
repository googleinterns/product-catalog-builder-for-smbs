package com.googleinterns.smb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Merchant;
import com.googleinterns.smb.scan.ScanBarcodeActivity;

/**
 * Parent activity for all navigation drawer children activities.
 */
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getName();
    private static final int START_SIGN_IN = 3;
    private static Context mApplicationContext;

    private boolean mIsSigningIn = false;
    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    protected FrameLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplicationContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        mContainer = findViewById(R.id.container);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        if (isSignInRequired()) {
            startSignIn();
        } else {
            initNavigationDrawer();
            initMerchant();
        }
    }

    private void initNavigationDrawer() {
        // Set custom toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_drawer);
        mNavigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
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
            case R.id.menu_offers:
                onOfferSelect();
                break;
            case R.id.menu_settings:
                onSettingSelect();
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
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isSignInRequired() {
        return (!mIsSigningIn && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    /**
     * Initiates google auth sign in with firebase UI
     */
    private void startSignIn() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void onBillSelect() {
        Intent intent = new Intent(this, ScanBarcodeActivity.class);
        // Start barcode scanner for creating bill
        intent.putExtra(ScanBarcodeActivity.CREATE_BILL, true);
        startActivity(intent);
    }

    private void onNewOrderSelect() {
        Intent intent = new Intent(this, NewOrdersActivity.class);
        startActivity(intent);
        finish();
    }

    private void onOngoingOrderSelect() {
        Intent intent = new Intent(this, OngoingOrdersActivity.class);
        startActivity(intent);
        finish();
    }

    private void onInventorySelect() {
        Intent intent = new Intent(this, InventoryActivity.class);
        startActivity(intent);
        finish();
    }

    private void onOfferSelect() {
        Intent intent = new Intent(this, ProductOfferActivity.class);
        startActivity(intent);
        finish();
    }

    private void onSettingSelect() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == START_SIGN_IN) {
            mIsSigningIn = false;
            if (resultCode != RESULT_OK && isSignInRequired()) {
                startSignIn();
            } else if (resultCode == RESULT_OK) {
                UIUtils.showToast(this, "Sign In successful!");
                initMerchant();
            }
        }
    }

    private void initMerchant() {
        // Check internet connection
        if (CommonUtils.isConnectedToInternet(this)) {
            // Initialize merchant
            Merchant merchant = Merchant.getInstance();
            merchant.fetchProducts();
            View headerLayout = mNavigationView.getHeaderView(0);
            TextView username = headerLayout.findViewById(R.id.text_view_username);
            TextView email = headerLayout.findViewById(R.id.email);
            ImageView profileImage = headerLayout.findViewById(R.id.image_view_profile_image);
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
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public static Context getContext() {
        return mApplicationContext;
    }
}

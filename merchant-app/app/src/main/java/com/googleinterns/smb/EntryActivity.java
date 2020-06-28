package com.googleinterns.smb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.googleinterns.smb.model.Merchant;

/**
 * Startup activity to display brand logo during app cold-start.
 */
public class EntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EntryActivity.this);
        int numOfProducts = preferences.getInt(Merchant.NUM_PRODUCTS, 0);
        if (numOfProducts > 0) {
            startActivity(new Intent(EntryActivity.this, OngoingOrdersActivity.class));
        } else {
            startActivity(new Intent(EntryActivity.this, InventoryActivity.class));
        }
        finish();
    }
}

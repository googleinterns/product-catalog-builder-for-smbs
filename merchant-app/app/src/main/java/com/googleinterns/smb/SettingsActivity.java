package com.googleinterns.smb;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Merchant;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getName();
    private static final int PICK_LOCATION = 1;
    private static final int EDIT_LOCATION = 2;

    private LatLng location;
    private String domainName;

    private TextInputEditText domainNameEditText;
    private TextInputLayout domainNameEditLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Settings");
        setContentView(R.layout.activity_settings);
        Button setLocation = findViewById(R.id.set_location);
        setLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, LocationPickerActivity.class);
                startActivityForResult(intent, PICK_LOCATION);
            }
        });
        Merchant merchant = Merchant.getInstance();
        final TextInputEditText storeName = findViewById(R.id.store_name_edit_text);
        final TextInputLayout storeNameLayout = findViewById(R.id.store_name_edit_layout);
        if (merchant.getStoreName() != null) {
            storeName.setText(merchant.getStoreName());
        }

        final TextInputEditText storeAddress = findViewById(R.id.store_address_edit_text);
        final TextInputLayout storeAddressLayout = findViewById(R.id.store_address_edit_layout);
        if (merchant.getAddress() != null) {
            storeAddress.setText(merchant.getAddress());
        }

        if (merchant.getLatLng() != null) {
            location = merchant.getLatLng();
        }

        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean allFieldsOk = true;
                if (storeName.getText().toString().trim().equals("")) {
                    storeNameLayout.setErrorEnabled(true);
                    storeNameLayout.setError("No store name specified");
                    allFieldsOk = false;
                }
                if (storeAddress.getText().toString().trim().equals("")) {
                    storeAddressLayout.setErrorEnabled(true);
                    storeAddressLayout.setError("No store name specified");
                    allFieldsOk = false;
                }
                if (location == null) {
                    UIUtils.showToast(SettingsActivity.this, "No location set");
                    allFieldsOk = false;
                }
                if (allFieldsOk) {
                    Merchant merchant = Merchant.getInstance();
                    merchant.setStoreName(storeName.getText().toString());
                    merchant.setAddress(storeAddress.getText().toString());
                    merchant.setLatLng(location);
                    if (domainName != null) {
                        merchant.setDomainName(domainName);
                    }
                    UIUtils.showToast(SettingsActivity.this, "Saved");
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
        Button cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView domainNameHelp = findViewById(R.id.domain_name_help);
        TooltipCompat.setTooltipText(domainNameHelp, getString(R.string.domain_name_help_info));
        domainNameEditLayout = findViewById(R.id.domain_name_edit_layout);
        domainNameEditText = findViewById(R.id.domain_name_edit_text);
        if (merchant.getDomainName() != null) {
            domainNameEditText.setText(merchant.getDomainName());
            domainName = merchant.getDomainName();
        }
        final ProgressBar searchProgressBar = findViewById(R.id.search_progress);
        domainNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                domainNameEditLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                if ("".equals(s.toString().trim())) {
                    return;
                }
                searchProgressBar.setVisibility(View.VISIBLE);
                FirebaseUtils.isDomainAvailable(s.toString(), new FirebaseUtils.DomainAvaliabilityCheckListener() {
                    @Override
                    public void onCheckComplete(boolean isAvailable) {
                        if (domainNameEditText.getText().toString().equals(s.toString())) {
                            searchProgressBar.setVisibility(View.INVISIBLE);
                            domainNameEditLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                            if (isAvailable) {
                                domainNameEditLayout.setEndIconDrawable(getDrawable(R.drawable.ic_action_ok_circle));
                                domainName = s.toString();
                            } else {
                                domainNameEditLayout.setEndIconDrawable(getDrawable(R.drawable.ic_error_circle));
                                domainName = null;
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_LOCATION) {
            if (resultCode == RESULT_OK) {
                double latitutde = data.getDoubleExtra("latitude", 0.0);
                double longitude = data.getDoubleExtra("longitude", 0.0);
                location = new LatLng(latitutde, longitude);
            } else {
                UIUtils.showToast(this, "No location picked");
            }
        }
    }
}

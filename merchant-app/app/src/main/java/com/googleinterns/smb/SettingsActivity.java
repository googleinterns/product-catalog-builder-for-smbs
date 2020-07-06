package com.googleinterns.smb;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Merchant;
import com.tooltip.Tooltip;

/**
 * Activity to set merchant details
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getName();
    private static final int PICK_LOCATION = 1;
    private static final int EDIT_LOCATION = 2;

    private LatLng mLocation;
    private String mDomainName;

    private TextInputEditText mEditTextDomainName;
    private TextInputLayout mTextLayoutDomainName;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Settings");
        setContentView(R.layout.activity_settings);
        Button setLocation = findViewById(R.id.button_set_location);
        setLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, LocationPickerActivity.class);
                startActivityForResult(intent, PICK_LOCATION);
            }
        });
        Merchant merchant = Merchant.getInstance();
        final TextInputEditText storeName = findViewById(R.id.edit_text_store_name);
        final TextInputLayout storeNameLayout = findViewById(R.id.layout_edit_text_store_name);
        if (merchant.getStoreName() != null && savedInstanceState == null) {
            storeName.setText(merchant.getStoreName());
        }

        final TextInputEditText storeAddress = findViewById(R.id.edit_text_store_address);
        final TextInputLayout storeAddressLayout = findViewById(R.id.layout_edit_text_store_address);
        if (merchant.getAddress() != null && savedInstanceState == null) {
            storeAddress.setText(merchant.getAddress());
        }

        if (merchant.getLatLng() != null) {
            mLocation = merchant.getLatLng();
        }

        Button save = findViewById(R.id.button_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean allFieldsOk = true;
                if (storeName.getText().toString().trim().equals("")) {
                    storeNameLayout.setErrorEnabled(true);
                    storeNameLayout.setError(SettingsActivity.this.getString(R.string.no_store_name));
                    allFieldsOk = false;
                }
                if (storeAddress.getText().toString().trim().equals("")) {
                    storeAddressLayout.setErrorEnabled(true);
                    storeAddressLayout.setError(SettingsActivity.this.getString(R.string.no_store_address));
                    allFieldsOk = false;
                }
                if (mLocation == null) {
                    UIUtils.showToast(SettingsActivity.this, SettingsActivity.this.getString(R.string.no_location_set));
                    allFieldsOk = false;
                }
                if (allFieldsOk) {
                    Merchant merchant = Merchant.getInstance();
                    merchant.setStoreName(storeName.getText().toString());
                    merchant.setAddress(storeAddress.getText().toString());
                    merchant.setLatLng(mLocation);
                    if (mDomainName != null) {
                        merchant.setDomainName(mDomainName);
                    }
                    UIUtils.showToast(SettingsActivity.this, SettingsActivity.this.getString(R.string.saved));
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
        Button cancel = findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageView domainNameHelp = findViewById(R.id.image_view_help);
        domainNameHelp.setTooltipText(getString(R.string.domain_name_help_info));
        final Tooltip toolTip = new Tooltip.Builder(domainNameHelp, R.style.TooltipStyle)
                .setText(R.string.domain_name_help_info)
                .setCancelable(true)
                .build();
        domainNameHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTip.show();
            }
        });

        mTextLayoutDomainName = findViewById(R.id.layout_edit_text_domain_name);
        mEditTextDomainName = findViewById(R.id.edit_text_domain_name);
        if (merchant.getDomainName() != null && savedInstanceState == null) {
            mEditTextDomainName.setText(merchant.getDomainName());
            mDomainName = merchant.getDomainName();
        }
        final ProgressBar searchProgressBar = findViewById(R.id.progress_bar_search);
        mEditTextDomainName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                mTextLayoutDomainName.setEndIconMode(TextInputLayout.END_ICON_NONE);
                if ("".equals(s.toString().trim())) {
                    return;
                }
                searchProgressBar.setVisibility(View.VISIBLE);
                FirebaseUtils.isDomainAvailable(s.toString(), new FirebaseUtils.DomainAvaliabilityCheckListener() {
                    @Override
                    public void onCheckComplete(boolean isAvailable) {
                        if (mEditTextDomainName.getText().toString().equals(s.toString())) {
                            searchProgressBar.setVisibility(View.INVISIBLE);
                            mTextLayoutDomainName.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                            if (isAvailable) {
                                mTextLayoutDomainName.setEndIconDrawable(getDrawable(R.drawable.ic_action_ok_circle));
                                mDomainName = s.toString();
                            } else {
                                mTextLayoutDomainName.setEndIconDrawable(getDrawable(R.drawable.ic_error_circle));
                                mDomainName = null;
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
                mLocation = new LatLng(latitutde, longitude);
            } else {
                UIUtils.showToast(this, getString(R.string.no_location_set));
            }
        }
    }
}

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
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.FirebaseUtils;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.fragment.DomainNameChangeAlertDialog;
import com.googleinterns.smb.fragment.InvalidDomainNameAlertDialog;
import com.googleinterns.smb.model.Merchant;
import com.tooltip.Tooltip;

/**
 * Activity to set merchant details
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getName();
    private static final int PICK_LOCATION = 1;

    private LatLng mLocation;
    private String mDomainName;
    private String mSavedDomainName;

    private TextInputEditText mEditTextDomainName;
    private TextInputLayout mTextLayoutDomainName;
    private Button mSetLocation;

    private boolean isDomainNameOptionOverridden = false;
    private boolean isDomainNameChangeConfirmed = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.settings));
        setContentView(R.layout.activity_settings);
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

        mSetLocation = findViewById(R.id.button_set_location);
        if (mLocation != null) {
            mSetLocation.setText(R.string.change_location);
        }
        mSetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, LocationPickerActivity.class);
                if (mLocation != null) {
                    intent.putExtra(CommonUtils.LATITUDE, mLocation.latitude);
                    intent.putExtra(CommonUtils.LONGITUDE, mLocation.longitude);
                }
                startActivityForResult(intent, PICK_LOCATION);
            }
        });
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
                if (mDomainName == null && !isDomainNameOptionOverridden) {
                    new InvalidDomainNameAlertDialog(new InvalidDomainNameAlertDialog.OnConfirmListener() {
                        @Override
                        public void onConfirm() {
                            isDomainNameOptionOverridden = true;
                            save.callOnClick();
                        }
                    }).show(getSupportFragmentManager(), InvalidDomainNameAlertDialog.class.getName());
                    allFieldsOk = false;
                }
                if (mDomainName != null &&
                        mSavedDomainName != null &&
                        !isDomainNameChangeConfirmed &&
                        !mDomainName.equals(mSavedDomainName)) {
                    new DomainNameChangeAlertDialog(new DomainNameChangeAlertDialog.OnConfirmListener() {
                        @Override
                        public void onConfirm() {
                            isDomainNameChangeConfirmed = true;
                            save.callOnClick();
                        }
                    }).show(getSupportFragmentManager(), DomainNameChangeAlertDialog.class.getName());
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
            mSavedDomainName = merchant.getDomainName();
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
                String queryDomainName = s.toString().trim();
                if ("".equals(queryDomainName)) {
                    searchProgressBar.setVisibility(View.INVISIBLE);
                    mDomainName = null;
                    return;
                }
                searchProgressBar.setVisibility(View.VISIBLE);
                FirebaseUtils.isDomainAvailable(s.toString(), new FirebaseUtils.DomainAvaliabilityCheckListener() {
                    @Override
                    public void onCheckComplete(boolean isAvailable) {
                        if (mEditTextDomainName.getText().toString().trim().equals(queryDomainName)) {
                            searchProgressBar.setVisibility(View.INVISIBLE);
                            mTextLayoutDomainName.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                            if (isAvailable || queryDomainName.equals(mSavedDomainName)) {
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
        if (resultCode == RESULT_OK) {
            double latitutde = data.getDoubleExtra(CommonUtils.LATITUDE, 0.0);
            double longitude = data.getDoubleExtra(CommonUtils.LONGITUDE, 0.0);
            mLocation = new LatLng(latitutde, longitude);
            mSetLocation.setText(R.string.change_location);
        } else {
            UIUtils.showToast(this, getString(R.string.no_location_set));
        }
    }
}

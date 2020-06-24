package com.googleinterns.smb;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.googleinterns.smb.common.CommonUtils;
import com.googleinterns.smb.common.UIUtils;
import com.googleinterns.smb.model.Offer;

import java.util.Calendar;
import java.util.Objects;

public class OfferDetailsActivity extends AppCompatActivity {

    public static final int RC_ADD_OFFER = 1;
    public static final int RC_EDIT_OFFER = 2;

    private RadioGroup selectDiscountType;
    private TextInputEditText discountEditText;
    private TextInputLayout discountLayout;
    private MaterialCheckBox checkBoxMarkForever;
    private TextView validityTextView;
    private MaterialDatePicker<Long> datePicker;
    private Long validityEndDate;
    private Button changeValidity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_offer);
        setTitle("Offer");
        selectDiscountType = findViewById(R.id.set_discount_radio_group);
        selectDiscountType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.set_percent_discount) {
                    discountLayout.setStartIconVisible(false);
                    discountLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    discountLayout.setEndIconDrawable(R.drawable.ic_percent);
                } else {
                    discountLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    discountLayout.setStartIconDrawable(R.drawable.ic_rupee);
                    discountLayout.setStartIconVisible(true);
                }
            }
        });
        discountEditText = findViewById(R.id.discount_edit_text);
        discountLayout = findViewById(R.id.discount_edit_layout);
        checkBoxMarkForever = findViewById(R.id.checkbox_mark_forever);
        checkBoxMarkForever.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    changeValidity.setEnabled(false);
                    validityTextView.setText(UIUtils.NIL_DATE);
                } else {
                    changeValidity.setEnabled(true);
                }
            }
        });
        checkBoxMarkForever.setChecked(true);
        validityTextView = findViewById(R.id.end_date);
        Calendar now = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        today.clear();
        today.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setStart(today.getTimeInMillis())
                .setValidator(new CalendarConstraints.DateValidator() {
                    @Override
                    public boolean isValid(long date) {
                        return date >= today.getTimeInMillis();
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(Parcel dest, int flags) {

                    }
                })
                .build();
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Set validity")
                .setCalendarConstraints(constraints)
                .setSelection(now.getTimeInMillis())
                .build();
        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                validityEndDate = selection;
                validityTextView.setText(CommonUtils.getFormattedDate(validityEndDate));
            }
        });
        datePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                datePicker.dismiss();
            }
        });
        datePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.dismiss();
            }
        });
        changeValidity = findViewById(R.id.change_validity);
        changeValidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show(getSupportFragmentManager(), MaterialDatePicker.class.getName());
            }
        });
        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Offer.OfferType offerType;
                if (selectDiscountType.getCheckedRadioButtonId() == R.id.set_percent_discount)
                    offerType = Offer.OfferType.PERCENTAGE_OFFER;
                else
                    offerType = Offer.OfferType.FLAT_OFFER;
                int discount;
                try {
                    discount = Integer.parseInt(Objects.requireNonNull(discountEditText.getText()).toString());
                } catch (NumberFormatException e) {
                    discountLayout.setError("Invalid discount");
                    return;
                }
                if (offerType == Offer.OfferType.PERCENTAGE_OFFER) {
                    if (discount > 100) {
                        discountLayout.setError("Percentage cannot be greater than 100");
                        return;
                    }
                }
                Boolean isValidForever = checkBoxMarkForever.isChecked();
                if (validityEndDate == null && !isValidForever) {
                    UIUtils.showToast(OfferDetailsActivity.this, "Please set offer validity");
                    return;
                }
                Offer offer = new Offer(offerType, discount, validityEndDate, isValidForever);
                Intent intent = new Intent();
                intent.putExtra("offer", offer);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        Button cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        Intent intent = getIntent();
        int requestCode = intent.getIntExtra("requestCode", 1);
        if (requestCode == RC_EDIT_OFFER) {
            Offer offer = (Offer) intent.getSerializableExtra("offer");
            if (offer.getOfferType() == Offer.OfferType.PERCENTAGE_OFFER) {
                selectDiscountType.check(R.id.set_percent_discount);
            } else {
                selectDiscountType.check(R.id.set_flat_discount);
            }
            int discount = offer.getOfferAmount();
            discountEditText.setText(String.valueOf(discount));
            Boolean isValidForever = offer.getValidForever();
            checkBoxMarkForever.setChecked(isValidForever);
            if (!isValidForever) {
                validityEndDate = offer.getValidityEndDate();
                validityTextView.setText(CommonUtils.getFormattedDate(validityEndDate));
            }
        }
    }
}

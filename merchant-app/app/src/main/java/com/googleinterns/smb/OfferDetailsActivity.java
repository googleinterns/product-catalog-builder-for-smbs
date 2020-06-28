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

/**
 * Activity to add and edit offer details.
 */
public class OfferDetailsActivity extends AppCompatActivity {

    public static final int RC_ADD_OFFER = 1;
    public static final int RC_EDIT_OFFER = 2;

    private RadioGroup mDiscountType;
    private TextInputEditText mEditTextDiscount;
    private TextInputLayout mTextLayoutDiscount;
    private MaterialCheckBox mCheckBoxMarkForever;
    private TextView mValidity;
    private MaterialDatePicker<Long> mDatePicker;
    private Long mValidityEndDate;
    private Button mChangeValidity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_details);
        setTitle("Offer");
        mDiscountType = findViewById(R.id.radio_group_set_discount_type);
        mDiscountType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_button_set_percent_discount) {
                    mTextLayoutDiscount.setStartIconVisible(false);
                    mTextLayoutDiscount.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
                    mTextLayoutDiscount.setEndIconDrawable(R.drawable.ic_percent);
                } else {
                    mTextLayoutDiscount.setEndIconMode(TextInputLayout.END_ICON_NONE);
                    mTextLayoutDiscount.setStartIconDrawable(R.drawable.ic_rupee);
                    mTextLayoutDiscount.setStartIconVisible(true);
                }
            }
        });
        mEditTextDiscount = findViewById(R.id.edit_text_discount);
        mTextLayoutDiscount = findViewById(R.id.layout_edit_text_discount);
        mCheckBoxMarkForever = findViewById(R.id.checkbox_mark_forever);
        mCheckBoxMarkForever.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mChangeValidity.setEnabled(false);
                    mValidity.setText(UIUtils.NIL_DATE);
                } else {
                    mChangeValidity.setEnabled(true);
                }
            }
        });
        mCheckBoxMarkForever.setChecked(true);
        mValidity = findViewById(R.id.text_view_end_date);
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
        mDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Set validity")
                .setCalendarConstraints(constraints)
                .setSelection(now.getTimeInMillis())
                .build();
        mDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                mValidityEndDate = selection;
                mValidity.setText(CommonUtils.getFormattedDate(mValidityEndDate));
            }
        });
        mDatePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mDatePicker.dismiss();
            }
        });
        mDatePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePicker.dismiss();
            }
        });
        mChangeValidity = findViewById(R.id.button_change_validity);
        mChangeValidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePicker.show(getSupportFragmentManager(), MaterialDatePicker.class.getName());
            }
        });
        Button save = findViewById(R.id.button_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Offer.OfferType offerType;
                if (mDiscountType.getCheckedRadioButtonId() == R.id.radio_button_set_percent_discount)
                    offerType = Offer.OfferType.PERCENTAGE_OFFER;
                else
                    offerType = Offer.OfferType.FLAT_OFFER;
                int discount;
                try {
                    discount = Integer.parseInt(Objects.requireNonNull(mEditTextDiscount.getText()).toString());
                } catch (NumberFormatException e) {
                    mTextLayoutDiscount.setError("Invalid discount");
                    return;
                }
                if (offerType == Offer.OfferType.PERCENTAGE_OFFER) {
                    if (discount > 100) {
                        mTextLayoutDiscount.setError("Percentage cannot be greater than 100");
                        return;
                    }
                }
                Boolean isValidForever = mCheckBoxMarkForever.isChecked();
                if (mValidityEndDate == null && !isValidForever) {
                    UIUtils.showToast(OfferDetailsActivity.this, "Please set offer validity");
                    return;
                }
                Offer offer = new Offer(offerType, discount, mValidityEndDate, isValidForever);
                Intent intent = new Intent();
                intent.putExtra("offer", offer);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        Button cancel = findViewById(R.id.button_cancel);
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
                mDiscountType.check(R.id.radio_button_set_percent_discount);
            } else {
                mDiscountType.check(R.id.radio_button_set_flat_discount);
            }
            int discount = offer.getOfferAmount();
            mEditTextDiscount.setText(String.valueOf(discount));
            Boolean isValidForever = offer.getValidForever();
            mCheckBoxMarkForever.setChecked(isValidForever);
            if (!isValidForever) {
                mValidityEndDate = offer.getValidityEndDate();
                mValidity.setText(CommonUtils.getFormattedDate(mValidityEndDate));
            }
        }
    }
}

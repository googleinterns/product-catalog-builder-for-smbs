package com.googleinterns.smb.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.googleinterns.smb.R;
import com.googleinterns.smb.common.UIUtils;

import java.util.Objects;

/**
 * Dialog for editing merchant price in confirmation activity
 */
public class EditPriceDialogFragment extends DialogFragment {

    public interface PriceConfirmationListener {
        void onPriceConfirm(Double discountPrice);
    }

    private static final String TAG = EditPriceDialogFragment.class.getName();
    private PriceConfirmationListener mListener;
    private View mDialogView;
    private TextInputEditText mEditTextDiscountPrice;
    private TextInputLayout mEditTextLayout;
    private Double mMRP;

    public EditPriceDialogFragment(PriceConfirmationListener listener, Double MRP) {
        mListener = listener;
        mMRP = MRP;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(mDialogView)
                .setTitle(R.string.set_discount_price)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ignore
                        UIUtils.closeKeyboard(requireContext());
                    }
                })
                .setPositiveButton(R.string.done, null); // Listener overridden later
        final Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // Manually set on click listener to positive button for validation before dismissing the dialog
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Double discountedPrice = getDiscountPrice();
                        // Discount price greater than MRP is not allowed
                        if (mMRP > 0 && discountedPrice > mMRP) {
                            mEditTextLayout.setError("Error: price must be less than MRP");
                        } else {
                            UIUtils.closeKeyboard(requireContext());
                            updateDiscountPrice(discountedPrice);
                            dismiss();
                        }
                    }
                });
            }
        });
        return dialog;
    }

    private Double getDiscountPrice() {
        Double discountPrice = mMRP;
        if (mEditTextDiscountPrice != null) {
            String discountPriceString = Objects.requireNonNull(mEditTextDiscountPrice.getText()).toString();
            discountPrice = Double.parseDouble(discountPriceString);
        } else {
            Log.e(TAG, "Error: unable to get discount price");
        }
        return discountPrice;
    }

    /**
     * Calls the registered listener with the updated value of discount price
     */
    private void updateDiscountPrice(Double discountPrice) {
        if (mListener != null) {
            mListener.onPriceConfirm(discountPrice);
        } else {
            Log.e(TAG, "Error while updating discount price");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.dialog_set_price, null);
        // initialise views
        mEditTextLayout = mDialogView.findViewById(R.id.layout_edit_text_discount_price);
        mEditTextDiscountPrice = mDialogView.findViewById(R.id.edit_text_discount_price);
        mEditTextDiscountPrice.requestFocus();
        UIUtils.showKeyboard(requireContext());
    }
}

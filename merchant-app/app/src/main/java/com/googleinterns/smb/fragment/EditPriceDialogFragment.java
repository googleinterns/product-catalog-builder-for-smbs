package com.googleinterns.smb.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.googleinterns.smb.R;

import java.util.Objects;

public class EditPriceDialogFragment extends DialogFragment {


    public interface OptionSelectListener {
        void onConfirm(Double discountPrice);
    }

    private static final String TAG = "EditPriceDialogFragment";
    private OptionSelectListener listener;
    private View mDialogView;
    private TextInputEditText mEditTextDiscountPrice;
    private TextInputLayout mEditTextLayout;
    private Double mMRP;

    public EditPriceDialogFragment(OptionSelectListener listener, Double mrp) {
        mMRP = mrp;
        this.listener = listener;
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
                        // ignore
                        closeKeyboard();
                    }
                })
                .setPositiveButton(R.string.done, null); // listener overridden later
        final Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        // manually set on click listener to positive button for validation before dismissing the dialog
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Double discountedPrice = getDiscountPrice();
                        // discount price greater than MRP is not allowed
                        if (discountedPrice > mMRP) {
                            mEditTextLayout.setError("Error: price must be less than MRP");
                        } else {
                            closeKeyboard();
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
        if (listener != null) {
            listener.onConfirm(discountPrice);
        } else {
            Log.e(TAG, "Error while updating discount price");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.set_price, null);
        // initialise views
        mEditTextLayout = mDialogView.findViewById(R.id.text_layout_discount_price);
        mEditTextDiscountPrice = mDialogView.findViewById(R.id.text_field_discount_price);
        mEditTextDiscountPrice.requestFocus();
        showKeyboard();
    }

    private void showKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error: showKeyboard", e);
        }
    }

    private void closeKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error: closeKeyboard", e);
        }
    }
}
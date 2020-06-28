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
 * Dialog for adding discount in {@link com.googleinterns.smb.BillingActivity}
 */
public class AddDiscountDialogFragment extends DialogFragment {

    public interface DiscountDialogInterface {
        void onDiscountSelect(double discount);

        double getTotalPrice();
    }

    private static final String TAG = AddDiscountDialogFragment.class.getName();

    private DiscountDialogInterface mListener;
    private View mDialogView;
    private TextInputEditText mDiscount;
    private TextInputLayout mDiscountTextLayout;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(mDialogView)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ignore
                        UIUtils.closeKeyboard(requireContext());
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
                        double discount = getDiscount();
                        double total = mListener.getTotalPrice();
                        if (total < discount) {
                            mDiscountTextLayout.setError("Error: discount greater than total");
                        } else {
                            UIUtils.closeKeyboard(requireContext());
                            mListener.onDiscountSelect(discount);
                            dismiss();
                        }
                    }
                });
            }
        });
        return dialog;
    }

    private double getDiscount() {
        double discount = 0;
        try {
            discount = Double.parseDouble((Objects.requireNonNull(mDiscount.getText()).toString()));
        } catch (Exception e) {
            Log.e(TAG, "Error: unable to parse discount percent", e);
        }
        return discount;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the DiscountDialogInterface so we can send events to the host
            mListener = (DiscountDialogInterface) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement " + DiscountDialogInterface.class.getName());
        }
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.dialog_add_discount, null);
        mDiscount = mDialogView.findViewById(R.id.edit_text_discount);
        mDiscountTextLayout = mDialogView.findViewById(R.id.layout_edit_text_discount);
        mDiscount.requestFocus();
        UIUtils.showKeyboard(requireContext());
    }
}

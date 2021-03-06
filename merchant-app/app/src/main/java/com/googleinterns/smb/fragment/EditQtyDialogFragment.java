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
 * Dialog to edit item quantity in billing activity
 */
public class EditQtyDialogFragment extends DialogFragment {

    public interface QtyConfirmationListener {
        void onQtyConfirm(int qty);
    }

    private static final String TAG = "EditPriceDialogFragment";
    private QtyConfirmationListener mListener;
    private View mDialogView;
    private TextInputEditText mEditTextQty;
    private TextInputLayout mEditTextLayout;

    public EditQtyDialogFragment(QtyConfirmationListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(mDialogView)
                .setTitle(R.string.set_quantity)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                        int qty;
                        try {
                            qty = getQty();
                        } catch (NumberFormatException e) {
                            mEditTextLayout.setError("Invalid quantity");
                            return;
                        }
                        UIUtils.closeKeyboard(requireContext());
                        updateQty(qty);
                        dismiss();
                    }
                });
            }
        });
        return dialog;
    }

    private int getQty() {
        int mQty;
        String qtyString = Objects.requireNonNull(mEditTextQty.getText()).toString();
        mQty = Integer.parseInt(qtyString);
        return mQty;
    }

    /**
     * Calls the registered listener with the updated value of quantity
     */
    private void updateQty(int qty) {
        if (mListener != null) {
            mListener.onQtyConfirm(qty);
        } else {
            Log.e(TAG, "Error while updating quantity");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.dialog_set_qty, null);
        // Initialise views
        mEditTextLayout = mDialogView.findViewById(R.id.layout_edit_text_qty);
        mEditTextQty = mDialogView.findViewById(R.id.edit_text_qty);
        mEditTextQty.requestFocus();
        UIUtils.showKeyboard(requireContext());
    }
}

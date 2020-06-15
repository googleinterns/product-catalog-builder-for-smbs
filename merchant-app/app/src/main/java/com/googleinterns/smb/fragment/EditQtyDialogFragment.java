package com.googleinterns.smb.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

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

    public interface OptionSelectListener {
        void onConfirm(int qty);
    }

    private static final String TAG = "EditPriceDialogFragment";
    private OptionSelectListener listener;
    private View mDialogView;
    private TextInputEditText mEditTextQty;
    private TextInputLayout mEditTextLayout;

    public EditQtyDialogFragment(OptionSelectListener listener) {
        this.listener = listener;
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
                        // ignore
                        UIUtils.closeKeyboard(requireContext());
                    }
                })
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UIUtils.closeKeyboard(requireContext());
                        updateQty(getQty());
                    }
                }); // listener overridden later
        return builder.create();
    }

    private int getQty() {
        int mQty;
        try {
            String qtyString = Objects.requireNonNull(mEditTextQty.getText()).toString();
            mQty = Integer.parseInt(qtyString);
        } catch (Exception e) {
            Log.e(TAG, "Error: unable to get quantity");
            mQty = 1;
        }
        return mQty;
    }

    /**
     * Calls the registered listener with the updated value of quantity
     */
    private void updateQty(int qty) {
        if (listener != null) {
            listener.onConfirm(qty);
        } else {
            Log.e(TAG, "Error while updating quantity");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.set_qty, null);
        // initialise views
        mEditTextLayout = mDialogView.findViewById(R.id.text_layout_qty);
        mEditTextQty = mDialogView.findViewById(R.id.text_field_qty);
        mEditTextQty.requestFocus();
        UIUtils.showKeyboard(requireContext());
    }
}

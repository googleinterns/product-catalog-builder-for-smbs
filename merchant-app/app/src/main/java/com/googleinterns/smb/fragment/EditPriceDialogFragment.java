package com.googleinterns.smb.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.googleinterns.smb.R;

import java.util.Objects;

public class EditPriceDialogFragment extends DialogFragment {


    public interface OptionSelectListener {
        void onConfirm(Double discountPrice);
    }

    private static final String TAG = "EditPriceDialogFragment";
    private OptionSelectListener listener;
    private View mDialogView;
    private TextInputEditText discountPrice;

    public EditPriceDialogFragment(OptionSelectListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(mDialogView)
                .setTitle(R.string.set_discount_price)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // ignore
                        closeKeyboard();
                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeKeyboard();
                        if (discountPrice != null && listener != null) {
                            String dp = Objects.requireNonNull(discountPrice.getText()).toString();
                            Double d = Double.parseDouble(dp);
                            listener.onConfirm(d);
                        }
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.set_price, null);
        // initialise views
        discountPrice = mDialogView.findViewById(R.id.text_field_discount_price);
        discountPrice.requestFocus();
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

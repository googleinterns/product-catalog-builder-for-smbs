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

import com.googleinterns.smb.R;
import com.googleinterns.smb.common.UIUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddDiscountDialogFragment extends DialogFragment {

    public interface OptionSelectListener {
        void onDiscountSelect(int percent);
    }

    private static final String TAG = "AddDiscountDialog";

    private OptionSelectListener listener;
    private View mDialogView;
    private TextInputEditText mDiscountPercent;
    private TextInputLayout mDiscountPercentTextLayout;

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
                        int percent = getPercent();
                        // check for valid percent
                        if (percent > 100 || percent < 0) {
                            mDiscountPercentTextLayout.setError("Error: discount percent must be between 0 and 100");
                        } else {
                            UIUtils.closeKeyboard(requireContext());
                            listener.onDiscountSelect(percent);
                            dismiss();
                        }
                    }
                });
            }
        });
        return dialog;
    }

    private int getPercent() {
        int percent = 0;
        try {
            percent = Integer.parseInt(mDiscountPercent.getText().toString());
        } catch (Exception e) {
            Log.e(TAG, "Error: unable to parse discount percent", e);
        }
        return percent;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (OptionSelectListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement OptionSelectListener");
        }
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.add_discount_dialog, null);
        mDiscountPercent = mDialogView.findViewById(R.id.text_field_discount_percent);
        mDiscountPercentTextLayout = mDialogView.findViewById(R.id.text_layout_discount_percent);
    }
}

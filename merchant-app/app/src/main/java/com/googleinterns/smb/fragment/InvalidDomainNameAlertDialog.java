package com.googleinterns.smb.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.googleinterns.smb.R;

/**
 * Dialog to alert merchant that no valid domain was set. See {@link com.googleinterns.smb.SettingsActivity}
 */
public class InvalidDomainNameAlertDialog extends DialogFragment {

    public interface OnConfirmListener {
        void onConfirm();
    }

    private OnConfirmListener mListener;

    public InvalidDomainNameAlertDialog(OnConfirmListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.invalid_domain_alert_msg)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onConfirm();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }
}

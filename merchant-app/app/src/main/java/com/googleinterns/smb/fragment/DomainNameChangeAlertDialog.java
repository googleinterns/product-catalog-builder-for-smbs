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
 * Dialog to alert change of domain name which will render all previous domain name web pages invalid. See {@link com.googleinterns.smb.SettingsActivity}
 */
public class DomainNameChangeAlertDialog extends DialogFragment {

    public interface OnConfirmListener {
        void onConfirm();
    }

    private OnConfirmListener mListener;

    public DomainNameChangeAlertDialog(OnConfirmListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.domain_name_change_alert_msg)
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

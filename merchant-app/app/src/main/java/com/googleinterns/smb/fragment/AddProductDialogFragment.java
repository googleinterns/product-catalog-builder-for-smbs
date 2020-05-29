package com.googleinterns.smb.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.googleinterns.smb.R;

public class AddProductDialogFragment extends DialogFragment {

    public interface OptionSelectListener {
        void onUploadSelect();

        void onScanSelect();
    }

    private OptionSelectListener listener;
    private View mDialogView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(mDialogView)
                .setTitle(R.string.choose_an_option);
        return builder.create();
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
        mDialogView = inflater.inflate(R.layout.select_dialog, null);
        View scanLayout = mDialogView.findViewById(R.id.scanLayout);
        View uploadLayout = mDialogView.findViewById(R.id.uploadLayout);
        scanLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onScanSelect();
            }
        });
        uploadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onUploadSelect();
            }
        });
    }
}
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

/**
 * Dialog for showing different options to add new product to inventory. See {@link com.googleinterns.smb.InventoryActivity}
 */
public class AddProductDialogFragment extends DialogFragment {

    public interface OptionSelectListener {
        void onImageUploadSelect();

        void onVideoUploadSelect();

        void onScanSelect();

        void onScanTextSelect();
    }

    private OptionSelectListener mListener;
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
            mListener = (OptionSelectListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement OptionSelectListener");
        }
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.dialog_select, null);
        // Initialise views
        View scanLayout = mDialogView.findViewById(R.id.layout_scan);
        View imageLayout = mDialogView.findViewById(R.id.layout_image);
        View videoLayout = mDialogView.findViewById(R.id.layout_video);
        View ocrLayout = mDialogView.findViewById(R.id.layout_ocr);

        // Attach listener
        scanLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onScanSelect();
            }
        });
        imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onImageUploadSelect();
            }
        });
        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onVideoUploadSelect();
            }
        });
        ocrLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onScanTextSelect();
            }
        });
    }
}

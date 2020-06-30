package com.googleinterns.smb.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.googleinterns.smb.R;
import com.googleinterns.smb.adapter.OfferAdapter;
import com.googleinterns.smb.model.Offer;

import java.util.List;

/**
 * Dialog to view offers in {@link com.googleinterns.smb.ProductOfferActivity}. See {@link com.googleinterns.smb.adapter.ProductOfferAdapter}
 */
public class ViewOfferDialogFragment extends DialogFragment implements OfferAdapter.OfferActionListener {

    public interface OffersDialogInterface {
        void onAddOfferSelect();

        void onEditOfferSelect(int offerIdx);

        void onDeleteOfferSelect(int offerIdx);
    }

    private static final String TAG = ViewOfferDialogFragment.class.getName();
    private View mDialogView;
    private List<Offer> mOffers;
    private OffersDialogInterface mListener;

    public ViewOfferDialogFragment(List<Offer> offers, OffersDialogInterface listener) {
        mOffers = offers;
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(mDialogView)
                .setTitle(R.string.offers)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        final Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.dialog_view_offer, null);

        RecyclerView recyclerView = mDialogView.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(new OfferAdapter(this, mOffers));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        });

        Button addOffer = mDialogView.findViewById(R.id.button_add_offer);
        addOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddOfferSelect();
            }
        });
    }

    @Override
    public void onEditOfferSelect(int position) {
        mListener.onEditOfferSelect(position);
    }

    @Override
    public void onDeleteOfferSelect(int position) {
        mListener.onDeleteOfferSelect(position);
    }
}

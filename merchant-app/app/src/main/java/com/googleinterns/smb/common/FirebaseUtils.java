package com.googleinterns.smb.common;

import android.content.Context;

import androidx.annotation.NonNull;

import com.googleinterns.smb.model.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUtils {

    public interface OnProductReceivedListener {
        void onProductReceived(List<Product> products);
    }

    // utility class shouldn't be instantiated
    private FirebaseUtils() {

    }

    public static void queryProducts(final Context context, List<String> barcodes) {
        final OnProductReceivedListener listener;
        try {
            listener = (OnProductReceivedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement OnProductReceivedListener");
        }
        if (barcodes.isEmpty()) {
            listener.onProductReceived(new ArrayList<Product>());
            return;
        }
        Query query = FirebaseFirestore.getInstance().collection("products");
        // filter products containing these barcodes
        query = query.whereIn("EAN", barcodes);
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Product> products = new ArrayList<>();
                        for (DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()) {
                            DocumentSnapshot documentSnapshot = documentChange.getDocument();
                            Product product = new Product(documentSnapshot);
                            products.add(product);
                        }
                        listener.onProductReceived(products);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        UIUtils.showToast(context, "Could'nt fetch products from database");
                    }
                });
    }
}

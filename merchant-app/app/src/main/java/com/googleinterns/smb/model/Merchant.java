package com.googleinterns.smb.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Merchant {

    public interface OnDataUpdatedListener {
        void onDataUpdateSuccess();

        void onDataUpdateFailure();
    }

    public interface OnProductFetchedListener {
        void onProductFetched(List<Product> products);
    }

    private final static String TAG = "Merchant";

    private FirebaseFirestore firestore;
    private String mid;

    public Merchant() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // user must sign in before use
        assert user != null;
        String uid = user.getUid();
        mid = uid;
        String name = user.getDisplayName();
        String email = user.getEmail();
        final Map<String, String> data = new HashMap<>();
        data.put("mid", uid);
        data.put("name", name);
        data.put("email", email);
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("merchants")
                .document(uid)
                .set(data);
    }

    public void addProducts(final OnDataUpdatedListener listener, List<Product> products) {
        WriteBatch batch = firestore.batch();
        for (Product product : products) {
            String collectionPath = "merchants/" + mid + "/products";
            DocumentReference documentReference = firestore.collection(collectionPath).document(product.getEAN());
            batch.set(documentReference, product.createFirebaseDocument());
        }
        batch.commit()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            listener.onDataUpdateSuccess();
                        } else {
                            listener.onDataUpdateFailure();
                        }
                    }
                });
    }

    /**
     * Returns all products which are not yet present in the merchant's inventory out of all products in list 'products'
     */
    public void getNewProducts(final OnProductFetchedListener listener, List<Product> products) {
        if (products.isEmpty())
            return;
        List<String> barcodes = new ArrayList<>();
        final Map<String, Product> eanToProduct = new HashMap<>();
        for (Product product : products) {
            barcodes.add(product.getEAN());
            eanToProduct.put(product.getEAN(), product);
        }
        Query query = firestore.collection("merchants/" + mid + "/products").whereIn("EAN", barcodes);
        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot queryDocumentSnapshots = task.getResult();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Product product = new Product(document);
                                // remove existing products
                                eanToProduct.remove(product.getEAN());
                            }
                            listener.onProductFetched(new ArrayList<>(eanToProduct.values()));
                        } else {
                            Log.e(TAG, "Error retrieving documents ", task.getException());
                        }
                    }
                });

    }
}

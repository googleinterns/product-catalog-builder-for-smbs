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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Merchant class using singleton pattern which represents the current merchant.
 */
public class Merchant {

    /**
     * Listener interface for database update status
     */
    public interface OnDataUpdatedListener {
        void onDataUpdateSuccess();

        void onDataUpdateFailure();
    }

    /**
     * Listener interface to receive retrieved products from database
     */
    public interface OnProductFetchedListener {
        void onProductFetched(List<Product> products);
    }

    private final static String TAG = "Merchant";

    // unique merchant UID given by firebase auth
    private String mid;
    // merchant name
    private String name;
    // merchant email
    private String email;
    // merchant device token, used to send notifications by FCM
    private String token;

    // merchant singleton instance
    private static Merchant mInstance;

    /**
     * Initialise merchant and update in database, reinitialise information if already present
     */
    protected Merchant() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // user must sign in before use
        assert user != null;
        String uid = user.getUid();
        mid = uid;
        name = user.getDisplayName();
        email = user.getEmail();
        final Map<String, String> data = new HashMap<>();
        data.put("mid", uid);
        data.put("name", name);
        data.put("email", email);
        FirebaseFirestore.getInstance().collection("merchants")
                .document(uid)
                .set(data);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        updateToken(Objects.requireNonNull(task.getResult()).getToken());
                    }
                });
    }

    /**
     * Get singleton merchant instance
     *
     * @return merchant instance
     */
    public static synchronized Merchant getInstance() {
        if (mInstance != null) {
            return mInstance;
        }
        mInstance = new Merchant();
        return mInstance;
    }

    /**
     * Remove merchant instance
     */
    public static synchronized void removeInstance() {
        mInstance = null;
    }

    public void addProducts(final OnDataUpdatedListener listener, List<Product> products) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        for (Product product : products) {
            String collectionPath = "merchants/" + mid + "/products";
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection(collectionPath).document(product.getEAN());
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
        // map all detected products
        for (Product product : products) {
            barcodes.add(product.getEAN());
            eanToProduct.put(product.getEAN(), product);
        }
        Query query = FirebaseFirestore.getInstance().collection("merchants/" + mid + "/products").whereIn("EAN", barcodes);
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
                            // TODO handle failure
                            Log.e(TAG, "Error retrieving documents ", task.getException());
                        }
                    }
                });

    }

    /**
     * Update merchant device token
     */
    public void updateToken(String token) {
        this.token = token;
        FirebaseFirestore.getInstance().collection("merchants")
                .document(mid)
                .update("token", token);
    }

    public String getMid() {
        return mid;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }
}

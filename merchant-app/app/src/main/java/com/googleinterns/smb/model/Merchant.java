package com.googleinterns.smb.model;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

    // Unique merchant UID given by firebase auth
    private String mid;
    // Merchant name
    private String name;
    // Merchant email
    private String email;
    // Merchant device token, used to send notifications by FCM
    private String token;
    // Merchant photo URI
    private Uri photoUri;
    // Number of products in inventory
    private int numProducts;

    // Merchant singleton instance
    private static Merchant mInstance;

    /**
     * Initialise merchant and update in database, reinitialise information if already present
     */
    protected Merchant() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // User must sign in before use
        assert user != null;
        // Unique ID given by FirebaseAuth
        mid = user.getUid();
        name = user.getDisplayName();
        email = user.getEmail();
        photoUri = user.getPhotoUrl();
        numProducts = 0;
        final Map<String, Object> data = new HashMap<>();
        data.put("mid", mid);
        data.put("name", name);
        data.put("email", email);
        data.put("num_products", 0);
        FirebaseFirestore.getInstance().collection("merchants")
                .document(mid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String oldToken = null;
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (!document.exists()) {
                                FirebaseFirestore.getInstance().collection("merchants")
                                        .document(mid)
                                        .set(data);
                            } else {
                                oldToken = document.getString("token");
                                numProducts = document.getLong("num_products").intValue();
                            }
                            final String finalOldToken = oldToken;
                            FirebaseInstanceId.getInstance().getInstanceId()
                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                            if (!task.isSuccessful()) {
                                                Log.w(TAG, "getInstanceId failed", task.getException());
                                                return;
                                            }
                                            String newToken = task.getResult().getToken();
                                            if (!newToken.equals(finalOldToken)) {
                                                updateToken(Objects.requireNonNull(task.getResult()).getToken());
                                            }
                                        }
                                    });
                        }
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
        int startSno = numProducts + 1;
        for (Product product : products) {
            String collectionPath = "merchants/" + mid + "/products";
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection(collectionPath).document(product.getEAN());
            Map<String, Object> data = product.createFirebaseDocument();
            data.put("sno", startSno);
            startSno++;
            batch.set(documentReference, data);
        }
        numProducts += products.size();
        DocumentReference merchant = FirebaseFirestore.getInstance().collection("merchants").document(mid);
        batch.update(merchant, "num_products", numProducts);
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
        // Map all detected products
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

    public Uri getPhotoUri() {
        return photoUri;
    }
}
